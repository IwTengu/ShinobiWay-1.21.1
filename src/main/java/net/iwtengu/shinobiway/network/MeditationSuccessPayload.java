package net.iwtengu.shinobiway.network;

import net.iwtengu.shinobiway.ShinobiWay;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MeditationSuccessPayload() implements CustomPacketPayload {

    public static final Type<MeditationSuccessPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShinobiWay.MOD_ID, "meditation_success")
    );

    public static final StreamCodec<FriendlyByteBuf, MeditationSuccessPayload> CODEC =
            StreamCodec.of((buf, p) -> {}, buf -> new MeditationSuccessPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}