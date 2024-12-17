package com.lince.observer.data.util;

/**
 *
 */
public class TimeCalculations {

    public Integer getVideoTimeMilis(Double videoTime) {
        return Math.toIntExact(Math.round(videoTime * 1000));
    }

    public int convertMsToFPS(int ms, double fps) {
        if (fps <= 0) {
            return ms / 40; //conversion to 25 fps by default
        }
        return (int) Math.round(ms * fps / 1000);
    }




}
