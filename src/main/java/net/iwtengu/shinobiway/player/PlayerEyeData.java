package net.iwtengu.shinobiway.player;

import com.mojang.serialization.Codec;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class PlayerEyeData {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, "shinobiway");

    // Флаг разблокировки. false по умолчанию, сохраняется между сессиями
    public static final Supplier<AttachmentType<Boolean>> EYE_SLOTS_UNLOCKED =
            ATTACHMENT_TYPES.register("eye_slots_unlocked", () ->
                    AttachmentType.builder(() -> false)
                            .serialize(Codec.BOOL) // стандартный Codec для Boolean
                            .copyOnDeath()
                            .build()
            );
}