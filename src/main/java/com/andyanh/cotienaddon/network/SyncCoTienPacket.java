package com.andyanh.cotienaddon.network;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.data.CoTienData;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// Server → Client: sync CoTienData để mở GUI
public record SyncCoTienPacket(CompoundTag data) implements CustomPacketPayload {

    public static final Type<SyncCoTienPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "sync_co_tien"));

    public static final StreamCodec<ByteBuf, SyncCoTienPacket> STREAM_CODEC =
            ByteBufCodecs.COMPOUND_TAG.map(SyncCoTienPacket::new, SyncCoTienPacket::data);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public CoTienData toData() {
        return CoTienData.deserializeNBT(data);
    }
}
