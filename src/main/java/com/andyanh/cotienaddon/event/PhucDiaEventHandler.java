package com.andyanh.cotienaddon.event;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.init.CoTienAttachments;
import com.andyanh.cotienaddon.system.PhucDiaManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.UUID;

@EventBusSubscriber(modid = CoTienAddon.MODID)
public class PhucDiaEventHandler {

    // --- Tiên Nguyên: tick mỗi 20 tick (1s) cho Cổ Tiên ---
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (player.tickCount % 20 != 0) return;

        CoTienData data = player.getData(CoTienAttachments.CO_TIEN_DATA.get());
        if (data.thangTienPhase < 4) return; // chỉ Cổ Tiên mới sinh Tiên Nguyên

        data.tienNguyen += PhucDiaManager.getTienNguyenRate(data.phucDiaGrade);
        player.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
    }

    // --- Zone boundary: tick mỗi 100 tick (5s) ---
    @SubscribeEvent
    public static void onPlayerTickZone(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer sp)) return;
        if (player.tickCount % 100 != 0) return;

        PhucDiaManager.enforceZoneBoundary(sp);
    }

    // --- Block break: kiểm tra PERM_BUILD ---
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().equals(PhucDiaManager.PHUC_DIA_KEY)) return;
        if (player.isCreative()) return;

        if (!checkPermission(player, player.getX(), player.getZ(), CoTienData.PERM_BUILD)) {
            event.setCanceled(true);
        }
    }

    // --- Block place: kiểm tra PERM_BUILD ---
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().equals(PhucDiaManager.PHUC_DIA_KEY)) return;
        if (player.isCreative()) return;

        if (!checkPermission(player, player.getX(), player.getZ(), CoTienData.PERM_BUILD)) {
            event.setCanceled(true);
        }
    }

    // --- Container open: kiểm tra PERM_CONTAINERS ---
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().equals(PhucDiaManager.PHUC_DIA_KEY)) return;

        // Kiểm tra xem block có phải container không
        var state = serverLevel.getBlockState(event.getPos());
        if (state.getMenuProvider(serverLevel, event.getPos()) == null) return;

        if (!checkPermission(player, player.getX(), player.getZ(), CoTienData.PERM_CONTAINERS)) {
            event.setCanceled(true);
        }
    }

    // --- Kiểm tra permission của player tại tọa độ (x, z) trong Phúc Địa ---
    private static boolean checkPermission(Player player, double x, double z, int permBit) {
        if (!(player instanceof ServerPlayer sp)) return true;

        UUID ownerUUID = PhucDiaManager.findZoneOwner(sp.server, x, z);
        if (ownerUUID == null) return false; // zone không có chủ → không ai được làm gì

        if (ownerUUID.equals(sp.getUUID())) return true; // chủ nhân → toàn quyền

        // Khách: kiểm tra whitelist + permission
        ServerPlayer owner = sp.server.getPlayerList().getPlayer(ownerUUID);
        if (owner == null) return false; // owner offline → deny

        CoTienData ownerData = owner.getData(CoTienAttachments.CO_TIEN_DATA.get());
        if (!ownerData.whitelist.contains(sp.getUUID().toString())) return false;

        return ownerData.hasPermission(sp.getUUID(), permBit);
    }
}
