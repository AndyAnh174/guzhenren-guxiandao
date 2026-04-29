package com.andyanh.cotienaddon.network;

import com.andyanh.cotienaddon.CoTienAddon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SetTonHieuPacket(String name, int color, boolean enabled) implements CustomPacketPayload {

    public static final Type<SetTonHieuPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "set_ton_hieu"));

    public static final StreamCodec<ByteBuf, SetTonHieuPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, SetTonHieuPacket::name,
                    ByteBufCodecs.INT,         SetTonHieuPacket::color,
                    ByteBufCodecs.BOOL,        SetTonHieuPacket::enabled,
                    SetTonHieuPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
