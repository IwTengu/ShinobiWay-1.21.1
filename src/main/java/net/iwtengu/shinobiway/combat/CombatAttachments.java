package net.iwtengu.shinobiway.combat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║                    CombatAttachments                        ║
 * ║  Регистрирует AttachmentType для CombatData.                ║
 * ║  NeoForge 1.21.1: вместо Capability — Data Attachments.     ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * Как работает:
 *  - AttachmentType регистрируется через DeferredRegister
 *  - Codec описы   вает что сохраняется (только unlocked)
 *  - active/dashCooldown НЕ в codec — они сбрасываются при логине
 *  - copyOnDeath(true) — unlocked копируется при смерти автоматически
 *
 * Использование:
 *   CombatData data = player.getData(CombatAttachments.COMBAT_DATA);
 */
public class CombatAttachments {

    /**
     * Регистрация attachment types.
     */
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(
                    NeoForgeRegistries.Keys.ATTACHMENT_TYPES,
                    "shinobiway"
            );

    /**
     * Codec для сохранения CombatData.
     * Сохраняем только unlocked.
     */
    private static final Codec<CombatData> COMBAT_DATA_CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.BOOL.fieldOf("unlocked")
                                    .forGetter(CombatData::isUnlocked)
                    ).apply(instance, unlocked -> {
                        CombatData data = new CombatData();
                        data.setUnlocked(unlocked);
                        return data;
                    })
            );

    /**
     * Attachment игрока.
     */
    public static final Supplier<AttachmentType<CombatData>> COMBAT_DATA =
            ATTACHMENT_TYPES.register("combat_data", () ->
                    AttachmentType.builder(CombatData::new)
                            .serialize(COMBAT_DATA_CODEC)
                            .copyOnDeath()
                            .build()
            );
}