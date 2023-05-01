package core;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

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

    protected static LinkedHashMap<String, JevaClip> mergeClipContainers(LinkedHashMap<String, JevaClip> clipMap1, LinkedHashMap<String, JevaClip> clipMap2) {
        LinkedHashMap<String, JevaClip> tempClipHierarchy = new LinkedHashMap<>(clipMap1);
        
        tempClipHierarchy.putAll(clipMap2);

        return tempClipHierarchy;
    }
    
    protected static LinkedHashMap<String, JevaClip> sortClipsByDepth(LinkedHashMap<String, JevaClip> clipHierarchy) {
        // 1. create temp LinkedHashMap
        LinkedHashMap<String, JevaClip> tempClipHierarchy = new LinkedHashMap<>(clipHierarchy);

        // 2. convert LinkedHashMap to List of Map.Entry
        List<Map.Entry<String, JevaClip>> clipListEntry = new ArrayList<Map.Entry<String, JevaClip>>(
                tempClipHierarchy.entrySet());

        // 3. sort list of entries using Collections class'
        // utility method sort(ls, cmptr)
        Collections.sort(clipListEntry,
                new Comparator<Map.Entry<String, JevaClip>>() {

                    @Override
                    public int compare(Entry<String, JevaClip> clip1,
                            Entry<String, JevaClip> clip2) {
                        return clip1.getValue().getDepth() - clip2.getValue().getDepth();
                    }
                });

        // 4. clear temp LinkedHashMap
        tempClipHierarchy.clear();

        // 5. iterating list and storing in LinkedHahsMap
        for (Map.Entry<String, JevaClip> map : clipListEntry) {
            tempClipHierarchy.put(map.getKey(), map.getValue());
        }

        // 6. return temp LinkedHashMap
        return tempClipHierarchy;
    }
}
