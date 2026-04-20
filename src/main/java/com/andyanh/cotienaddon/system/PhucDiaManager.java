package com.andyanh.cotienaddon.system;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.init.CoTienAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * Quản lý Phúc Địa: shared dimension với per-player zone (partition approach).
 *
 * Layout: mỗi player chiếm một "slot" theo trục X.
 *   - Slot N: X ∈ [N*SLOT_SIZE, (N+1)*SLOT_SIZE)
 *   - Center: (N*SLOT_SIZE + SLOT_SIZE/2, 5, SLOT_SIZE/2)
 *   - Z cố định: 0..SLOT_SIZE, giúp mọi player có cùng chiều sâu trục Z
 *
 * Grade → bán kính được phép đi:
 *   - Hạ đẳng (1):  128  block
 *   - Trung đẳng(2): 512  block
 *   - Thượng đẳng(3):2048 block
 *   - Siêu đẳng (4): không giới hạn
 */
public class PhucDiaManager {

    public static final ResourceKey<Level> PHUC_DIA_KEY = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "phuc_dia"));

    private static final int SLOT_SIZE = 8192; // block per slot, đủ rộng cho mọi grade

    // --- Helpers ---

    public static BlockPos getSlotCenter(int slot) {
        return new BlockPos(slot * SLOT_SIZE + SLOT_SIZE / 2, 5, SLOT_SIZE / 2);
    }

    public static int getBorderRadius(int grade) {
        return switch (grade) {
            case 1 -> 128;
            case 2 -> 512;
            case 3 -> 2048;
            default -> Integer.MAX_VALUE; // grade 4 = vô hạn
        };
    }

    public static boolean isInZone(int slot, int grade, double x, double z) {
        if (grade >= 4) return true;
        int radius = getBorderRadius(grade);
        double cx = slot * SLOT_SIZE + SLOT_SIZE / 2.0;
        double cz = SLOT_SIZE / 2.0;
        return Math.abs(x - cx) <= radius && Math.abs(z - cz) <= radius;
    }

    /**
     * Tìm owner của zone chứa tọa độ (x, z) trong Phúc Địa dimension.
     * Trả null nếu không có ai sở hữu vị trí đó.
     */
    public static UUID findZoneOwner(MinecraftServer server, double x, double z) {
        int slot = (int) (x / SLOT_SIZE);
        if (slot < 0) return null;
        PhucDiaSavedData savedData = PhucDiaSavedData.get(server);
        return savedData.getOwner(slot);
    }

    // --- Tiên Nguyên production rate (per second) ---

    public static double getTienNguyenRate(int grade) {
        return switch (grade) {
            case 1 -> 0.1;
            case 2 -> 0.5;
            case 3 -> 2.0;
            case 4 -> 10.0;
            default -> 0;
        };
    }

    // --- Assign slot khi thăng tiên thành công ---

    public static void assignSlotOnAscension(ServerPlayer sp) {
        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
        if (data.phucDiaSlot >= 0) return; // đã có slot

        PhucDiaSavedData savedData = PhucDiaSavedData.get(sp.getServer());
        data.phucDiaSlot = savedData.allocateSlot(sp.getUUID());
        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);

        CoTienAddon.LOGGER.info("[CoTienAddon] Assigned Phuc Dia slot {} to {}",
                data.phucDiaSlot, sp.getName().getString());
    }

    // --- Teleport vào Phúc Địa của bản thân ---

    public static void teleportToOwnPhucDia(ServerPlayer sp) {
        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
        if (data.thangTienPhase < 4) {
            sp.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "gui.cotienaddon.phuc_dia.not_ascended"));
            return;
        }

        ServerLevel phucDia = sp.server.getLevel(PHUC_DIA_KEY);
        if (phucDia == null) {
            CoTienAddon.LOGGER.error("[CoTienAddon] Phuc Dia dimension not found!");
            return;
        }

        BlockPos center = getSlotCenter(data.phucDiaSlot);
        sp.teleportTo(phucDia,
                center.getX() + 0.5, center.getY(), center.getZ() + 0.5,
                0f, 0f);
        sp.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                "gui.cotienaddon.phuc_dia.enter"));
    }

    // --- Teleport ra Overworld ---

    public static void teleportOutOfPhucDia(ServerPlayer sp) {
        ServerLevel overworld = sp.server.overworld();
        // Teleport về spawn point thế giới
        BlockPos spawn = overworld.getSharedSpawnPos();
        sp.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
                sp.getYRot(), sp.getXRot());
        sp.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                "gui.cotienaddon.phuc_dia.exit"));
    }

    // --- Teleport khách vào Phúc Địa của ownerUUID ---

    public static void teleportGuestToPhucDia(ServerPlayer guest, UUID ownerUUID, MinecraftServer server) {
        PhucDiaSavedData savedData = PhucDiaSavedData.get(server);
        int slot = savedData.getSlot(ownerUUID);
        if (slot < 0) return;

        ServerLevel phucDia = server.getLevel(PHUC_DIA_KEY);
        if (phucDia == null) return;

        // Lấy grade của owner từ CoTienData
        int ownerGrade = 1;
        for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
            if (sp.getUUID().equals(ownerUUID)) {
                ownerGrade = sp.getData(CoTienAttachments.CO_TIEN_DATA.get()).phucDiaGrade;
                break;
            }
        }

        BlockPos center = getSlotCenter(slot);
        guest.teleportTo(phucDia, center.getX() + 0.5, center.getY(), center.getZ() + 0.5, 0f, 0f);

        // Debuff khách: Mining Fatigue + Weakness
        guest.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 600, 1, false, true));
        guest.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 0, false, true));
    }

    // --- Check nếu player đang đứng ngoài zone của mình → push về ---

    public static void enforceZoneBoundary(ServerPlayer sp) {
        if (!sp.level().dimension().equals(PHUC_DIA_KEY)) return;

        UUID ownerUUID = findZoneOwner(sp.server, sp.getX(), sp.getZ());

        if (ownerUUID == null) {
            teleportOutOfPhucDia(sp);
            return;
        }

        if (ownerUUID.equals(sp.getUUID())) {
            // Chủ nhân → kiểm tra zone boundary
            CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
            if (!isInZone(data.phucDiaSlot, data.phucDiaGrade, sp.getX(), sp.getZ())) {
                BlockPos center = getSlotCenter(data.phucDiaSlot);
                sp.teleportTo(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
            }
            return;
        }

        // Là khách → kiểm tra whitelist của owner
        CoTienData ownerData = getOwnerData(sp.server, ownerUUID);
        if (ownerData == null || !ownerData.whitelist.contains(sp.getUUID().toString())) {
            teleportOutOfPhucDia(sp);
        }
    }

    private static CoTienData getOwnerData(MinecraftServer server, UUID ownerUUID) {
        ServerPlayer owner = server.getPlayerList().getPlayer(ownerUUID);
        if (owner == null) return null;
        return owner.getData(CoTienAttachments.CO_TIEN_DATA.get());
    }
}
