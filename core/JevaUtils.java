package core;

import java.awt.Color;
import java.io.File;
import java.util.UUID;

public class JevaUtils {
    protected static JevaScript emptyScript = (self) -> {
    };

    public static String generateUUID() {
        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();

        return uuidAsString;
    }

    public static double lerp(double prev, double curr, double alpha) {
        return curr * alpha + prev * (1.0 - alpha);
    }

    public static int roundInt(double val) {
        return (int) Math.round(val);
    }

    public static float roundFloat(double val) {
        return (float) Math.round(val);
    }

    public static double roundDouble(double val) {
        return Math.round(val);
    }

    public static int clampInt(double val, double min, double max) {
        return (int) clampDouble(val, min, max);
    }

    public static float clampFloat(double val, double min, double max) {
        return (float) clampDouble(val, min, max);
    }

    public static double clampDouble(double val, double min, double max) {
        return Math.min(Math.max(val, min), max);
    }

    public static boolean fileExists(String path) {
        return new File(path).isFile();
    }

    public static Color color(String hex) {
        hex = hex.replace("#", "");
        switch (hex.length()) {
            case 3:
                return new Color(
                        Integer.valueOf(hex.substring(0, 1).concat(hex.substring(0, 1)), 16),
                        Integer.valueOf(hex.substring(1, 2).concat(hex.substring(1, 2)), 16),
                        Integer.valueOf(hex.substring(2, 3).concat(hex.substring(2, 3)), 16));
            case 4:
                return new Color(
                        Integer.valueOf(hex.substring(0, 1).concat(hex.substring(0, 1)), 16),
                        Integer.valueOf(hex.substring(1, 2).concat(hex.substring(1, 2)), 16),
                        Integer.valueOf(hex.substring(2, 3).concat(hex.substring(2, 3)), 16),
                        Integer.valueOf(hex.substring(3, 4).concat(hex.substring(3, 4)), 16));
            case 6:
                return new Color(
                        Integer.valueOf(hex.substring(0, 2), 16),
                        Integer.valueOf(hex.substring(2, 4), 16),
                        Integer.valueOf(hex.substring(4, 6), 16));
            case 8:
                return new Color(
                        Integer.valueOf(hex.substring(0, 2), 16),
                        Integer.valueOf(hex.substring(2, 4), 16),
                        Integer.valueOf(hex.substring(4, 6), 16),
                        Integer.valueOf(hex.substring(6, 8), 16));
        }
        return new Color(0, 0, 0);
    }
}
