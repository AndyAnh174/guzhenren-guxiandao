package com.andyanh.cotienaddon.network;

import com.andyanh.cotienaddon.CoTienAddon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// Client → Server: teleport vào/ra Phúc Địa
// enter = true: vào Phúc Địa của bản thân; false: ra Overworld
public record TeleportPhucDiaPacket(boolean enter) implements CustomPacketPayload {

    public static final Type<TeleportPhucDiaPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "teleport_phuc_dia"));

    public static final StreamCodec<ByteBuf, TeleportPhucDiaPacket> STREAM_CODEC =
            ByteBufCodecs.BOOL.map(TeleportPhucDiaPacket::new, TeleportPhucDiaPacket::enter);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
