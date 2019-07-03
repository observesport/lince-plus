package com.deicos.lince.app.helper;

import com.deicos.lince.data.system.operations.OSUtils;
import com.google.common.base.Charsets;
import edu.stanford.ejalbert.BrowserLauncher;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;

/**
 * lince-scientific-desktop
 * com.deicos.lince.app.helper
 * Created by Alberto Soto Fernandez in 22/06/2016.
 * Description:
 */
public class ServerValuesHelper {

    public static final String BASE_URI = "http://localhost";


    public static String getServerURL(ApplicationContext context, Integer port) {
        String uri;
        try {
            if (port == 0 || port == null) {
                //String port = environment.getProperty("local.server.port");
                //port = ((AnnotationConfigEmbeddedWebApplicationContext) context).getEmbeddedServletContainer().getPort();
                //port = ((AnnotationConfigEmbeddedWebApplicationContext) context).getEmbeddedServletContainer().getPort();
            }
            uri = String.format(BASE_URI + ":%s", port);
            return new URL(uri).toString();
        } catch (Exception e) {
            System.out.println("ERR LOCALHOST:" + e.toString());
        }
        return BASE_URI + "/";
    }

    public static String getHtmlFileName(File file) {
        try {
            return UriUtils.encodePath(file.getName(), Charsets.UTF_8.toString());
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Gets loaded videos and creates a list with all needed parameters to show them on web
     * Uses tika for detecting video type (mandatory for videojs)
     * @return
     */


    /**
     * Opens browser in any system and condition and returns status of it
     *
     * @return
     */
    public static boolean openLinceBrowser(ApplicationContext context) {
        OSUtils osUtils = new OSUtils();
        return osUtils.openLinkInBrowser(getServerURL(context, null), false);
    }

    public static boolean openLANLinceBrowser(String url) {
        OSUtils osUtils = new OSUtils();
        return osUtils.openLinkInBrowser(url, true);
    }


    /**
     * https://github.com/sakaiproject/sakai/blob/master/entitybroker/utils/src/java/org/sakaiproject/entitybroker/util/spring/ResourceFinder.java
     * http://www.programcreek.com/java-api-examples/org.springframework.core.io.FileSystemResource
     *
     * @param path
     * @return
     */
    public static Resource makeResource(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        Resource r = null;
        // try the environment path first
        String envPath = getEnvironmentPath() + path;
        r = new FileSystemResource(envPath);
        if (!r.exists()) {
            // try the relative path next
            String relPath = getRelativePath() + path;
            r = new FileSystemResource(relPath);
            if (!r.exists()) {
                // now try the classloader
                ClassLoader cl = ServerValuesHelper.class.getClassLoader();
                r = new ClassPathResource(path, cl);
                if (!r.exists()) {
                    // finally try the system classloader
                    cl = ClassLoader.getSystemClassLoader();
                    r = new ClassPathResource(path, cl);
                }
            }
        }
        if (!r.exists()) {
            throw new IllegalArgumentException("Could not find this resource (" + path + ") in any of the checked locations");
        }
        return r;
    }

    public static String relativePath = "sakai/";
    public static String environmentPathVariable = "sakai.home";

    protected static String getEnvironmentPath() {
        String envPath = System.getenv(environmentPathVariable);
        if (envPath == null) {
            envPath = System.getProperty(environmentPathVariable);
            if (envPath == null) {
                String container = getContainerHome();
                if (container == null) {
                    container = "";
                }
                envPath = container + File.separatorChar + "sakai" + File.separatorChar;
            }
        }
        return envPath;
    }

    protected static String getContainerHome() {
        String catalina = System.getProperty("catalina.base");
        if (catalina == null) {
            catalina = System.getProperty("catalina.home");
        }
        return catalina;
    }

    protected static String getRelativePath() {
        File currentPath = new File("");
        File f = new File(currentPath, relativePath);
        if (!f.exists() || !f.isDirectory()) {
            f = new File(currentPath, "sakai");
            if (!f.exists() || !f.isDirectory()) {
                f = currentPath;
            }
        }
        String absPath = f.getAbsolutePath();
        if (!absPath.endsWith(File.separatorChar + "")) {
            absPath += File.separatorChar;
        }
        return absPath;
    }
}
