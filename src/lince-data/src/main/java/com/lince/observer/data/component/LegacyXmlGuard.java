package com.lince.observer.data.component;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import com.lince.observer.data.component.LegacyXmlException.Reason;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Allow-list check run on a Lince 1 document before it is handed to {@link java.beans.XMLDecoder} (LO-349).
 * <p>
 * {@code XMLDecoder} instantiates any class and calls any method the XML describes, so decoding an uploaded file is
 * remote code execution unless the document is restricted first. Only the subset {@link java.beans.XMLEncoder} writes
 * for Lince 1 registers and observation tools is accepted: a {@code <java>} root, objects of the legacy bean classes,
 * {@code java.util} collections and boxed literals, bean properties, {@code add}/{@code put} calls and array indexes.
 * Static calls ({@code method=}/{@code field=} on {@code object}, {@code class=} on {@code void}), any other method,
 * the {@code class}/{@code classLoader} properties, DTDs, entities and processing instructions are refused.
 * </p>
 * The check is lexical on the exact bytes the decoder will read; it does not load any class.
 */
public final class LegacyXmlGuard {

    static final int MAX_DEPTH = 200;

    private static final Set<String> LEGACY_CLASSES = Set.of(
            "lince.modelo.InstrumentoObservacional.RootInstrumentoObservacional",
            "lince.modelo.InstrumentoObservacional.NodoInformacion",
            "lince.modelo.InstrumentoObservacional.Criterio",
            "lince.modelo.InstrumentoObservacional.Categoria",
            "lince.modelo.FilaRegistro",
            "com.lince.observer.legacy.instrumentoObservacional.RootInstrumentoObservacional",
            "com.lince.observer.legacy.instrumentoObservacional.NodoInformacion",
            "com.lince.observer.legacy.instrumentoObservacional.Criterio",
            "com.lince.observer.legacy.instrumentoObservacional.Categoria",
            "com.lince.observer.legacy.FilaRegistro");

    private static final Set<String> JDK_CLASSES = Set.of(
            "java.util.HashMap",
            "java.util.ArrayList",
            "java.util.LinkedHashMap",
            "java.util.LinkedList",
            "javax.swing.tree.DefaultMutableTreeNode",
            "java.lang.String",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Boolean",
            "java.lang.Double",
            "java.lang.Float");

    /** Component types accepted on {@code <array class=...>} besides the allowed classes. */
    private static final Set<String> PRIMITIVES = Set.of(
            "int", "long", "short", "byte", "boolean", "float", "double", "char");

    private static final Set<String> LITERALS = Set.of(
            "string", "int", "long", "short", "byte", "boolean", "float", "double", "char", "null");

    private static final Set<String> METHODS = Set.of("add", "put");

    private static final Set<String> FORBIDDEN_PROPERTIES = Set.of("class", "classLoader");

    private static final Pattern PROPERTY = Pattern.compile("[A-Za-z][A-Za-z0-9_]*");
    private static final Pattern NON_NEGATIVE_INT = Pattern.compile("0|[1-9][0-9]{0,8}");
    private static final Pattern ID = Pattern.compile("[A-Za-z0-9_]+");

    private static final String DECODER_CLASS = "java.beans.XMLDecoder";

    private LegacyXmlGuard() {
    }

    /**
     * @param className a fully qualified class name exactly as written in the document
     * @return true when the class may appear in {@code object} or {@code array} elements
     */
    public static boolean isAllowedClass(String className) {
        return LEGACY_CLASSES.contains(className) || JDK_CLASSES.contains(className);
    }

    /**
     * Refuses a document that is not a plain Lince 1 bean graph.
     *
     * @param xml the exact bytes that will be passed to {@link java.beans.XMLDecoder}
     * @throws LegacyXmlException naming the offending element or attribute and its line
     */
    public static void check(byte[] xml) throws LegacyXmlException {
        Deque<String> open = new ArrayDeque<>();
        XMLStreamException malformed = parse(xml, open);
        if (malformed != null && !open.isEmpty()) {
            // XMLEncoder output that was flushed but never closed (every Lince 1 file, and encodeToXml) has no closing
            // tags; XMLDecoder reads it anyway. Close what was open and check again: the decoder reads the same prefix.
            byte[] closed = withClosingTags(xml, open);
            malformed = parse(closed, new ArrayDeque<>());
        }
        if (malformed != null) {
            Location l = malformed.getLocation();
            throw new LegacyXmlException(Reason.MALFORMED, "Not a valid Lince 1 file: malformed XML"
                    + (l != null ? " at line " + l.getLineNumber() : "") + ": " + malformed.getMessage(), malformed);
        }
    }

