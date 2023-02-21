package core;

import java.util.UUID;

public class JevaUtils {
    public static String generateUUID() {
        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();

        return uuidAsString;
    }

    public static int roundInt(double val) {
        return (int) Math.round(val);
    }

    public static double roundDouble(double val) {
        return Math.round(val);
    }
}
