package com.andyanh.cotienaddon.network;

import com.andyanh.cotienaddon.CoTienAddon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// Client → Server: toggle một ecosystem setting
// setting: 0=fixedDay, 1=allowRain, 2=peacefulMobs, 3=guzhenrenMobs
public record EcosystemPacket(int setting, boolean value) implements CustomPacketPayload {

    public static final Type<EcosystemPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "ecosystem_packet"));

    public static final StreamCodec<ByteBuf, EcosystemPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,  EcosystemPacket::setting,
                    ByteBufCodecs.BOOL, EcosystemPacket::value,
                    EcosystemPacket::new);

    public static final int FIXED_DAY       = 0;
    public static final int ALLOW_RAIN      = 1;
    public static final int PEACEFUL_MOBS   = 2;
    public static final int GUZHENREN_MOBS  = 3;

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
