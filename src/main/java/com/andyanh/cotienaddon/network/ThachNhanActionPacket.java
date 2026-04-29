package com.andyanh.cotienaddon.network;

import com.andyanh.cotienaddon.CoTienAddon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// action: 0=upgradeHP, 1=upgradeATK, 2=upgradeSPD, 3=dismiss
public record ThachNhanActionPacket(int entityId, int action) implements CustomPacketPayload {

    public static final int ACTION_HP  = 0;
    public static final int ACTION_ATK = 1;
    public static final int ACTION_SPD = 2;
    public static final int ACTION_DISMISS = 3;
    public static final int ACTION_BUY = 4;

    public static final Type<ThachNhanActionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "thach_nhan_action"));

    public static final StreamCodec<ByteBuf, ThachNhanActionPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, ThachNhanActionPacket::entityId,
                    ByteBufCodecs.INT, ThachNhanActionPacket::action,
                    ThachNhanActionPacket::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