    /** @return the parse error, or null when the whole document passed; {@code open} holds the unclosed elements */
    private static XMLStreamException parse(byte[] xml, Deque<String> open) throws LegacyXmlException {
        XMLInputFactory factory = XMLInputFactory.newDefaultFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        XMLStreamReader reader = null;
        try {
            reader = factory.createXMLStreamReader(new ByteArrayInputStream(xml));
            walk(reader, open);
            return null;
        } catch (XMLStreamException e) {
            return e;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException ignored) {
                    // nothing to release
                }
            }
        }
    }

    private static byte[] withClosingTags(byte[] xml, Deque<String> open) {
        StringBuilder tail = new StringBuilder("\n");
        for (String name : open) { // innermost first
            tail.append("</").append(name).append('>');
        }
        byte[] extra = tail.toString().getBytes(StandardCharsets.US_ASCII);
        byte[] out = Arrays.copyOf(xml, xml.length + extra.length);
        System.arraycopy(extra, 0, out, xml.length, extra.length);
        return out;
    }

    private static void walk(XMLStreamReader r, Deque<String> stack) throws XMLStreamException, LegacyXmlException {
        boolean rootSeen = false;
        while (r.hasNext()) {
            int event = r.next();
            switch (event) {
                case XMLStreamConstants.START_ELEMENT -> {
                    String name = elementName(r);
                    String parent = stack.peek();
                    if (parent == null) {
                        if (rootSeen || !"java".equals(name)) {
                            throw reject(r, Reason.STRUCTURE, "root element must be <java>, found <" + name + ">");
                        }
                        rootSeen = true;
                    }
                    if (stack.size() >= MAX_DEPTH) {
                        throw reject(r, Reason.TOO_DEEP, "elements nested too deep (more than " + MAX_DEPTH + " levels)");
                    }
                    checkPlacement(r, name, parent);
                    checkElement(r, name);
                    stack.push(name);
                }
                case XMLStreamConstants.END_ELEMENT -> stack.pop();
                case XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA, XMLStreamConstants.SPACE -> {
                    String parent = stack.peek();
                    boolean literal = parent != null && LITERALS.contains(parent) && !"null".equals(parent);
                    if (!literal && !r.isWhiteSpace()) {
                        throw reject(r, Reason.STRUCTURE, "unexpected text inside " + (parent == null ? "the document" : "<" + parent + ">"));
                    }
                }
                case XMLStreamConstants.DTD -> throw reject(r, Reason.DTD, "DOCTYPE / DTD is not allowed");
                case XMLStreamConstants.ENTITY_REFERENCE -> throw reject(r, Reason.DTD, "entity reference &" + r.getLocalName()
                        + "; is not allowed");
                case XMLStreamConstants.ENTITY_DECLARATION, XMLStreamConstants.NOTATION_DECLARATION ->
                        throw reject(r, Reason.DTD, "DOCTYPE / DTD declarations are not allowed");
                case XMLStreamConstants.PROCESSING_INSTRUCTION ->
                        throw reject(r, Reason.PROCESSING_INSTRUCTION, "processing instruction <?" + r.getPITarget() + "?> is not allowed");
                default -> {
                    // comments, document start/end
                }
            }
        }
        if (!rootSeen) {
            throw new LegacyXmlException(Reason.STRUCTURE, "Not a valid Lince 1 file: no <java> root element");
        }
    }

    private static String elementName(XMLStreamReader r) throws LegacyXmlException {
        String prefix = r.getPrefix();
        String ns = r.getNamespaceURI();
        if ((prefix != null && !prefix.isEmpty()) || (ns != null && !ns.isEmpty()) || r.getNamespaceCount() > 0) {
            throw reject(r, Reason.NAMESPACE, "namespaces are not allowed (element <" + r.getLocalName() + ">)");
        }
        return r.getLocalName();
    }

    /** Where an element may appear: {@code void} needs a target, {@code char} only inside {@code string}. */
    private static void checkPlacement(XMLStreamReader r, String name, String parent) throws LegacyXmlException {
        if (parent == null) {
            return;
        }
        if ("java".equals(name)) {
            throw reject(r, Reason.STRUCTURE, "nested <java> element");
        }
        if ("void".equals(name) && "java".equals(parent)) {
            throw reject(r, Reason.STRUCTURE, "<void> directly under <java> is not allowed");
        }
        if ("string".equals(parent) && !"char".equals(name)) {
            throw reject(r, Reason.STRUCTURE, "<" + name + "> inside <string> is not allowed");
        }
        if (LITERALS.contains(parent) && !"string".equals(parent)) {
            throw reject(r, Reason.STRUCTURE, "<" + name + "> inside <" + parent + "> is not allowed");
        }
    }

    private static void checkElement(XMLStreamReader r, String name) throws LegacyXmlException {
        Map<String, String> attrs = attributes(r, name);
        switch (name) {
            case "java" -> {
                allowOnly(r, name, attrs, Set.of("version", "class"));
                String cls = attrs.get("class");
                if (cls != null && !DECODER_CLASS.equals(cls)) {
                    throw reject(r, Reason.CLASS, "<java class=\"" + cls + "\"> must be " + DECODER_CLASS);
                }
            }
            case "object" -> checkObject(r, attrs);
            case "void" -> checkVoid(r, attrs);
            case "array" -> checkArray(r, attrs);
            case "char" -> allowOnly(r, name, attrs, Set.of("id", "code"));
            default -> {
                if (!LITERALS.contains(name)) {
                    throw reject(r, Reason.ELEMENT, "element <" + name + "> is not allowed");
                }
                allowOnly(r, name, attrs, Set.of("id"));
            }
        }
        String id = attrs.get("id");
        if (id != null && !ID.matcher(id).matches()) {
            throw reject(r, Reason.VALUE, "<" + name + " id=\"" + id + "\"> is not a valid id");
        }
        String idref = attrs.get("idref");
        if (idref != null && !ID.matcher(idref).matches()) {
            throw reject(r, Reason.VALUE, "<" + name + " idref=\"" + idref + "\"> is not a valid id");
        }
    }

    private static void checkObject(XMLStreamReader r, Map<String, String> attrs) throws LegacyXmlException {
        allowOnly(r, "object", attrs, Set.of("class", "id", "idref"));
        String cls = attrs.get("class");
        boolean ref = attrs.containsKey("idref");
        if (cls == null && !ref) {
            throw reject(r, Reason.STRUCTURE, "<object> needs a class or an idref");
        }
        if (cls != null && ref) {
            throw reject(r, Reason.STRUCTURE, "<object> cannot have both class and idref");
        }
        if (cls != null && !isAllowedClass(cls)) {
            throw reject(r, Reason.CLASS, "<object class=\"" + cls + "\"> is not an allowed class");
        }
    }

    private static void checkVoid(XMLStreamReader r, Map<String, String> attrs) throws LegacyXmlException {
        allowOnly(r, "void", attrs, Set.of("property", "method", "index", "id", "idref"));
        int actions = (attrs.containsKey("property") ? 1 : 0) + (attrs.containsKey("method") ? 1 : 0)
                + (attrs.containsKey("index") ? 1 : 0);
        if (actions > 1) {
            throw reject(r, Reason.STRUCTURE, "<void> may have only one of property, method, index");
        }
        if (actions == 0 && !attrs.containsKey("idref")) {
            throw reject(r, Reason.STRUCTURE, "<void> needs a property, method, index or idref");
        }
        String method = attrs.get("method");
        if (method != null && !METHODS.contains(method)) {
            throw reject(r, Reason.METHOD, "<void method=\"" + method + "\"> is not allowed (only add, put)");
        }
        String property = attrs.get("property");
        if (property != null && (!PROPERTY.matcher(property).matches() || FORBIDDEN_PROPERTIES.contains(property))) {
            throw reject(r, Reason.PROPERTY, "<void property=\"" + property + "\"> is not allowed");
        }
        String index = attrs.get("index");
        if (index != null && !NON_NEGATIVE_INT.matcher(index).matches()) {
            throw reject(r, Reason.VALUE, "<void index=\"" + index + "\"> is not a non-negative integer");
        }
    }

    private static void checkArray(XMLStreamReader r, Map<String, String> attrs) throws LegacyXmlException {
        allowOnly(r, "array", attrs, Set.of("class", "length", "id"));
        String cls = attrs.get("class");
        if (cls == null) {
            throw reject(r, Reason.STRUCTURE, "<array> needs a class");
        }
        if (!isAllowedClass(cls) && !PRIMITIVES.contains(cls)) {
            throw reject(r, Reason.CLASS, "<array class=\"" + cls + "\"> is not an allowed class");
        }
        String length = attrs.get("length");
        if (length != null && !NON_NEGATIVE_INT.matcher(length).matches()) {
            throw reject(r, Reason.VALUE, "<array length=\"" + length + "\"> is not a non-negative integer");
        }
    }

    private static Map<String, String> attributes(XMLStreamReader r, String element) throws LegacyXmlException {
        Map<String, String> attrs = new HashMap<>();
        for (int i = 0; i < r.getAttributeCount(); i++) {
            String prefix = r.getAttributePrefix(i);
            String ns = r.getAttributeNamespace(i);
            String name = r.getAttributeLocalName(i);
            if ((prefix != null && !prefix.isEmpty()) || (ns != null && !ns.isEmpty())) {
                throw reject(r, Reason.NAMESPACE, "namespaced attribute " + prefix + ":" + name + " on <" + element + "> is not allowed");
            }
            attrs.put(name, r.getAttributeValue(i));
        }
        return attrs;
    }

    private static void allowOnly(XMLStreamReader r, String element, Map<String, String> attrs, Set<String> allowed)
            throws LegacyXmlException {
        for (String a : attrs.keySet()) {
            if (!allowed.contains(a)) {
                throw reject(r, Reason.ATTRIBUTE, "attribute " + a + "=\"" + attrs.get(a) + "\" is not allowed on <" + element + ">");
            }
        }
    }

    private static LegacyXmlException reject(XMLStreamReader r, Reason reason, String why) {
        Location l = r.getLocation();
        return new LegacyXmlException(reason, "Not a valid Lince 1 file: " + why
                + (l != null ? " (line " + l.getLineNumber() + ")" : ""));
    }
}
