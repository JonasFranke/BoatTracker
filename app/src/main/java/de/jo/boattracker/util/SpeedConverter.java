package de.jo.boattracker.util;

public class SpeedConverter {

    public static double getMetersPerSecondToKnots(double ms) {
        return ms * 1.943844;
    }

    public static double getKnotsToMetersPerSecond(double kn) {
        return kn / 1.943844;
    }
}
