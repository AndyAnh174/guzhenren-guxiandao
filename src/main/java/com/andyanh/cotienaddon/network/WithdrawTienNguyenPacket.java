package com.andyanh.cotienaddon.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record WithdrawTienNguyenPacket(int amount) implements CustomPacketPayload {

    public static final Type<WithdrawTienNguyenPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("cotienaddon", "withdraw_tien_nguyen"));

    public static final StreamCodec<ByteBuf, WithdrawTienNguyenPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, WithdrawTienNguyenPacket::amount,
                    WithdrawTienNguyenPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
