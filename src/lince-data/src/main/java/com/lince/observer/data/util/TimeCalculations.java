package com.lince.observer.data.util;

/**
 *
 */
public class TimeCalculations {

    public Integer getVideoTimeMilis(Double videoTime) {
        return Math.toIntExact(Math.round(videoTime * 1000));
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
            return ms / 40; //conversion to 25 fps by default
        }
        return Math.round(ms * fps / 1000);
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
        long millisecond = milis % 1000;
        String secondsValue = String.format("%03d", millisecond);
        milis /= 1000;
        long seconds = milis % 60;
        milis /= 60;
        long minutes = milis % 60;
        long hours = milis / 60;

        if (omitHoursIfZero && hours == 0) {
            return String.format("%d:%02d.%s", minutes, seconds, secondsValue);
        } else {
            return String.format("%d:%02d:%02d.%s", hours, minutes, seconds, secondsValue);
        }
    }
}
