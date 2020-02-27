package com.deicos.lince.data.system.operations;

import edu.stanford.ejalbert.BrowserLauncher;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

public class OSUtils {

    Logger log = LoggerFactory.getLogger(OSUtils.class);
    private OSDetector.SYS_TYPE sys_type;

    public OSUtils() {
        OSDetector os = new OSDetector();
        this.sys_type = os.getOS();
    }

    public void openBrowser(String link) {
        try {
            switch (sys_type) {
                case WIN:
                    System.out.println("Working for the man :)");
                    break;
                case MACOS:
                    Runtime.getRuntime().exec("open " + link);
                    break;
                case LINUX:
                case SOLARIS:
                case UNKNOWN:
                default:
                    System.out.println("Not supported");
            }
        } catch (Exception e) {

        }
    }


    /**
     * Old version.
     * Check what happens!
     * Opens any link in a browser
     *
     * @param url new url
     * @return action final status
     */
    public boolean openLinkInBrowser(String url, boolean doLanPrepare) {
        if (StringUtils.isNotEmpty(url)) {
            try {
                if (doLanPrepare && StringUtils.contains(url, "localhost")) {
                    url = StringUtils.replace(url, "localhost", InetAddress.getLocalHost().getHostAddress());
                }
                BrowserLauncher launcher = new BrowserLauncher();
                launcher.openURLinBrowser(url);
                return true;
            } catch (Exception e) {
                log.error("Exception on opening. Trying in raw mode", e);
                try {
                    Runtime rt = Runtime.getRuntime();
                    if (sys_type == OSDetector.SYS_TYPE.WIN) {
                        rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
                    } else if (sys_type == OSDetector.SYS_TYPE.MACOS) {
                        rt.exec("open " + url);
                    } else {
                        // Ubuntu
                        String[] browsers = {"epiphany", "firefox", "mozilla", "konqueror",
                                "netscape", "opera", "links", "lynx"};
                        StringBuffer cmd = new StringBuffer();
                        for (int i = 0; i < browsers.length; i++)
                            cmd.append((i == 0 ? "" : " || ") + browsers[i] + " \"" + url + "\" ");
                        rt.exec(new String[]{"sh", "-c", cmd.toString()});
                    }
                    return true;
                } catch (Exception e1) {
                    log.error("============ Controlled Exception on opening =============== ", e1);
                    return false;
                }
            }
        }
        return false;
    }
}
