package com.andyanh.cotienaddon.network;

import com.andyanh.cotienaddon.CoTienAddon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// Client → Server: yêu cầu mở GUI Không Khiếu
public record OpenKhongKhieuPacket() implements CustomPacketPayload {

    public static final Type<OpenKhongKhieuPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "open_khong_khieu"));

    public static final StreamCodec<ByteBuf, OpenKhongKhieuPacket> STREAM_CODEC =
            StreamCodec.unit(new OpenKhongKhieuPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
