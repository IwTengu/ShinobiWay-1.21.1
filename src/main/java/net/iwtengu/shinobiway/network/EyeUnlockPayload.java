package net.iwtengu.shinobiway.network;

import net.iwtengu.shinobiway.ShinobiWay;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record EyeUnlockPayload(boolean unlocked) implements CustomPacketPayload {

    public static final Type<EyeUnlockPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShinobiWay.MOD_ID, "eye_unlock_sync")
    );

    public static final StreamCodec<FriendlyByteBuf, EyeUnlockPayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> buf.writeBoolean(payload.unlocked()),
                    buf -> new EyeUnlockPayload(buf.readBoolean())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}