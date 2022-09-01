package de.jo.boattracker.util;

public class SpeedConverter {

    public static float getMetersPerSecondToKnots(float ms) {
        return ms * 1.94f;//1.943844f
    }

    public static float getKnotsToMetersPerSecond(float kn) {
        return kn / 1.94f;
    }

    public static float shortenSpeed(float speed) {
        char[] c = String.valueOf(speed).toCharArray();
        StringBuilder rs = new StringBuilder();
        int current = 0;
        for (char s : c) {
            if (!String.valueOf(s).equals(".")) {
                rs.append(s);
                current ++;
            } else {
                rs.append(".");
                current ++;
                rs.append(c[current]);
                current ++;
                rs.append(c[current]);
                break;
            }
        }
        return Float.parseFloat(rs.toString());
    }
}
