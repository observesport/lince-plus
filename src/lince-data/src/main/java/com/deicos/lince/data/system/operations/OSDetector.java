package com.deicos.lince.data.system.operations;

import org.apache.commons.lang3.StringUtils;

public class OSDetector {

    enum SYS_TYPE {MACOS, WIN, LINUX, SOLARIS, UNKNOWN}

    private String sysvalue = StringUtils.EMPTY;

    public OSDetector() {
        this.sysvalue = System.getProperty("os.name").toLowerCase();
    }

    private boolean isWindows() {
        return StringUtils.contains(sysvalue, "win");

    }

    private boolean isMac() {

        return StringUtils.contains(sysvalue, "mac");

    }

    private boolean isUnix() {
        return StringUtils.contains(sysvalue, "nix") ||
                StringUtils.contains(sysvalue, "nux") ||
                StringUtils.contains(sysvalue, "aix");

    }

    private boolean isSolaris() {
        return StringUtils.contains(sysvalue, "sunos");
    }

    public SYS_TYPE getOS() {
        if (isMac()) {
            return SYS_TYPE.MACOS;
        } else if (isWindows()) {
            return SYS_TYPE.WIN;
        } else if (isUnix()) {
            return SYS_TYPE.LINUX;
        } else if (isSolaris()) {
            return SYS_TYPE.SOLARIS;
        } else {
            return SYS_TYPE.UNKNOWN;
        }
    }

}
