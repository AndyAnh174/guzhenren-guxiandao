package com.andyanh.cotienaddon.network;

import com.andyanh.cotienaddon.CoTienAddon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

// Client → Server: toggle một permission bit cho member
public record UpdatePermissionPacket(String memberUUID, int permBit, boolean value)
        implements CustomPacketPayload {

    public static final Type<UpdatePermissionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "update_permission"));

    public static final StreamCodec<ByteBuf, UpdatePermissionPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, UpdatePermissionPacket::memberUUID,
                    ByteBufCodecs.INT, UpdatePermissionPacket::permBit,
                    ByteBufCodecs.BOOL, UpdatePermissionPacket::value,
                    UpdatePermissionPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
