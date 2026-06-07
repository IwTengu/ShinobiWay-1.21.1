package net.iwtengu.shinobiway.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientEyeData {

    private static final Map<UUID, Boolean> EYE_UNLOCKED = new HashMap<>();

    public static void set(UUID uuid, boolean unlocked) {
        EYE_UNLOCKED.put(uuid, unlocked);
    }

    public static boolean get(UUID uuid) {
        return EYE_UNLOCKED.getOrDefault(uuid, false);
    }
}