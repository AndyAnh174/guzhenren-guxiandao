package com.andyanh.cotienaddon.init;

import com.andyanh.cotienaddon.CoTienAddon;
import java.util.UUID;
import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.item.DinhTienDuItem;
import com.andyanh.cotienaddon.init.CoTienItems;
import com.andyanh.cotienaddon.network.*;
import net.minecraft.world.item.ItemStack;
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
        reg.playToServer(
                WithdrawTienNguyenPacket.TYPE,
                WithdrawTienNguyenPacket.STREAM_CODEC,
                CoTienNetwork::handleWithdrawTienNguyen
        );
        reg.playToServer(
                UpgradePhucDiaPacket.TYPE,
                UpgradePhucDiaPacket.STREAM_CODEC,
                CoTienNetwork::handleUpgradePhucDia
        );
        reg.playToServer(
                CallDialinhPacket.TYPE,
                CallDialinhPacket.STREAM_CODEC,
                CoTienNetwork::handleCallDialinh
        );
        reg.playToServer(
                EcosystemPacket.TYPE,
                EcosystemPacket.STREAM_CODEC,
                CoTienNetwork::handleEcosystem
        );
        reg.playToClient(
                com.andyanh.cotienaddon.network.OpenThachNhanScreenPacket.TYPE,
                com.andyanh.cotienaddon.network.OpenThachNhanScreenPacket.STREAM_CODEC,
                CoTienNetwork::handleOpenThachNhanScreen
        );
        reg.playToServer(
                com.andyanh.cotienaddon.network.ThachNhanActionPacket.TYPE,
                com.andyanh.cotienaddon.network.ThachNhanActionPacket.STREAM_CODEC,
                CoTienNetwork::handleThachNhanAction
        );
        reg.playToServer(
                com.andyanh.cotienaddon.network.AnnexPhucDiaPacket.TYPE,
                com.andyanh.cotienaddon.network.AnnexPhucDiaPacket.STREAM_CODEC,
                CoTienNetwork::handleAnnexPhucDia
        );
        reg.playToServer(
                com.andyanh.cotienaddon.network.SetTonHieuPacket.TYPE,
                com.andyanh.cotienaddon.network.SetTonHieuPacket.STREAM_CODEC,
                CoTienNetwork::handleSetTonHieu
        );
        reg.playToServer(
                com.andyanh.cotienaddon.network.SectNetwork.OpenSectPacket.TYPE,
                com.andyanh.cotienaddon.network.SectNetwork.OpenSectPacket.STREAM_CODEC,
                com.andyanh.cotienaddon.network.SectNetwork.OpenSectPacket::handle
        );
        reg.playToClient(
                com.andyanh.cotienaddon.network.SectNetwork.SyncSectPacket.TYPE,
                com.andyanh.cotienaddon.network.SectNetwork.SyncSectPacket.STREAM_CODEC,
                CoTienNetwork::handleSyncSectPacket
        );
        reg.playToServer(
                com.andyanh.cotienaddon.network.SectNetwork.SectActionPacket.TYPE,
                com.andyanh.cotienaddon.network.SectNetwork.SectActionPacket.STREAM_CODEC,
                com.andyanh.cotienaddon.network.SectNetwork.SectActionPacket::handle
        );
    }

    private static void handleSyncSectPacket(com.andyanh.cotienaddon.network.SectNetwork.SyncSectPacket pkt, IPayloadContext ctx) {
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            com.andyanh.cotienaddon.client.CoTienClientHandler.handleSyncSectPacket(pkt, ctx);
        }
    }

    // --- Không Khiếu ---

    public static net.minecraft.nbt.CompoundTag buildSyncNBT(ServerPlayer sp) {
        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
        net.minecraft.nbt.CompoundTag tag = data.serializeNBT();
        // Inject guzhenren vars so client can show conditions
        var gv = sp.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
        tag.putDouble("zhuanshu", gv.zhuanshu);
        tag.putDouble("jieduan", gv.jieduan);
        tag.putDouble("daode", gv.daode);
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

            // Kiểm tra ít nhất 2 lưu phái đạo đạt Đại Tông Sư (> 50,000 điểm)
            var daoCheck = com.andyanh.cotienaddon.system.ThangTienManager.checkDaoNganCondition(guzhenrenVars);
            if (daoCheck.count() < 2) {
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§c[Tiên Khiếu] Cần ít nhất §e2 §clưu phái đạo đạt §6Chuẩn Vô thượng Đại Tông Sư §c(>100,000 điểm)!"));
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§7  Hiện tại: §e" + daoCheck.count() + "/2 §7| Cao nhất: §f" + daoCheck.topName() + " §7(" + (int)daoCheck.topValue() + " điểm)"));
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
                    data.memberNames.put(targetUUID, target.getName().getString());
                    sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);

                    // Clickable invite message
                    String ownerName = sp.getName().getString();
                    var inviteMsg = net.minecraft.network.chat.Component.literal(
                            "§a✦ §f" + ownerName + " §amời bạn vào Phúc Địa! ")
                        .append(net.minecraft.network.chat.Component.literal("§e§l[Chấp nhận]")
                            .withStyle(s -> s
                                .withClickEvent(new net.minecraft.network.chat.ClickEvent(
                                    net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND,
                                    "/cotien acceptinvite " + ownerName))
                                .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                                    net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                                    net.minecraft.network.chat.Component.literal("Click để vào Phúc Địa của " + ownerName)))));
                    target.sendSystemMessage(inviteMsg);
                    sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "§a✦ Đã mời §f" + target.getName().getString() + " §avào Phúc Địa!"));
                } else {
                    sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "§e✦ " + target.getName().getString() + " đã có trong danh sách khách."));
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

                // Kick player — xuất hiện tại vị trí đang đứng của chủ nhân
                try {
                    ServerPlayer kicked = sp.server.getPlayerList().getPlayer(
                            java.util.UUID.fromString(memberUUID));
                    if (kicked != null && PhucDiaManager.isPhucDiaDimension(kicked.level().dimension())) {
                        // Teleport guest tới vị trí hiện tại của owner
                        if (sp.level() instanceof net.minecraft.server.level.ServerLevel ownerLevel
                                && !PhucDiaManager.isPhucDiaDimension(ownerLevel.dimension())) {
                            // Owner đang ở thế giới thực → guest về đó
                            kicked.teleportTo(ownerLevel, sp.getX(), sp.getY(), sp.getZ(),
                                    kicked.getYRot(), kicked.getXRot());
                            kicked.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    "§c✦ Bạn bị đưa ra khỏi Phúc Địa bởi " + sp.getName().getString() + "!"));
                            sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    "§a✦ Đã đưa §f" + kicked.getName().getString() + " §ara ngoài!"));
                        } else {
                            // Owner đang trong Phúc Địa → về overworld spawn
                            PhucDiaManager.teleportOutOfPhucDia(kicked);
                        }
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        });
    }

    private static void handleTeleportPhucDia(TeleportPhucDiaPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            if (sp.getPersistentData().contains("tran_vu_sealed")) {
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§9☯ Băng Phong Trận: không thể vào/ra Phúc Địa khi bị phong ấn!"));
                return;
            }
            if (pkt.enter()) {
                PhucDiaManager.teleportToOwnPhucDia(sp);
            } else {
                // Không cho thoát khi đang có Kiếp/Tai
                var pd = sp.getPersistentData();
                if (pd.contains("kiep_ticks") || pd.contains("ditai_ticks")) {
                    sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§c⚔ Không thể rời Phúc Địa trong khi Thiên Kiếp/Địa Tai đang diễn ra!"));
                    return;
                }
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
                String dimId = pkt.dimensionId().isEmpty()
                        ? sp.level().dimension().location().toString()
                        : pkt.dimensionId();
                data.savedLocations.add(new CoTienData.SavedLocation(pkt.name(), pkt.x(), pkt.y(), pkt.z(), dimId));
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
            if (sp.getPersistentData().contains("tran_vu_sealed")) {
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§c[Định Tiên Du] Không gian bị Trấn Vũ phong ấn — không thể dịch chuyển!"));
                return;
            }
            DinhTienDuItem.requestTeleport(sp, pkt.x(), pkt.y(), pkt.z(), pkt.dimensionId());
        });
    }

    private static void handleUpgradePhucDia(UpgradePhucDiaPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());

            // Gate: phải hoàn thành Bond Quest mới được nâng cấp Phúc Địa
            int uType = pkt.upgradeType();
            if (uType >= 0 && uType <= 7) {
                if (!data.dialinhBondComplete) {
                    sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§c[Địa Linh] Phải hoàn thành Nhiệm Vụ Thân Thiết trước khi nâng cấp!"));
                    sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§7  ↳ Shift+Click vào Địa Linh để nhận nhiệm vụ."));
                    return;
                }
            }
            // Gate: skill Địa Linh cũng cần bond
            if (uType == 10 || uType == 11) {
                if (!data.dialinhBondComplete) {
                    sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§c[Địa Linh] Phải hoàn thành Nhiệm Vụ Thân Thiết trước!"));
                    return;
                }
            }

            record UpgradeDef(double cost, Runnable apply, String msg) {}
            UpgradeDef def = switch (pkt.upgradeType()) {
                case UpgradePhucDiaPacket.TYPE_LEVEL -> new UpgradeDef(
                        data.getPhucDiaLevelUpCost(),
                        () -> {
                            data.phucDiaLevel++;
                            data.phucDiaXP += data.getPhucDiaLevelUpCost();
                            var pd = sp.server.getLevel(com.andyanh.cotienaddon.system.PhucDiaManager.PHUC_DIA_KEY);
                            if (pd != null) com.andyanh.cotienaddon.system.PhucDiaManager.updateTimeDialation(pd, data.phucDiaLevel);
                        },
                        "§a✦ Phúc Địa Cấp " + (data.phucDiaLevel + 1) + " — bán kính mở rộng!");
                case UpgradePhucDiaPacket.TYPE_PRODUCTION -> new UpgradeDef(
                        data.getProductionUpgradeCost(),
                        () -> data.productionLevel++,
                        "§b✦ Năng suất Tiên Nguyên Cấp " + (data.productionLevel + 1)
                        + " (×" + String.format("%.1f", data.getProductionMultiplier() + 0.5) + ")!");
                case UpgradePhucDiaPacket.TYPE_TIME -> new UpgradeDef(
                        data.getTimeUpgradeCost(),
                        () -> {
                            data.timeLevel++;
                            var pd = sp.server.getLevel(com.andyanh.cotienaddon.system.PhucDiaManager.PHUC_DIA_KEY);
                            if (pd != null) com.andyanh.cotienaddon.system.PhucDiaManager.updateTimeDialation(pd, data.phucDiaLevel);
                        },
                        "§e✦ Tốc độ Thời gian Cấp " + (data.timeLevel + 1) + " — vạn vật sinh trưởng nhanh hơn!");
                case UpgradePhucDiaPacket.TYPE_DEFENSE -> new UpgradeDef(
                        data.getDefenseUpgradeCost(),
                        () -> data.defenseLevel++,
                        "§6✦ Phòng Hộ Cấp " + (data.defenseLevel + 1) + " — Thiên Kiếp suy yếu!");
                case UpgradePhucDiaPacket.TYPE_LINGMAI -> new UpgradeDef(
                        data.getLingmaiUpgradeCost(),
                        () -> data.lingmaiLevel++,
                        "§d✦ Linh Mạch Cấp " + (data.lingmaiLevel + 1) + " — Khí vận dồi dào!");
                case UpgradePhucDiaPacket.TYPE_THACH_NHAN_SLOT -> new UpgradeDef(
                        data.getThachnhanSlotCost(),
                        () -> data.thachnhanSlots++,
                        "§8⚒ Mở Slot Thạch Nhân #" + (data.thachnhanSlots + 1) + "!");
                case UpgradePhucDiaPacket.TYPE_DIALINHSTORAGE -> {
                    if (data.dialinhStorageLevel >= 3) yield null;
                    int newStoreLvl = data.dialinhStorageLevel + 1;
                    yield new UpgradeDef(data.getDialinhStorageCost(), () -> {
                        data.dialinhStorageLevel = newStoreLvl;
                        // Cập nhật DiaSinhEntity trong dimension ngay lập tức
                        if (sp.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                            var phucDiaLevel = sp.server.getLevel(com.andyanh.cotienaddon.system.PhucDiaManager.PHUC_DIA_KEY);
                            if (phucDiaLevel != null) {
                                phucDiaLevel.getEntitiesOfClass(
                                    com.andyanh.cotienaddon.entity.DiaSinhEntity.class,
                                    new net.minecraft.world.phys.AABB(-8192,-512,-8192,8192,512,8192),
                                    e -> sp.getUUID().toString().equals(e.getOwnerUUID())
                                ).forEach(e -> e.setStorageLevel(newStoreLvl));
                            }
                        }
                    }, "§2☯ Kho Địa Linh Cấp " + newStoreLvl + " — " + com.andyanh.cotienaddon.entity.DiaSinhEntity.getSlotsForLevel(newStoreLvl) + " slot!");
                }
                case UpgradePhucDiaPacket.TYPE_BUY_THACH_NHAN -> {
                    // Mua Thạch Nhân mới ngay tại vị trí player
                    if (!(sp.level() instanceof net.minecraft.server.level.ServerLevel sl)) yield null;
                    var existing = sl.getEntitiesOfClass(com.andyanh.cotienaddon.entity.ThachNhanEntity.class,
                            sp.getBoundingBox().inflate(8192), e -> sp.getUUID().toString().equals(e.getOwnerUUID()));
                    if (existing.size() >= data.thachnhanSlots) {
                        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "§c[Thạch Nhân] Đã đạt giới hạn slot! Mua thêm slot trước."));
                        yield null;
                    }
                    yield new UpgradeDef(80.0, () -> {
                        var tn = com.andyanh.cotienaddon.init.CoTienEntities.THACH_NHAN.get().create(sl);
                        if (tn != null) {
                            tn.setOwnerUUID(sp.getUUID().toString());
                            tn.setPos(sp.getX() + 1.5, sp.getY(), sp.getZ() + 1.5);
                            tn.applyStats();
                            tn.getPersistentData().putBoolean("cotien_spawned", true);
                            tn.setCustomName(net.minecraft.network.chat.Component.literal("§8⚒ Thạch Nhân"));
                            tn.setCustomNameVisible(true);
                            sl.addFreshEntity(tn);
                        }
                    }, "§8⚒ Thạch Nhân được tạo! (-80 TN)");
                }

                case UpgradePhucDiaPacket.TYPE_CALL_THACH_NHAN -> {
                    if (!(sp.level() instanceof net.minecraft.server.level.ServerLevel sl)) yield null;
                    if (!com.andyanh.cotienaddon.system.PhucDiaManager.isPhucDiaDimension(sl.dimension())) {
                        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[Thạch Nhân] Bạn phải đứng trong Phúc Địa!"));
                        yield null;
                    }
                    var tns = sl.getEntitiesOfClass(com.andyanh.cotienaddon.entity.ThachNhanEntity.class,
                            new net.minecraft.world.phys.AABB(-8192,-512,-8192,8192,512,8192),
                            e -> sp.getUUID().toString().equals(e.getOwnerUUID()));
                    if (tns.isEmpty()) {
                        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[Thạch Nhân] Không tìm thấy Thạch Nhân nào!"));
                        yield null;
                    }
                    yield new UpgradeDef(0.0, () -> {
                        for (var tn : tns) {
                            tn.teleportTo(sp.getX() + 1.5, sp.getY(), sp.getZ() + 1.5);
                        }
                    }, "§d☯ Đã triệu hồi " + tns.size() + " Thạch Nhân về vị trí của bạn!");
                }
                case UpgradePhucDiaPacket.TYPE_DIALINH_SKILL_DMG -> {
                    if (data.dialinhSkillDamage >= 10) yield null;
                    int nextLvl = data.dialinhSkillDamage + 1;
                    yield new UpgradeDef(data.getDialinhSkillCost(data.dialinhSkillDamage), () -> {
                        data.dialinhSkillDamage = nextLvl;
                        // Sync stats cho Địa Linh entity
                        syncDialinhStats(sp, data);
                    }, "§c⚔ Sức Mạnh Địa Linh Cấp " + nextLvl + " — DMG +" + (nextLvl * 20) + "!");
                }
                case UpgradePhucDiaPacket.TYPE_DIALINH_SKILL_HP -> {
                    if (data.dialinhSkillHp >= 10) yield null;
                    int nextLvl = data.dialinhSkillHp + 1;
                    yield new UpgradeDef(data.getDialinhSkillCost(data.dialinhSkillHp), () -> {
                        data.dialinhSkillHp = nextLvl;
                        syncDialinhStats(sp, data);
                    }, "§a❤ Sinh Lực Địa Linh Cấp " + nextLvl + " — HP +" + (nextLvl * 50000) + "!");
                }
                case UpgradePhucDiaPacket.TYPE_TP_THACH_NHAN -> {
                    if (!(sp.level() instanceof net.minecraft.server.level.ServerLevel sl)) yield null;
                    var tns = sp.server.getLevel(com.andyanh.cotienaddon.system.PhucDiaManager.PHUC_DIA_KEY)
                            .getEntitiesOfClass(com.andyanh.cotienaddon.entity.ThachNhanEntity.class,
                            new net.minecraft.world.phys.AABB(-8192,-512,-8192,8192,512,8192),
                            e -> sp.getUUID().toString().equals(e.getOwnerUUID()));
                    if (tns.isEmpty()) {
                        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[Thạch Nhân] Không tìm thấy Thạch Nhân nào trong Phúc Địa!"));
                        yield null;
                    }
                    var tn = tns.get(0);
                    yield new UpgradeDef(0.0, () -> {
                        if (sl.dimension() != tn.level().dimension()) {
                            sp.teleportTo((net.minecraft.server.level.ServerLevel)tn.level(), tn.getX(), tn.getY(), tn.getZ(), sp.getYRot(), sp.getXRot());
                        } else {
                            sp.teleportTo(tn.getX(), tn.getY(), tn.getZ());
                        }
                    }, "§b→ Đã dịch chuyển đến chỗ Thạch Nhân!");
                }
                default -> null;
            };

            if (def == null) return;
            if (def.cost() == Double.MAX_VALUE || data.tienNguyen < def.cost()) {
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§c[Phúc Địa] Không đủ Tiên Nguyên! Cần " + (int)def.cost()));
                return;
            }
            data.tienNguyen -= def.cost();
            def.apply().run();
            sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(def.msg()));
            sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
            PacketDistributor.sendToPlayer(sp, new SyncCoTienPacket(buildSyncNBT(sp)));
        });
    }

    private static void syncDialinhStats(ServerPlayer sp, CoTienData data) {
        for (var key : new net.minecraft.resources.ResourceKey[]{
                com.andyanh.cotienaddon.system.PhucDiaManager.phucDiaKey(1),
                com.andyanh.cotienaddon.system.PhucDiaManager.phucDiaKey(2),
                com.andyanh.cotienaddon.system.PhucDiaManager.phucDiaKey(3),
                com.andyanh.cotienaddon.system.PhucDiaManager.phucDiaKey(4)}) {
            @SuppressWarnings("unchecked")
            var lvl = sp.server.getLevel((net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>) key);
            if (lvl == null) continue;
            lvl.getEntitiesOfClass(com.andyanh.cotienaddon.entity.DiaSinhEntity.class,
                    new net.minecraft.world.phys.AABB(-8192, -512, -8192, 8192, 512, 8192),
                    e -> sp.getUUID().toString().equals(e.getOwnerUUID()))
                .forEach(e -> e.updateStatsFromOwner(data));
        }
    }

    private static void handleWithdrawTienNguyen(WithdrawTienNguyenPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
            int amount = Math.min(pkt.amount(), (int) data.tienNguyen);
            if (amount <= 0) return;
            data.tienNguyen -= amount;
            sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
            // Cho item vào inventory
            ItemStack stack = new ItemStack(CoTienItems.TIEN_NGUYEN.get(), amount);
            if (!sp.getInventory().add(stack)) {
                sp.drop(stack, false);
            }
            PacketDistributor.sendToPlayer(sp, new SyncCoTienPacket(buildSyncNBT(sp)));
        });
    }

    // --- Hệ sinh thái ---

    private static void handleEcosystem(EcosystemPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
            if (data.thangTienPhase < 4) return;
            switch (pkt.setting()) {
                case EcosystemPacket.FIXED_DAY      -> data.ecoFixedDay      = pkt.value();
                case EcosystemPacket.ALLOW_RAIN     -> data.ecoAllowRain     = pkt.value();
                case EcosystemPacket.PEACEFUL_MOBS  -> data.ecoPeacefulMobs  = pkt.value();
                case EcosystemPacket.GUZHENREN_MOBS -> data.ecoGuzhenrenMobs = pkt.value();
            }
            sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
            PacketDistributor.sendToPlayer(sp, new SyncCoTienPacket(buildSyncNBT(sp)));
        });
    }

    // --- Địa Linh call ---

    private static void handleCallDialinh(CallDialinhPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            if (!(sp.level() instanceof net.minecraft.server.level.ServerLevel sl)) return;
            if (!PhucDiaManager.isPhucDiaDimension(sl.dimension())) {
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[Địa Linh] Phải đứng trong Phúc Địa!"));
                return;
            }
            // Chỉ chủ nhân mới được gọi Địa Linh
            UUID ownerUUID = PhucDiaManager.findZoneOwner(sp.server, sp.getX(), sp.getZ());
            if (ownerUUID == null || !ownerUUID.equals(sp.getUUID())) {
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[Địa Linh] Chỉ chủ nhân Phúc Địa mới được triệu hoàn Địa Linh!"));
                return;
            }
            // Tìm Địa Linh trong toàn dimension slot
            var aabb = new net.minecraft.world.phys.AABB(sp.getX(), sp.getY(), sp.getZ(), sp.getX(), sp.getY(), sp.getZ()).inflate(8192);
            var list = sl.getEntitiesOfClass(com.andyanh.cotienaddon.entity.DiaSinhEntity.class, aabb);
            if (list.isEmpty()) {
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[Địa Linh] Không tìm thấy Địa Linh!"));
                return;
            }
            var dialinhEntity = list.get(0);
            if (pkt.summonToMe()) {
                dialinhEntity.teleportTo(sp.getX() + 1.5, sp.getY(), sp.getZ());
                dialinhEntity.setYRot(sp.getYRot() + 180f);
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a[Địa Linh] Đã triệu hoàn!"));
            } else {
                sp.teleportTo(dialinhEntity.getX(), dialinhEntity.getY(), dialinhEntity.getZ());
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a[Địa Linh] Đã dịch chuyển đến Địa Linh!"));
            }
        });
    }

    private static void handleAnnexPhucDia(com.andyanh.cotienaddon.network.AnnexPhucDiaPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            if (!(sp.level() instanceof net.minecraft.server.level.ServerLevel sl)) return;

            CoTienData annexer = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
            if (annexer.thangTienPhase < 4) return;

            net.minecraft.nbt.CompoundTag node = pkt.nodeData();
            if (node.isEmpty()) return;

            double victimNK   = node.getDouble("nhanKhi");
            int    victimGrade = node.getInt("phucDiaGrade");
            int    victimSlot  = node.getInt("phucDiaSlot");
            String victimName  = node.getString("victimName");
            String victimUUID  = node.getString("victimUUID");
            double transferTN  = node.getDouble("tienNguyen");
            int    daoCost     = victimGrade * 5;

            // Kiểm tra điều kiện: Nhân Khí >= nạn nhân + đủ Đạo Ngân
            double annexerNK = annexer.calcNhanKhi();
            if (annexerNK < victimNK) {
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§c[Annex] Nhân Khí không đủ! Cần ≥ §f" + (int)victimNK + " §c(bạn có §f" + (int)annexerNK + "§c)"));
                return;
            }
            // Kiểm tra Đạo Ngân = liupai_tiandao của annexer
            var annexerGv = sp.getData(net.guzhenren.network.GuzhenrenModVariables.PLAYER_VARIABLES);
            double daoNganRequired = daoCost * 100.0; // mỗi "đạo ngân" = 100 điểm tiandao
            if (annexerGv.liupai_tiandao < daoNganRequired) {
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§c[Annex] Cần §f" + (int)daoNganRequired + " §cđiểm Thiên đạo Ngân! (bạn có §f"
                    + (int)annexerGv.liupai_tiandao + "§c)"));
                return;
            }

            // Trừ Đạo Ngân (tiandao)
            annexerGv.liupai_tiandao -= daoNganRequired;
            annexerGv.markSyncDirty();
            sp.setData(net.guzhenren.network.GuzhenrenModVariables.PLAYER_VARIABLES, annexerGv);

            // Xóa item khỏi tay
            sp.getMainHandItem().shrink(1);

            // Merge data: tăng Khí tối đa + level + TN
            annexer.tienNguyen += transferTN;
            annexer.phucDiaLevel = Math.min(10, annexer.phucDiaLevel + Math.max(1, victimGrade));
            // Bonus thienKhi/diaKhi cap (lưu qua nhanKhi)
            annexer.nhanKhi += victimNK * 0.3;

            // Xóa data nạn nhân nếu họ đang online
            ServerPlayer victim = sl.getServer().getPlayerList().getPlayer(
                    tryParseUUID(victimUUID));
            if (victim != null) {
                CoTienData victimData = victim.getData(CoTienAttachments.CO_TIEN_DATA.get());
                victimData.thangTienPhase = 0;
                victimData.phucDiaSlot    = -1;
                victimData.phucDiaGrade   = 0;
                victimData.tienNguyen     = 0;
                victimData.hasDialinh     = false;
                victim.setData(CoTienAttachments.CO_TIEN_DATA.get(), victimData);
                victim.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§4§l✦ Phúc Địa của bạn đã bị " + sp.getName().getString() + " nuốt mất! Tu vi tan vỡ!"));
            }

            // Giải phóng slot nạn nhân trong PhucDiaSavedData (if possible)
            sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), annexer);
            PacketDistributor.sendToPlayer(sp, new SyncCoTienPacket(buildSyncNBT(sp)));

            sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§6§l✦ ANNEX THÀNH CÔNG! §r§6Phúc Địa của §f" + victimName
                + " §6đã thuộc về bạn! §aTiên Nguyên +§f" + (int)transferTN
                + " §a| Phúc Địa Cấp +§f" + Math.max(1, victimGrade)));
            sl.playSound(null, sp.blockPosition(),
                    net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1f, 0.8f);

            CoTienAddon.LOGGER.info("[Phase5] {} annexed {}'s PhucDia (slot={})",
                    sp.getName().getString(), victimName, victimSlot);
        });
    }

    private static java.util.UUID tryParseUUID(String s) {
        try { return java.util.UUID.fromString(s); } catch (Exception e) { return new java.util.UUID(0,0); }
    }

    private static void handleSetTonHieu(com.andyanh.cotienaddon.network.SetTonHieuPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
            var gv = sp.getData(net.guzhenren.network.GuzhenrenModVariables.PLAYER_VARIABLES);
            // Yêu cầu: Bát chuyển đỉnh phong (zhuanshu>=8, jieduan>=4) hoặc Cửu chuyển (zhuanshu>=9)
            boolean eligible = (gv.zhuanshu >= 9) || (gv.zhuanshu >= 8 && gv.jieduan >= 4);
            if (!eligible) return;
            // Strip dấu ngoặc đơn/kép thừa (user gõ "Tên" từ command)
            String cleanName = pkt.name().replaceAll("^[\"']+|[\"']+$", "").trim();
            data.tonHieuName    = cleanName.substring(0, Math.min(cleanName.length(), 20));
            data.tonHieuColor   = pkt.color() & 0xFFFFFF;
            data.tonHieuEnabled = pkt.enabled() && !data.tonHieuName.isEmpty();
            sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
            if (data.tonHieuEnabled) {
                com.andyanh.cotienaddon.event.PhucDiaEventHandler.applyTonHieuNameplate(sp, data);
            } else {
                sp.setCustomName(net.minecraft.network.chat.Component.literal("§b[Tiên Cổ] §7" + sp.getName().getString()));
            }
            PacketDistributor.sendToPlayer(sp, new SyncCoTienPacket(buildSyncNBT(sp)));
        });
    }

    // --- Sync packet: delegate to client handler ---

    private static void handleSyncPacket(SyncCoTienPacket pkt, IPayloadContext ctx) {
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            com.andyanh.cotienaddon.client.CoTienClientHandler.handleSyncPacket(pkt, ctx);
        }
    }

    private static void handleOpenThachNhanScreen(
            com.andyanh.cotienaddon.network.OpenThachNhanScreenPacket pkt, IPayloadContext ctx) {
        // Delegate to client-only handler — avoids Screen in CoTienNetwork's constant pool
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            com.andyanh.cotienaddon.client.CoTienClientHandler.handleOpenThachNhanScreen(pkt, ctx);
        }
    }

    private static void handleThachNhanAction(
            com.andyanh.cotienaddon.network.ThachNhanActionPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            if (!(sp.level() instanceof net.minecraft.server.level.ServerLevel sl)) return;

            var entity = sl.getEntity(pkt.entityId());
            if (!(entity instanceof com.andyanh.cotienaddon.entity.ThachNhanEntity tn)) return;

            // Kiểm tra owner
            if (!sp.getUUID().toString().equals(tn.getOwnerUUID())) {
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[Thạch Nhân] Không phải của bạn!"));
                return;
            }

            CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());

            switch (pkt.action()) {
                case com.andyanh.cotienaddon.network.ThachNhanActionPacket.ACTION_HP -> {
                    int cost = com.andyanh.cotienaddon.entity.ThachNhanEntity.getUpgradeCost(tn.getHpLevel());
                    if (data.tienNguyen >= cost && tn.upgradeHp(data.tienNguyen)) {
                        data.tienNguyen -= cost;
                        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a⚒ Nâng HP thành công! (-" + cost + " TN)"));
                    } else sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cKhông đủ Tiên Nguyên hoặc đã MAX!"));
                }
                case com.andyanh.cotienaddon.network.ThachNhanActionPacket.ACTION_ATK -> {
                    int cost = com.andyanh.cotienaddon.entity.ThachNhanEntity.getUpgradeCost(tn.getAtkLevel());
                    if (data.tienNguyen >= cost && tn.upgradeAtk(data.tienNguyen)) {
                        data.tienNguyen -= cost;
                        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a⚒ Nâng Công thành công! (-" + cost + " TN)"));
                    } else sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cKhông đủ Tiên Nguyên hoặc đã MAX!"));
                }
                case com.andyanh.cotienaddon.network.ThachNhanActionPacket.ACTION_SPD -> {
                    int cost = com.andyanh.cotienaddon.entity.ThachNhanEntity.getUpgradeCost(tn.getSpdLevel());
                    if (data.tienNguyen >= cost && tn.upgradeSpd(data.tienNguyen)) {
                        data.tienNguyen -= cost;
                        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a⚒ Nâng Tốc Đào thành công! (-" + cost + " TN)"));
                    } else sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cKhông đủ Tiên Nguyên hoặc đã MAX!"));
                }
                case com.andyanh.cotienaddon.network.ThachNhanActionPacket.ACTION_DISMISS -> {
                    // Trả đồ về cho player trước khi xóa
                    for (int i = 0; i < tn.getInventory().getContainerSize(); i++) {
                        if (!tn.getInventory().getItem(i).isEmpty()) {
                            sp.getInventory().add(tn.getInventory().getItem(i));
                        }
                    }
                    tn.discard();
                    sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7[Thạch Nhân] Đã giải tán. Đồ trong kho trả về túi."));
                }
            }
        });
    }
}
