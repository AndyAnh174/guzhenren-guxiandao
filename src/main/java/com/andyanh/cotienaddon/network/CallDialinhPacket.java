package com.andyanh.cotienaddon.network;

import com.andyanh.cotienaddon.CoTienAddon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// Client → Server: summonToMe=true → gọi Địa Linh về phía player; false → tele player đến Địa Linh
public record CallDialinhPacket(boolean summonToMe) implements CustomPacketPayload {

    public static final Type<CallDialinhPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "call_dialinhpacket"));

    public static final StreamCodec<ByteBuf, CallDialinhPacket> STREAM_CODEC =
            ByteBufCodecs.BOOL.map(CallDialinhPacket::new, CallDialinhPacket::summonToMe);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
