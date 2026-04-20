package com.andyanh.cotienaddon.network;

import com.andyanh.cotienaddon.CoTienAddon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client → Server: mời hoặc kick thành viên.
 * action: 0 = invite (theo playerName), 1 = kick (theo memberUUID)
 */
public record ManageMemberPacket(int action, String target) implements CustomPacketPayload {

    public static final int ACTION_INVITE = 0;
    public static final int ACTION_KICK   = 1;

    public static final Type<ManageMemberPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "manage_member"));

    public static final StreamCodec<ByteBuf, ManageMemberPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,         ManageMemberPacket::action,
                    ByteBufCodecs.STRING_UTF8, ManageMemberPacket::target,
                    ManageMemberPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
