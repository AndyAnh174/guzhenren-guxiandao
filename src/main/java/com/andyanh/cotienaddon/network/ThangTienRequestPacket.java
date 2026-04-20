package com.andyanh.cotienaddon.network;

import com.andyanh.cotienaddon.CoTienAddon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// Client → Server: player bấm nút Đột Phá, yêu cầu bắt đầu thăng tiên
public record ThangTienRequestPacket() implements CustomPacketPayload {

    public static final Type<ThangTienRequestPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "thang_tien_request"));

    public static final StreamCodec<ByteBuf, ThangTienRequestPacket> STREAM_CODEC =
            StreamCodec.unit(new ThangTienRequestPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
