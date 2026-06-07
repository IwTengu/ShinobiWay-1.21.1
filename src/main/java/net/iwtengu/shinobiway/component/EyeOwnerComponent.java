package net.iwtengu.shinobiway.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

/**
 * Компонент хранящий UUID владельца глаза и его ник.
 * Сериализуется в NBT предмета автоматически.
 */
public record EyeOwnerComponent(UUID ownerUUID, String ownerName) {

    public static final Codec<EyeOwnerComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.CODEC.fieldOf("owner_uuid").forGetter(EyeOwnerComponent::ownerUUID),
                    Codec.STRING.fieldOf("owner_name").forGetter(EyeOwnerComponent::ownerName)
            ).apply(instance, EyeOwnerComponent::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, EyeOwnerComponent> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, EyeOwnerComponent::ownerName,
                    UUIDUtil.STREAM_CODEC,     EyeOwnerComponent::ownerUUID,
                    (name, uuid) -> new EyeOwnerComponent(uuid, name)
            );
}