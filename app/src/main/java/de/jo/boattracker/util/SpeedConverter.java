package de.jo.boattracker.util;

public class SpeedConverter {

    public static float getMetersPerSecondToKnots(float ms) {
        return ms * 1.943844f;
    }

    public static float getKnotsToMetersPerSecond(float kn) {
        return kn / 1.943844f;
    }
}
