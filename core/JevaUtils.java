package core;

import java.util.UUID;

public class JevaUtils {
    public static String generateUUID() {
        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();

        return uuidAsString;
    }
}
