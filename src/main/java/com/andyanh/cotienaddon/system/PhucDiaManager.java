package com.andyanh.cotienaddon.system;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.entity.DiaSinhEntity;
import com.andyanh.cotienaddon.init.CoTienAttachments;
import com.andyanh.cotienaddon.init.CoTienEntities;
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

    // Dimension theo grade (1-4), grade 0 fallback về grade 1
    public static final ResourceKey<Level> PHUC_DIA_KEY = phucDiaKey(1); // default key cho check

    public static ResourceKey<Level> phucDiaKey(int grade) {
        int g = Math.max(1, Math.min(4, grade));
        return ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "phuc_dia_" + g));
    }

    public static boolean isPhucDiaDimension(ResourceKey<Level> dim) {
        String path = dim.location().getPath();
        return dim.location().getNamespace().equals(CoTienAddon.MODID)
                && path.startsWith("phuc_dia");
    }

    private static final int SLOT_SIZE = 8192; // block per slot, đủ rộng cho mọi grade

    // --- Helpers ---

    // Surface của flat terrain: bedrock(1) + stone(50) + dirt(3) + grass(1) = y=55, spawn tại 56
    private static final int SURFACE_Y = 56;

    public static BlockPos getSlotCenter(int slot) {
        return new BlockPos(slot * SLOT_SIZE + SLOT_SIZE / 2, SURFACE_Y, SLOT_SIZE / 2);
    }

    public static BlockPos getSurfacePos(ServerLevel level, int x, int z) {
        return new BlockPos(x, SURFACE_Y, z);
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

    // Tiên Nguyên là tài nguyên cực hiếm — rate tính per-second, rất chậm
    // Có thể nâng cấp qua Địa Linh Thạch (nhân với productionMultiplier)
    public static double getTienNguyenBaseRate(int grade) {
        return switch (grade) {
            case 1 -> 0.01;   // Hạ đẳng: 1 Tiên Nguyên / 100 giây
            case 2 -> 0.02;   // Trung đẳng: 1 / 50 giây
            case 3 -> 0.035;  // Thượng đẳng: 1 / ~28 giây
            case 4 -> 0.05;   // Siêu đẳng: 1 / 20 giây
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

        // Re-sync slot vào SavedData (phòng trường hợp đổi world/LAN mới)
        if (data.phucDiaSlot >= 0) {
            PhucDiaSavedData savedData = PhucDiaSavedData.get(sp.server);
            if (savedData.getOwner(data.phucDiaSlot) == null) {
                savedData.forceRegister(sp.getUUID(), data.phucDiaSlot);
            }
        }

        ResourceKey<Level> dimKey = phucDiaKey(data.phucDiaGrade);
        ServerLevel phucDia = sp.server.getLevel(dimKey);
        if (phucDia == null) {
            CoTienAddon.LOGGER.error("[CoTienAddon] Phuc Dia dimension {} not found!", dimKey.location());
            return;
        }

        BlockPos rawCenter = getSlotCenter(data.phucDiaSlot);
        BlockPos center = getSurfacePos(phucDia, rawCenter.getX(), rawCenter.getZ());
        sp.teleportTo(phucDia,
                center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
                0f, 0f);

        // Lần đầu vào → spawn Địa Linh (dùng NBT flag, không dùng entity scan vì chunk có thể unloaded)
        if (!data.hasDialinh) {
            spawnDialinh(phucDia, center, sp);
            data.hasDialinh = true;
            sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
        }

        // Cập nhật randomTickSpeed theo level
        updateTimeDialation(phucDia, data.phucDiaLevel);

        sp.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                "gui.cotienaddon.phuc_dia.enter"));
    }

    // --- Teleport guest vào Phúc Địa của owner ---
    public static boolean teleportToOwnerPhucDia(ServerPlayer guest, ServerPlayer owner) {
        CoTienData ownerData = owner.getData(CoTienAttachments.CO_TIEN_DATA.get());
        if (ownerData.thangTienPhase < 4) return false;

        // Kiểm tra guest có trong whitelist không
        String guestUUID = guest.getUUID().toString();
        if (!ownerData.whitelist.contains(guestUUID)) {
            guest.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§c✦ Bạn chưa được mời vào Phúc Địa của " + owner.getName().getString() + "!"));
            return false;
        }

        ResourceKey<Level> dimKey = phucDiaKey(ownerData.phucDiaGrade);
        ServerLevel phucDia = guest.server.getLevel(dimKey);
        if (phucDia == null) return false;

        BlockPos rawCenter = getSlotCenter(ownerData.phucDiaSlot);
        BlockPos center = getSurfacePos(phucDia, rawCenter.getX(), rawCenter.getZ());
        // Offset nhỏ để không đứng chồng lên chủ
        guest.teleportTo(phucDia,
                center.getX() + 2.5, center.getY() + 0.5, center.getZ() + 0.5,
                0f, 0f);
        guest.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§a✦ Đã vào Phúc Địa của §f" + owner.getName().getString() + "§a!"));
        owner.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§a✦ §f" + guest.getName().getString() + " §ađã bước vào Phúc Địa của bạn."));
        return true;
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

        // Lấy grade của owner để chọn đúng dimension
        int ownerGrade = 1;
        for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
            if (sp.getUUID().equals(ownerUUID)) {
                ownerGrade = sp.getData(CoTienAttachments.CO_TIEN_DATA.get()).phucDiaGrade;
                break;
            }
        }
        ServerLevel phucDia = server.getLevel(phucDiaKey(ownerGrade));
        if (phucDia == null) return;

        BlockPos center = getSlotCenter(slot);
        guest.teleportTo(phucDia, center.getX() + 0.5, center.getY(), center.getZ() + 0.5, 0f, 0f);

        // Debuff khách: Mining Fatigue + Weakness
        guest.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 600, 1, false, true));
        guest.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 0, false, true));
    }

    // --- Check nếu player đang đứng ngoài zone của mình → push về ---

    public static void enforceZoneBoundary(ServerPlayer sp) {
        if (!isPhucDiaDimension(sp.level().dimension())) return;

        UUID ownerUUID = findZoneOwner(sp.server, sp.getX(), sp.getZ());

        if (ownerUUID == null) {
            teleportOutOfPhucDia(sp);
            return;
        }

        if (ownerUUID.equals(sp.getUUID())) {
            // Chủ nhân → kiểm tra zone boundary
            CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
            if (!isInZone(data.phucDiaSlot, data.phucDiaGrade, sp.getX(), sp.getZ())) {
                double cx = data.phucDiaSlot * SLOT_SIZE + SLOT_SIZE / 2.0;
                double cz = SLOT_SIZE / 2.0;
                int radius = getBorderRadius(data.phucDiaGrade);
                double nx = sp.getX();
                double nz = sp.getZ();
                if (nx > cx + radius) nx = cx + radius - 1.5;
                if (nx < cx - radius) nx = cx - radius + 1.5;
                if (nz > cz + radius) nz = cz + radius - 1.5;
                if (nz < cz - radius) nz = cz - radius + 1.5;
                sp.teleportTo(nx, sp.getY(), nz);
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c§l⚠ Ranh giới Phúc Địa! §7Không gian hiện tại đã đạt cực hạn. Hãy nâng cấp Phúc Địa để mở rộng thêm."));
                // Play sound
                sp.playNotifySound(net.minecraft.sounds.SoundEvents.BEACON_DEACTIVATE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.5f);
            }
            return;
        }

        // Là khách → kiểm tra whitelist của owner
        CoTienData ownerData = getOwnerData(sp.server, ownerUUID);
        if (ownerData == null || !ownerData.whitelist.contains(sp.getUUID().toString())) {
            teleportOutOfPhucDia(sp);
            return;
        }

        // Khách đã trong whitelist → vẫn enforce zone boundary của chủ
        if (!isInZone(ownerData.phucDiaSlot, ownerData.phucDiaGrade, sp.getX(), sp.getZ())) {
            double cx = ownerData.phucDiaSlot * SLOT_SIZE + SLOT_SIZE / 2.0;
            double cz = SLOT_SIZE / 2.0;
            int radius = getBorderRadius(ownerData.phucDiaGrade);
            double nx = sp.getX(), nz = sp.getZ();
            if (nx > cx + radius) nx = cx + radius - 1.5;
            if (nx < cx - radius) nx = cx - radius + 1.5;
            if (nz > cz + radius) nz = cz + radius - 1.5;
            if (nz < cz - radius) nz = cz - radius + 1.5;
            sp.teleportTo(nx, sp.getY(), nz);
            sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§c§l⚠ Ranh giới Phúc Địa của chủ nhân!"));
        }
    }

    private static boolean isDialinhPresent(ServerLevel level, BlockPos center) {
        // Check toàn bộ slot (SLOT_SIZE/2 = 4096 block) vì DiaSinh có AI và có thể đi xa
        return !level.getEntitiesOfClass(DiaSinhEntity.class,
                new net.minecraft.world.phys.AABB(center).inflate(SLOT_SIZE / 2.0)).isEmpty();
    }

    public static void spawnDialinh(ServerLevel level, BlockPos center, ServerPlayer owner) {
        DiaSinhEntity entity = CoTienEntities.DIA_LINH.get().create(level);
        if (entity == null) return;
        // Random skin từ 14 skin có sẵn
        int skinIdx = level.random.nextInt(DiaSinhEntity.SKIN_COUNT);
        entity.setSkinIndex(skinIdx);
        entity.setOwnerUUID(owner.getUUID().toString());
        // Đứng cách center 3 block về phía nam
        BlockPos spawnPos = getSurfacePos(level, center.getX(), center.getZ() + 3);
        entity.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        entity.setYRot(180f);
        CoTienData ownerData = owner.getData(CoTienAttachments.CO_TIEN_DATA.get());
        entity.updateStatsFromOwner(ownerData);
        level.addFreshEntity(entity);
        CoTienAddon.LOGGER.info("[DiaSinh] Spawned Dia Linh (skin={}) for {} at {}",
                skinIdx, owner.getName().getString(), center);
    }

    public static void updateTimeDialation(ServerLevel level, int phucDiaLevel) {
        // randomTickSpeed: level 0=15, level 5=30, level 10=60
        int tickSpeed = 15 + phucDiaLevel * 5;
        level.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_RANDOMTICKING)
                .set(tickSpeed, level.getServer());
    }

    private static CoTienData getOwnerData(MinecraftServer server, UUID ownerUUID) {
        ServerPlayer owner = server.getPlayerList().getPlayer(ownerUUID);
        if (owner == null) return null;
        return owner.getData(CoTienAttachments.CO_TIEN_DATA.get());
    }
}
