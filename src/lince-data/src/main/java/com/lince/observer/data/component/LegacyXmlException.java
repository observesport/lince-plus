package com.lince.observer.data.component;

import java.io.IOException;

/**
 * A Lince 1 (.rlince / .ilince) document was refused by {@link LegacyXmlGuard} before reaching
 * {@link java.beans.XMLDecoder}: it is malformed or describes something other than a plain Lince 1 bean graph.
 * {@link #getReason()} tells which rule refused it; the message names the element or attribute and the line.
 */
public class LegacyXmlException extends IOException {

    /** The guard rule a document broke. */
    public enum Reason {
        /** Not well-formed XML. */
        MALFORMED,
        /** A DOCTYPE, DTD declaration or entity reference. */
        DTD,
        /** A processing instruction other than the XML declaration. */
        PROCESSING_INSTRUCTION,
        /** Namespaced elements or attributes. */
        NAMESPACE,
        /** Wrong root, misplaced element, stray text or a missing/conflicting required attribute. */
        STRUCTURE,
        /** Elements nested deeper than the guard allows. */
        TOO_DEEP,
        /** An element name outside the grammar. */
        ELEMENT,
        /** An attribute not allowed on its element (method/field on object, class/field on void, ...). */
        ATTRIBUTE,
        /** A class outside the allow-list (object, array or the java root). */
        CLASS,
        /** A void method other than add/put. */
        METHOD,
        /** A forbidden or malformed property name. */
        PROPERTY,
        /** A malformed id, idref, index or length value. */
        VALUE
    }

    private final Reason reason;

    public LegacyXmlException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public LegacyXmlException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
