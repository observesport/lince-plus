package com.lince.observer.data.util;

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

import java.io.File;
import java.io.IOException;
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

    protected JAXBContext getXMLContext() throws JAXBException {
        return JAXBContext.newInstance(LinceFileProjectWrapper.class, Criteria.class, Category.class);
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
        JAXBContext context = getXMLContext();
        Unmarshaller um = context.createUnmarshaller();
        // Reading XML from the file and unmarshalling.
        LinceFileProjectWrapper data = new LinceFileProjectWrapper();
        if (file.exists() && !file.isDirectory()) {
            //Por defecto va a la carpeta de usuarios
            data = (LinceFileProjectWrapper) um.unmarshal(file);
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
            JAXBContext context = getXMLContext();
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            LinceFileProjectWrapper wrapper = new LinceFileProjectWrapper();
            wrapper.setProfiles(linceProject.getProfiles());
            wrapper.setObservationTool(linceProject.getObservationTool());
            wrapper.setYoutubeVideoPlayList(linceProject.getYoutubeVideoPlayList());
            wrapper.setRegister(linceProject.getRegister());
            // Marshalling and saving XML to the file.
            m.marshal(wrapper, file);
        } catch (Exception e) {
            log.error("Error writing project file", e);
            throw new RuntimeException("Failed to write project file", e);
        }
    }

}
