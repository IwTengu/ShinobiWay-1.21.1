package net.iwtengu.shinobiway.player;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

public class ShurikenMasteryData {

    private static final String TAG_NAME = "shinobiway_data";
    private static final String SHURIKEN_MASTERY = "ShurikenMastery";

    public static int getShurikenMastery(ServerPlayer player) {
        CompoundTag tag = getMainTag(player);
        return tag.getInt(SHURIKEN_MASTERY);
    }

    public static void setShurikenMastery(ServerPlayer player, int value) {

        CompoundTag tag = getMainTag(player);

        if (value < 0) value = 0;
        if (value > 5000) value = 5000;

        tag.putInt(SHURIKEN_MASTERY, value);
    }

    public static void addShurikenMastery(ServerPlayer player, int amount) {

        int current = getShurikenMastery(player);

        setShurikenMastery(player, current + amount);

        checkAdvancements(player);
    }

    private static CompoundTag getMainTag(ServerPlayer player) {

        CompoundTag persistent = player.getPersistentData();

        if (!persistent.contains(TAG_NAME)) {
            persistent.put(TAG_NAME, new CompoundTag());
        }

        return persistent.getCompound(TAG_NAME);
    }

    private static void grant(ServerPlayer player, String idString) {

        ResourceLocation id = ResourceLocation.tryParse(idString);

        if (id == null) {
            return;
        }

        AdvancementHolder advancement = player.server
                .getAdvancements()
                .get(id);

        if (advancement == null) {
            return;
        }

        PlayerAdvancements progress = player.getAdvancements();

        if (!progress.getOrStartProgress(advancement).isDone()) {
            progress.award(advancement, "impossible");
        }
    }

    private static void checkAdvancements(ServerPlayer player) {

        int mastery = getShurikenMastery(player);

        if (mastery >= 1000) {
            grant(player, "shinobiway:mastery_1000");
        }

        if (mastery >= 2500) {
            grant(player, "shinobiway:mastery_2500");
        }

        if (mastery >= 5000) {
            grant(player, "shinobiway:mastery_5000");
        }
    }
}