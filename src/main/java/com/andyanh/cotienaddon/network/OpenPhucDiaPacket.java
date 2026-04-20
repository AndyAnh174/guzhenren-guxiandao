package com.andyanh.cotienaddon.network;

import com.andyanh.cotienaddon.CoTienAddon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// Client → Server: yêu cầu mở GUI Phúc Địa (server sync data về)
public record OpenPhucDiaPacket() implements CustomPacketPayload {

    public static final Type<OpenPhucDiaPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "open_phuc_dia"));

    public static final StreamCodec<ByteBuf, OpenPhucDiaPacket> STREAM_CODEC =
            StreamCodec.unit(new OpenPhucDiaPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
