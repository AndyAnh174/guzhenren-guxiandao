package com.andyanh.cotienaddon.network;

import com.andyanh.cotienaddon.CoTienAddon;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AnnexPhucDiaPacket(CompoundTag nodeData) implements CustomPacketPayload {

    public static final Type<AnnexPhucDiaPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "annex_phuc_dia"));

    public static final StreamCodec<ByteBuf, AnnexPhucDiaPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        byte[] bytes = nbtToBytes(pkt.nodeData());
                        buf.writeInt(bytes.length);
                        buf.writeBytes(bytes);
                    },
                    buf -> {
                        int len = buf.readInt();
                        byte[] bytes = new byte[len];
                        buf.readBytes(bytes);
                        return new AnnexPhucDiaPacket(bytesToNbt(bytes));
                    });

    private static byte[] nbtToBytes(CompoundTag tag) {
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            net.minecraft.nbt.NbtIo.write(tag, new java.io.DataOutputStream(baos));
            return baos.toByteArray();
        } catch (Exception e) { return new byte[0]; }
    }

    private static CompoundTag bytesToNbt(byte[] bytes) {
        try {
            return net.minecraft.nbt.NbtIo.read(
                    new java.io.DataInputStream(new java.io.ByteArrayInputStream(bytes)));
        } catch (Exception e) { return new CompoundTag(); }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
