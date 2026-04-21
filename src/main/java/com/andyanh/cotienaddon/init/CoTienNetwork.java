package com.andyanh.cotienaddon.init;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.item.DinhTienDuItem;
import com.andyanh.cotienaddon.network.*;
import com.andyanh.cotienaddon.system.PhucDiaManager;
import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = CoTienAddon.MODID, bus = EventBusSubscriber.Bus.MOD)
public class CoTienNetwork {

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar reg = event.registrar("1");

        reg.playBidirectional(
                SyncCoTienPacket.TYPE,
                SyncCoTienPacket.STREAM_CODEC,
                CoTienNetwork::handleSyncPacket
        );
        reg.playToServer(
                OpenKhongKhieuPacket.TYPE,
                OpenKhongKhieuPacket.STREAM_CODEC,
                CoTienNetwork::handleOpenKhongKhieu
        );
        reg.playToServer(
                ThangTienRequestPacket.TYPE,
                ThangTienRequestPacket.STREAM_CODEC,
                CoTienNetwork::handleThangTienRequest
        );
        reg.playToServer(
                OpenPhucDiaPacket.TYPE,
                OpenPhucDiaPacket.STREAM_CODEC,
                CoTienNetwork::handleOpenPhucDia
        );
        reg.playToServer(
                UpdatePermissionPacket.TYPE,
                UpdatePermissionPacket.STREAM_CODEC,
                CoTienNetwork::handleUpdatePermission
        );
        reg.playToServer(
                ManageMemberPacket.TYPE,
                ManageMemberPacket.STREAM_CODEC,
                CoTienNetwork::handleManageMember
        );
        reg.playToServer(
                TeleportPhucDiaPacket.TYPE,
                TeleportPhucDiaPacket.STREAM_CODEC,
                CoTienNetwork::handleTeleportPhucDia
        );
        reg.playToServer(
                DebugActionPacket.TYPE,
                DebugActionPacket.STREAM_CODEC,
                CoTienNetwork::handleDebugAction
        );
        reg.playToServer(
                TeleportDinhTienDuPacket.TYPE,
                TeleportDinhTienDuPacket.STREAM_CODEC,
                CoTienNetwork::handleTeleportDinhTienDu
        );
        reg.playToServer(
                SaveLocationPacket.TYPE,
                SaveLocationPacket.STREAM_CODEC,
                CoTienNetwork::handleSaveLocation
        );
    }

    // --- Không Khiếu ---

    private static net.minecraft.nbt.CompoundTag buildSyncNBT(ServerPlayer sp) {
        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
        net.minecraft.nbt.CompoundTag tag = data.serializeNBT();
        // Inject guzhenren vars so client can show conditions
        var gv = sp.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
        tag.putDouble("zhuanshu", gv.zhuanshu);
        tag.putDouble("jieduan", gv.jieduan);
        return tag;
    }

    private static void handleOpenKhongKhieu(OpenKhongKhieuPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            PacketDistributor.sendToPlayer(sp, new SyncCoTienPacket(buildSyncNBT(sp)));
        });
    }

    private static void handleThangTienRequest(ThangTienRequestPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;

            var guzhenrenVars = sp.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
            if (guzhenrenVars.zhuanshu < 5.0 || guzhenrenVars.jieduan < 4.0) {
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§c[Tiên Khiếu] Cần đạt Ngũ Chuyển Đỉnh Phong (zhuanshu=5.0, jieduan≥4) mới có thể Thăng Tiên!"));
                return;
            }

            CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
            if (data.thangTienPhase != 0) return;

            com.andyanh.cotienaddon.system.ThangTienManager.startAscension(sp);
        });
    }

    // --- Phúc Địa ---

    private static void handleOpenPhucDia(OpenPhucDiaPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            PacketDistributor.sendToPlayer(sp, new SyncCoTienPacket(buildSyncNBT(sp)));
        });
    }

    private static void handleUpdatePermission(UpdatePermissionPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;

            CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
            if (data.thangTienPhase < 4) return; // chỉ Cổ Tiên mới có quyền

            try {
                java.util.UUID memberUUID = java.util.UUID.fromString(pkt.memberUUID());
                if (!data.whitelist.contains(pkt.memberUUID())) return; // phải là member
                data.setPermission(memberUUID, pkt.permBit(), pkt.value());
                sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
            } catch (IllegalArgumentException e) {
                CoTienAddon.LOGGER.warn("[CoTienAddon] Invalid UUID in UpdatePermissionPacket: {}", pkt.memberUUID());
            }
        });
    }

    private static void handleManageMember(ManageMemberPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;

            CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
            if (data.thangTienPhase < 4) return;

            if (pkt.action() == ManageMemberPacket.ACTION_INVITE) {
                // Invite: tìm player theo tên
                ServerPlayer target = sp.server.getPlayerList().getPlayerByName(pkt.target());
                if (target == null || target.getUUID().equals(sp.getUUID())) return;

                CoTienData targetData = target.getData(CoTienAttachments.CO_TIEN_DATA.get());
                if (!data.hasPermission(sp.getUUID(), CoTienData.PERM_MANAGE)
                        && !sp.getUUID().toString().equals(data.phucDiaOwnerUUID)) {
                    return; // chỉ owner hoặc người có PERM_MANAGE mới được mời
                }

                String targetUUID = target.getUUID().toString();
                if (!data.whitelist.contains(targetUUID)) {
                    data.whitelist.add(targetUUID);
                    sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                    target.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                            "gui.cotienaddon.phuc_dia.invited_by", sp.getName()));
                }

            } else if (pkt.action() == ManageMemberPacket.ACTION_KICK) {
                // Kick: theo UUID string
                String memberUUID = pkt.target();
                if (!data.whitelist.contains(memberUUID)) return;

                // Chỉ owner mới kick được
                if (!sp.getUUID().toString().equals(data.phucDiaOwnerUUID)) return;

                data.whitelist.remove(memberUUID);
                data.permissions.remove(memberUUID);
                sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);

                // Kick player ra khỏi Phúc Địa ngay nếu đang online
                try {
                    ServerPlayer kicked = sp.server.getPlayerList().getPlayer(
                            java.util.UUID.fromString(memberUUID));
                    if (kicked != null && kicked.level().dimension().equals(PhucDiaManager.PHUC_DIA_KEY)) {
                        PhucDiaManager.teleportOutOfPhucDia(kicked);
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        });
    }

    private static void handleTeleportPhucDia(TeleportPhucDiaPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            if (pkt.enter()) {
                PhucDiaManager.teleportToOwnPhucDia(sp);
            } else {
                PhucDiaManager.teleportOutOfPhucDia(sp);
            }
        });
    }

    private static void handleDebugAction(DebugActionPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
            switch (pkt.action()) {
                case DebugActionPacket.ASCEND -> {
                    if (data.thangTienPhase != 0) {
                        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§e[Debug] Dang phase " + data.thangTienPhase + ", reset truoc!"));
                        return;
                    }
                    // Pre-fill chỉ khi nk > 0, nếu không để checkNapKhi tự xử lý
                    double nk = data.calcNhanKhi();
                    if (nk > 0) {
                        data.thienKhi = Math.max(data.thienKhi, nk * 0.5 + 1);
                        data.diaKhi   = Math.max(data.diaKhi,   nk * 0.5 + 1);
                    }
                    sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                    com.andyanh.cotienaddon.system.ThangTienManager.startAscension(sp);
                }
                case DebugActionPacket.COMPLETE -> {
                    data.thangTienPhase = 3;
                    sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                    com.andyanh.cotienaddon.system.ThangTienManager.completeAscension(sp);
                }
                case DebugActionPacket.RESET -> {
                    data.thangTienPhase = 0;
                    data.thienKhi = 0;
                    data.diaKhi = 0;
                    sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                    sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§e[Debug] Reset phase=0"));
                }
            }
        });
    }

    // --- Định Tiên Du ---

    private static void handleSaveLocation(SaveLocationPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
            if (pkt.action() == SaveLocationPacket.ACTION_SAVE) {
                if (data.savedLocations.size() >= CoTienData.MAX_SAVED_LOCATIONS) return;
                data.savedLocations.add(new CoTienData.SavedLocation(pkt.name(), pkt.x(), pkt.y(), pkt.z()));
            } else if (pkt.action() == SaveLocationPacket.ACTION_DELETE) {
                int idx = pkt.deleteIndex();
                if (idx >= 0 && idx < data.savedLocations.size()) {
                    data.savedLocations.remove(idx);
                }
            }
            sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
            // Sync back so client sees updated list
            PacketDistributor.sendToPlayer(sp, new SyncCoTienPacket(buildSyncNBT(sp)));
        });
    }

    private static void handleTeleportDinhTienDu(TeleportDinhTienDuPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            DinhTienDuItem.requestTeleport(sp, pkt.x(), pkt.y(), pkt.z());
        });
    }

    // --- Sync packet: delegate to client handler ---

    private static void handleSyncPacket(SyncCoTienPacket pkt, IPayloadContext ctx) {
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            com.andyanh.cotienaddon.client.CoTienClientHandler.handleSyncPacket(pkt, ctx);
        }
    }
}
