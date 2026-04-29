package com.andyanh.cotienaddon.network;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.system.SectSavedData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

public class SectNetwork {

    // 1. Packet to open Sect Screen
    public record OpenSectPacket() implements CustomPacketPayload {
        public static final Type<OpenSectPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "open_sect"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenSectPacket> STREAM_CODEC = StreamCodec.unit(new OpenSectPacket());

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }

        public static void handle(OpenSectPacket pkt, IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                if (ctx.player() instanceof ServerPlayer sp) {
                    SectSavedData data = SectSavedData.get(sp.level());
                    SectSavedData.Sect sect = data.getSectOfPlayer(sp.getUUID());
                    if (sect != null) SectActionPacket.populateNames(sp.server, sect);
                    ctx.reply(new SyncSectPacket(sect));
                }
            });
        }
    }

    // 2. Packet to sync Sect Info to client
    public record SyncSectPacket(SectSavedData.Sect sect) implements CustomPacketPayload {
        public static final Type<SyncSectPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "sync_sect"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncSectPacket> STREAM_CODEC = StreamCodec.of(
                (buf, pkt) -> {
                    buf.writeBoolean(pkt.sect != null);
                    if (pkt.sect != null) {
                        buf.writeUUID(pkt.sect.id);
                        buf.writeUtf(pkt.sect.name);
                        buf.writeUUID(pkt.sect.leader);
                        buf.writeEnum(pkt.sect.type);
                        buf.writeInt(pkt.sect.members.size());
                        for (UUID m : pkt.sect.members) {
                            buf.writeUUID(m);
                            buf.writeUtf(pkt.sect.memberNames.getOrDefault(m, m.toString().substring(0, 8)));
                        }
                        // home
                        buf.writeBoolean(pkt.sect.homePos != null);
                        if (pkt.sect.homePos != null) {
                            buf.writeInt(pkt.sect.homePos.getX());
                            buf.writeInt(pkt.sect.homePos.getY());
                            buf.writeInt(pkt.sect.homePos.getZ());
                            buf.writeUtf(pkt.sect.homeDimension != null ? pkt.sect.homeDimension : "");
                        }
                    }
                },
                buf -> {
                    if (!buf.readBoolean()) return new SyncSectPacket(null);
                    UUID id = buf.readUUID();
                    String name = buf.readUtf();
                    UUID leader = buf.readUUID();
                    SectSavedData.SectType type = buf.readEnum(SectSavedData.SectType.class);
                    SectSavedData.Sect sect = new SectSavedData.Sect(id, name, leader, type);
                    sect.members.clear();
                    int size = buf.readInt();
                    for (int i = 0; i < size; i++) {
                        UUID m = buf.readUUID();
                        String mName = buf.readUtf();
                        sect.members.add(m);
                        sect.memberNames.put(m, mName);
                    }
                    if (buf.readBoolean()) {
                        sect.homePos = new net.minecraft.core.BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
                        sect.homeDimension = buf.readUtf();
                    }
                    return new SyncSectPacket(sect);
                }
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }

        public static void handle(SyncSectPacket pkt, IPayloadContext ctx) {
            // Client side handling will be in CoTienClientHandler
        }
    }

    // 3. Packet for Actions (Create, Invite, Kick, Leave, Set Home)
    public record SectActionPacket(int action, String stringData) implements CustomPacketPayload {
        public static final Type<SectActionPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "sect_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SectActionPacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, SectActionPacket::action,
                ByteBufCodecs.STRING_UTF8, SectActionPacket::stringData,
                SectActionPacket::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }

        public enum ActionType {
            CREATE, INVITE, KICK, LEAVE, SET_HOME, ACCEPT, DENY
        }

        public static void handle(SectActionPacket pkt, IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                if (ctx.player() instanceof ServerPlayer sp) {
                    ActionType type = ActionType.values()[pkt.action % ActionType.values().length];
                    handleAction(sp, type, pkt.stringData);
                }
            });
        }

        private static void handleAction(ServerPlayer sp, ActionType action, String data) {
            SectSavedData sectData = SectSavedData.get(sp.level());
            SectSavedData.Sect currentSect = sectData.getSectOfPlayer(sp.getUUID());

            switch (action) {
                case CREATE -> {
                    if (currentSect != null) return;
                    // Check cost 100k Yuan Shi from Elder Gu
                    if (consumeYuanShi(sp, 100000)) {
                        net.guzhenren.network.GuzhenrenModVariables.PlayerVariables vars = sp.getData(net.guzhenren.network.GuzhenrenModVariables.PLAYER_VARIABLES);
                        SectSavedData.SectType type = (vars.zhuanshu >= 6.0) ? SectSavedData.SectType.IMMORTAL : SectSavedData.SectType.MORTAL;
                        
                        sectData.createSect(data, sp.getUUID(), type);
                        
                        // Set home if possible
                        SectSavedData.Sect newSect = sectData.getSectOfPlayer(sp.getUUID());
                        if (newSect != null) {
                            newSect.homePos = sp.blockPosition();
                            newSect.homeDimension = sp.level().dimension().location().toString();
                            sectData.setDirty();
                        }
                        
                        // Sync back with names
                        SectSavedData.Sect created = sectData.getSectOfPlayer(sp.getUUID());
                        if (created != null) populateNames(sp.server, created);
                        sp.connection.send(new SyncSectPacket(created));
                        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a[Tông Môn/Team] Đã thành lập §l" + data + " §a(" + type.displayName + ")!"));
                    } else {
                        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[Tông Môn/Team] Không đủ 100,000 Nguyên Thạch trong Nguyên Lão Cổ!"));
                    }
                }
                case LEAVE -> {
                    if (currentSect != null) {
                        boolean wasLeader = currentSect.leader.equals(sp.getUUID());
                        sectData.removeMember(sp.getUUID());
                        sp.connection.send(new SyncSectPacket(null));
                        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7[Tông Môn] Bạn đã rời khỏi tông môn."));
                        // Notify remaining members
                        SectSavedData.Sect updated = sectData.getSectOfPlayer(
                                currentSect.members.stream().filter(m -> !m.equals(sp.getUUID())).findFirst().orElse(null));
                        if (updated != null) syncToAllMembers(sp, sectData, updated);
                    }
                }
                case INVITE -> {
                    if (currentSect == null || !currentSect.leader.equals(sp.getUUID())) return;
                    String targetName = data;
                    net.minecraft.server.level.ServerPlayer target = sp.server.getPlayerList().getPlayerByName(targetName);
                    if (target == null) {
                        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[Tông Môn] Không tìm thấy §f" + targetName + " §c(phải online)"));
                        return;
                    }
                    if (sectData.getSectOfPlayer(target.getUUID()) != null) {
                        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[Tông Môn] " + targetName + " đã thuộc tông môn khác!"));
                        return;
                    }
                    SectSavedData.addPendingInvite(target.getUUID(), currentSect.id);
                    target.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "§6[Tông Môn] §f" + sp.getName().getString() + " §7mời bạn vào §6" + currentSect.name
                            + "§7.\n§a/cotien sect accept §7hoặc §c/cotien sect deny"));
                    sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a[Tông Môn] Đã gửi lời mời đến §f" + targetName));
                }
                case KICK -> {
                    if (currentSect == null || !currentSect.leader.equals(sp.getUUID())) return;
                    UUID targetUUID = UUID.fromString(data);
                    if (targetUUID.equals(sp.getUUID())) return; // không tự kick mình
                    net.minecraft.server.level.ServerPlayer targetP = sp.server.getPlayerList().getPlayer(targetUUID);
                    sectData.removeMember(targetUUID);
                    if (targetP != null) {
                        targetP.connection.send(new SyncSectPacket(null));
                        targetP.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[Tông Môn] Bạn đã bị đuổi khỏi tông môn!"));
                    }
                    SectSavedData.Sect updated2 = sectData.getSectOfPlayer(sp.getUUID());
                    syncToAllMembers(sp, sectData, updated2);
                }
                case SET_HOME -> {
                    if (currentSect == null || !currentSect.leader.equals(sp.getUUID())) return;
                    currentSect.homePos = sp.blockPosition();
                    currentSect.homeDimension = sp.level().dimension().location().toString();
                    sectData.setDirty();
                    sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "§a[Tông Môn] Điểm Home đặt tại §f" + sp.blockPosition().toShortString()));
                    syncToAllMembers(sp, sectData, currentSect);
                }
                case ACCEPT -> {
                    UUID sectId = SectSavedData.getPendingInvite(sp.getUUID());
                    if (sectId == null) {
                        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[Tông Môn] Không có lời mời nào!"));
                        return;
                    }
                    if (sectData.getSectOfPlayer(sp.getUUID()) != null) {
                        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[Tông Môn] Bạn đã thuộc tông môn rồi!"));
                        return;
                    }
                    sectData.addMember(sectId, sp.getUUID());
                    SectSavedData.Sect joined = sectData.getSect(sectId);
                    if (joined != null) {
                        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a[Tông Môn] Bạn đã gia nhập §6" + joined.name));
                        syncToAllMembers(sp, sectData, joined);
                    }
                }
                case DENY -> {
                    SectSavedData.getPendingInvite(sp.getUUID()); // remove
                    sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7[Tông Môn] Đã từ chối lời mời."));
                }
            }
        }

        /** Gửi SyncSectPacket có tên thành viên đến tất cả member online */
        private static void syncToAllMembers(net.minecraft.server.level.ServerPlayer caller, SectSavedData sectData, SectSavedData.Sect sect) {
            if (sect == null) return;
            populateNames(caller.server, sect);
            for (UUID m : sect.members) {
                net.minecraft.server.level.ServerPlayer mp = caller.server.getPlayerList().getPlayer(m);
                if (mp != null) mp.connection.send(new SyncSectPacket(sect));
            }
        }

        public static void populateNames(net.minecraft.server.MinecraftServer server, SectSavedData.Sect sect) {
            for (UUID m : sect.members) {
                net.minecraft.server.level.ServerPlayer online = server.getPlayerList().getPlayer(m);
                sect.memberNames.put(m, online != null ? online.getName().getString() : m.toString().substring(0, 8));
            }
        }

        private static boolean consumeYuanShi(ServerPlayer sp, double amount) {
            if (sp.isCreative()) return true;

            double total = 0;
            List<ItemStack> elderGus = new ArrayList<>();
            
            // Try all possible variations of the tag
            String[] keys = {
                "Số lượng Nguyên thạch trong Nguyên Lão Cổ",
                "Số lượng nguyên thạch trong Nguyên Lão Cổ",
                "Số lượng nguyên thạch trong nguyên lão cổ",
                "Nguyên thạch",
                "nguyên thạch"
            };

            for (int i = 0; i < sp.getInventory().getContainerSize(); i++) {
                ItemStack stack = sp.getInventory().getItem(i);
                if (stack.isEmpty()) continue;
                
                CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                if (customData != null) {
                    CompoundTag tag = customData.copyTag();
                    for (String key : keys) {
                        if (tag.contains(key)) {
                            double val = tag.getDouble(key);
                            if (val > 0) {
                                total += val;
                                elderGus.add(stack);
                                break;
                            }
                        }
                    }
                }
            }

            if (total < amount) return false;

            double remainingToDeduct = amount;
            for (ItemStack stack : elderGus) {
                CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                if (customData == null) continue;
                
                CompoundTag tag = customData.copyTag();
                for (String key : keys) {
                    if (tag.contains(key)) {
                        double count = tag.getDouble(key);
                        double deduct = Math.min(count, remainingToDeduct);
                        final double newCount = count - deduct;
                        final String foundKey = key;
                        
                        CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> t.putDouble(foundKey, newCount));
                        
                        remainingToDeduct -= deduct;
                        break;
                    }
                }
                if (remainingToDeduct <= 0) break;
            }
            return true;
        }
    }
}
