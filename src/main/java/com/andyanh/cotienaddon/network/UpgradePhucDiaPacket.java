package com.andyanh.cotienaddon.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UpgradePhucDiaPacket(int upgradeType) implements CustomPacketPayload {

    public static final int TYPE_LEVEL      = 0;
    public static final int TYPE_PRODUCTION = 1;
    public static final int TYPE_TIME       = 2;
    public static final int TYPE_DEFENSE    = 3;
    public static final int TYPE_LINGMAI         = 4;
    public static final int TYPE_THACH_NHAN_SLOT  = 5; // Mở thêm slot Thạch Nhân (vô hạn)
    public static final int TYPE_BUY_THACH_NHAN   = 6; // Mua Thạch Nhân mới
    public static final int TYPE_DIALINHSTORAGE   = 7; // Nâng cấp kho Địa Linh (27→54 slots)
    public static final int TYPE_CALL_THACH_NHAN  = 8;
    public static final int TYPE_TP_THACH_NHAN    = 9;
    public static final int TYPE_DIALINH_SKILL_DMG = 10;
    public static final int TYPE_DIALINH_SKILL_HP  = 11;

    public static final Type<UpgradePhucDiaPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("cotienaddon", "upgrade_phuc_dia"));

    public static final StreamCodec<ByteBuf, UpgradePhucDiaPacket> STREAM_CODEC =
            ByteBufCodecs.INT.map(UpgradePhucDiaPacket::new, UpgradePhucDiaPacket::upgradeType);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
