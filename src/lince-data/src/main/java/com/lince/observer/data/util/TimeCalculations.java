package com.lince.observer.data.util;

/**
 *
 */
public class TimeCalculations {

    public static final double DEFAULT_FPS = 25.0;
    private static final int MILLISECONDS_PER_SECOND = 1000;

    public Integer getVideoTimeMillis(Double videoTime) {
        return Math.toIntExact(Math.round(videoTime * MILLISECONDS_PER_SECOND));
    }

    /**
     * Converts milliseconds to frames based on the given frames per second (FPS).
     * If the FPS is 0 or negative, it defaults to 25 FPS.
     *
     * @param ms  The time in milliseconds to convert.
     * @param fps The frames per second rate. If 0 or negative, 25 FPS is used as default.
     * @return The number of frames corresponding to the given milliseconds and FPS.
     */
    public long convertMsToFPS(long ms, double fps) {
        if (fps <= 0) {
            return Math.round(ms * DEFAULT_FPS / MILLISECONDS_PER_SECOND); // Default to 25 fps
        }
        return Math.round(ms * fps / MILLISECONDS_PER_SECOND);
    }

    public int convertMsToFrames(long ms, double fps) {
        return Math.toIntExact(convertMsToFPS(ms, fps));
    }

    /**
     * Formats a given time in milliseconds into a string representation that includes all time components.
     * The resulting format is "HH:MM:SS.mmm" where HH is hours, MM is minutes, SS is seconds, and mmm is milliseconds.
     * This method always includes the hours component, even if it's zero.
     *
     * @param ms The time to format, in milliseconds.
     * @return A string representation of the time in the format "HH:MM:SS.mmm".
     */
    public String formatTimeWithOptionalHours(long ms) {
        return formatMilliseconds(ms, true);
    }

    /**
     * Formats a given time in milliseconds into a string representation that includes all time components.
     * The resulting format is "HH:MM:SS.mmm" where HH is hours, MM is minutes, SS is seconds, and mmm is milliseconds.
     * This method always includes the hours component, even if it's zero.
     *
     * @param ms The time to format, in milliseconds.
     * @return A string representation of the time in the format "HH:MM:SS.mmm".
     */
    public String formatTimeWithAllComponents(long ms) {
        return formatMilliseconds(ms, false);
    }

    private String formatMilliseconds(long milis, boolean omitHoursIfZero) {
        long millisecond = milis % MILLISECONDS_PER_SECOND;
        String millisecondsValue = String.format("%03d", millisecond);
        milis /= MILLISECONDS_PER_SECOND;
        long seconds = milis % 60;
        milis /= 60;
        long minutes = milis % 60;
        long hours = milis / 60;

        if (omitHoursIfZero && hours == 0) {
            return String.format("%02d:%02d.%s", minutes, seconds, millisecondsValue);
        } else {
            return String.format("%02d:%02d:%02d.%s", hours, minutes, seconds, millisecondsValue);
        }
    }
}
