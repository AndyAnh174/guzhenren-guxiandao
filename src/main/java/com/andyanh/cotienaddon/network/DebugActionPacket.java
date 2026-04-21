package com.andyanh.cotienaddon.network;

import com.andyanh.cotienaddon.CoTienAddon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DebugActionPacket(int action) implements CustomPacketPayload {
    public static final int ASCEND  = 0;
    public static final int COMPLETE = 1;
    public static final int RESET   = 2;

    public static final CustomPacketPayload.Type<DebugActionPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "debug_action"));
    public static final StreamCodec<ByteBuf, DebugActionPacket> STREAM_CODEC =
            ByteBufCodecs.INT.map(DebugActionPacket::new, DebugActionPacket::action);

    @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }
}
