package com.andyanh.cotienaddon.network;

import com.andyanh.cotienaddon.CoTienAddon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenThachNhanScreenPacket(int entityId) implements CustomPacketPayload {

    public static final Type<OpenThachNhanScreenPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "open_thach_nhan_screen"));

    public static final StreamCodec<ByteBuf, OpenThachNhanScreenPacket> STREAM_CODEC =
            ByteBufCodecs.INT.map(OpenThachNhanScreenPacket::new, OpenThachNhanScreenPacket::entityId);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
