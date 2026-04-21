package com.andyanh.cotienaddon.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * action=0: save location (name, x, y, z)
 * action=1: delete location (index)
 */
public record SaveLocationPacket(int action, String name, double x, double y, double z, int deleteIndex)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SaveLocationPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("cotienaddon", "save_location"));

    public static final StreamCodec<ByteBuf, SaveLocationPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,    SaveLocationPacket::action,
                    ByteBufCodecs.STRING_UTF8, SaveLocationPacket::name,
                    ByteBufCodecs.DOUBLE, SaveLocationPacket::x,
                    ByteBufCodecs.DOUBLE, SaveLocationPacket::y,
                    ByteBufCodecs.DOUBLE, SaveLocationPacket::z,
                    ByteBufCodecs.INT,    SaveLocationPacket::deleteIndex,
                    SaveLocationPacket::new
            );

    public static final int ACTION_SAVE   = 0;
    public static final int ACTION_DELETE = 1;

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
