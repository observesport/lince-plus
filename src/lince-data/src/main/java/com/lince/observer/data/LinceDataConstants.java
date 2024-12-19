package com.lince.observer.data;

/**
 * .data
 * Class LinceDataConstants
 * @author berto (alberto.soto@gmail.com). 27/04/2018
 */
public class LinceDataConstants {
    public static final String CSV_CHAR_SEPARATOR_COMMA =",";//;
    public static final String CSV_CHAR_SEPARATOR_SEMICOLON =";";
    public static final String CATEGORY_PREFIX = "cat";
    public static final String CRITERIA_PREFIX = "cri";
    public static final String CATEGORY_INFO_SUFIX = "-DATA";
    public static final int CATEGORY_INFO_ID_MULTIPLIER = 1000000;

    public static final String SUPPORTED_VIDEO_FILES = "*.mp4;*.mkv;*.mp3;*.flv;*.wmv;*.mpg;*.mpeg;*.ogv;*.mov;*.ogg;*.avi";
    public static final String PREFERENCES_FILE_PATH = "filePath";

    public static final String CTX_VIDEO_URL = "videoUrl";
    public static final String CTX_LOCALE = "locale";
    public static final String CTX_VIDEO_URL_LOCAL = "videoUrlLocal";
    public static final String CTX_VIDEO_URL_VIDEO = "videoUrlVideo";
    public static final String CTX_CATEGORIES = "categories";
    public static final String CTX_VIDEO_REGISTER = "videoRegister";
    public static final String CTX_NUM_VIDEOS = "num_videos";
    public static final String CTX_PLAYLIST = "playlist";
    public static final String CTX_FRAME_RATE = "frameRate";
    public static final String CTX_R_ATTRIBUTES = "rAttributes";
    public static final String CTX_PORT = "port";
    public static final String CTX_IS_SCENE = "isScenes";
    public static final String CTX_TIME="time";
    public static final String CTX_LINCE_VERSION = "lince_version";

    public enum ColumnType {
        EVENT_TIME_FRAMES("TFrames"),
        EVENT_TIME_SECONDS("TSegundos"),
        EVENT_TIME_MS("TMilisegundos"),
        EVENT_DURATION_FRAMES("DuracionFr"),
        EVENT_DURATION_SECONDS("DuracionSeg"),
        EVENT_DURATION_MS("DuracionMiliseg");

        private final String value;

        ColumnType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        public String getValue() {
            return value;
        }

        public static ColumnType fromValue(String value) {
            for (ColumnType type : values()) {
                if (type.getValue().equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("No enum constant with value: " + value);
        }
    }

    @Deprecated
    public static final String COL_TFRAMES = "TFrames";
    @Deprecated
    public static final String COL_DURACION_FR = "DuracionFr";
    @Deprecated
    public static final String COL_TSEGUNDOS = "TSegundos";
    @Deprecated
    public static final String COL_DURACION_SEC = "DuracionSeg";
    @Deprecated
    public static final String COL_TMILISEGUNDOS = "TMilisegundos";
    @Deprecated
    public static final String COL_DURACION_MS = "DuracionMiliseg";

    public static final double DEFAULT_FPS = 25.0;

}
