package com.andyanh.cotienaddon.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TeleportDinhTienDuPacket(double x, double y, double z) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TeleportDinhTienDuPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("cotienaddon", "teleport_dinh_tien_du"));

    public static final StreamCodec<ByteBuf, TeleportDinhTienDuPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, TeleportDinhTienDuPacket::x,
                    ByteBufCodecs.DOUBLE, TeleportDinhTienDuPacket::y,
                    ByteBufCodecs.DOUBLE, TeleportDinhTienDuPacket::z,
                    TeleportDinhTienDuPacket::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
