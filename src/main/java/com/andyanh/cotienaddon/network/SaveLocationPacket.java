package com.andyanh.cotienaddon.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SaveLocationPacket(int action, String name, double x, double y, double z,
                                  int deleteIndex, String dimensionId)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SaveLocationPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("cotienaddon", "save_location"));

    public static final StreamCodec<ByteBuf, SaveLocationPacket> STREAM_CODEC =
            StreamCodec.of(
                (buf, pkt) -> {
                    buf.writeInt(pkt.action());
                    writeString(buf, pkt.name());
                    buf.writeDouble(pkt.x());
                    buf.writeDouble(pkt.y());
                    buf.writeDouble(pkt.z());
                    buf.writeInt(pkt.deleteIndex());
                    writeString(buf, pkt.dimensionId());
                },
                buf -> new SaveLocationPacket(
                    buf.readInt(),
                    readString(buf),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readInt(),
                    readString(buf)
                )
            );

    private static void writeString(ByteBuf buf, String s) {
        byte[] bytes = (s != null ? s : "").getBytes(java.nio.charset.StandardCharsets.UTF_8);
        buf.writeShort(bytes.length);
        buf.writeBytes(bytes);
    }

    private static String readString(ByteBuf buf) {
        int len = buf.readShort();
        byte[] bytes = new byte[len];
        buf.readBytes(bytes);
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    public static final int ACTION_SAVE   = 0;
    public static final int ACTION_DELETE = 1;

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
