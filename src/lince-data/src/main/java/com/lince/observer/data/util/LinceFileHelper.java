package com.lince.observer.data.util;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.user.ResearchProfile;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Alberto Soto. 24/5/24
 */

public class LinceFileHelper {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Checks modification date on file
     * Null if it does not exist
     *
     * @param file current file
     * @return Modification date
     */
    public Date getLastModifiedDate(File file) {
        try {
            if (file != null) {
                return new Date(Files.readAttributes(file.toPath(), BasicFileAttributes.class).lastModifiedTime().toMillis());
            }
        } catch (IOException e) {
            log.error("opening file ", e);
        }
        return null;
    }

    /**
     * Built once: a {@link JAXBContext} is thread safe and expensive to create, and the REST import/export reads and
     * writes projects on every request.
     */
    private static volatile JAXBContext xmlContext;

    protected JAXBContext getXMLContext() throws JAXBException {
        JAXBContext context = xmlContext;
        if (context == null) {
            synchronized (LinceFileHelper.class) {
                context = xmlContext;
                if (context == null) {
                    context = JAXBContext.newInstance(LinceFileProjectWrapper.class, Criteria.class, Category.class);
                    xmlContext = context;
                }
            }
        }
        return context;
    }

    /**
     * 2019 - Functional function - DARK ZONE
     * Unmarshalls file, and executes desired code with it
     *
     * @param file Loaded file
     * @param f    Function to execute
     * @return Unmarshalled info
     */
    public LinceFileProjectWrapper readProjectFile(File file, Consumer<LinceFileProjectWrapper> f)
            throws JAXBException {
        LinceFileProjectWrapper data = new LinceFileProjectWrapper();
        if (file.exists() && !file.isDirectory()) {
            try (InputStream in = Files.newInputStream(file.toPath())) {
                data = readProject(in);
            } catch (IOException e) {
                throw new JAXBException("Could not read " + file.getPath(), e);
            }
        }
        f.accept(data);
        return data;
    }

    /**
     * Save info data as XML
     *
     * @param file             file
     * @param researchProfiles list profiles
     * @param criteria         criteria data
     * @param videos           videos
     * @param youtubeVideos    youtubeVideos
     * @param register         register
     * @return same file with data on it
     */
    public File saveFile(File file
            , List<ResearchProfile> researchProfiles
            , List<Criteria> criteria
            , List<File> videos
            , List<String> youtubeVideos
            , List<LinceRegisterWrapper> register) {
        try {
            JAXBContext context = getXMLContext();
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            LinceFileProjectWrapper wrapper = new LinceFileProjectWrapper();
            wrapper.setProfiles(researchProfiles);
            wrapper.setObservationTool(criteria);
            wrapper.setVideoPlayList(videos);
            wrapper.setYoutubeVideoPlayList(youtubeVideos);
            wrapper.setRegister(register);
            // Marshalling and saving XML to the file.
            m.marshal(wrapper, file);
        } catch (Exception e) {
            log.error("Error saving file", e);
        }
        return file;
    }
    public void writeProjectFile(File file, LinceFileProjectWrapper linceProject) {
        try {
            // Marshalling and saving XML to the file.
            createProjectMarshaller().marshal(toWritableWrapper(linceProject), file);
        } catch (Exception e) {
            log.error("Error writing project file", e);
            throw new RuntimeException("Failed to write project file", e);
        }
    }

    /**
     * Stream variant of {@link #writeProjectFile(File, LinceFileProjectWrapper)}: same JAXB settings and the same
     * content (profiles, instrument, youtube playlist, registers; local videos are not written). The stream is
     * not closed.
     *
     * @param out          target stream
     * @param linceProject project to serialise
     * @throws JAXBException when marshalling fails
     */
    public void writeProject(OutputStream out, ILinceProject linceProject) throws JAXBException {
        createProjectMarshaller().marshal(toWritableWrapper(linceProject), out);
    }

    /**
     * Stream variant of {@link #readProjectFile(File, Consumer)}. External entities and DTDs are disabled because
     * the content may come from an upload. The stream is not closed.
     *
     * @param in project XML
     * @return unmarshalled project
     * @throws JAXBException when the content is not a Lince PLUS project
     */
    public LinceFileProjectWrapper readProject(InputStream in) throws JAXBException {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        XMLStreamReader reader = null;
        try {
            reader = factory.createXMLStreamReader(in);
            Unmarshaller um = getXMLContext().createUnmarshaller();
            Object data = um.unmarshal(reader);
            if (data instanceof LinceFileProjectWrapper project) {
                return project;
            }
            throw new JAXBException("Not a Lince PLUS project: " + (data == null ? null : data.getClass().getSimpleName()));
        } catch (XMLStreamException e) {
            throw new JAXBException("Unreadable project XML", e);
        } finally {
            closeQuietly(reader);
        }
    }

    private void closeQuietly(XMLStreamReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (XMLStreamException e) {
                log.debug("closing xml reader", e);
            }
        }
    }

    private Marshaller createProjectMarshaller() throws JAXBException {
        Marshaller m = getXMLContext().createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        return m;
    }

    private LinceFileProjectWrapper toWritableWrapper(ILinceProject linceProject) {
        LinceFileProjectWrapper wrapper = new LinceFileProjectWrapper();
        wrapper.setProfiles(linceProject.getProfiles());
        wrapper.setObservationTool(linceProject.getObservationTool());
        wrapper.setYoutubeVideoPlayList(linceProject.getYoutubeVideoPlayList());
        wrapper.setRegister(linceProject.getRegister());
        return wrapper;
    }

}
