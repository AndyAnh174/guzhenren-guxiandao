package com.andyanh.cotienaddon.event;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.init.CoTienAttachments;
import com.andyanh.cotienaddon.util.GuTierDetector;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = CoTienAddon.MODID)
public class CoTienEventHandler {

    // --- Track gu item usage ---
    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        ItemStack stack = event.getItem();
        int tier = GuTierDetector.getTier(stack);
        if (tier == 0) return;

        CoTienData data = player.getData(CoTienAttachments.CO_TIEN_DATA.get());
        switch (tier) {
            case 1 -> data.guUsed_tier1++;
            case 2 -> data.guUsed_tier2++;
            case 3 -> data.guUsed_tier3++;
            case 4 -> data.guUsed_tier4++;
            case 5 -> data.guUsed_tier5++;
        }
        data.nhanKhi = data.calcNhanKhi();
        player.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
    }

    // --- Track gufang pickup (proxy cho craft completion) ---
    @SubscribeEvent
    public static void onItemPickup(net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Post event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;

        ItemStack stack = event.getOriginalStack();
        if (!GuTierDetector.isGufang(stack)) return;

        CoTienData data = player.getData(CoTienAttachments.CO_TIEN_DATA.get());
        data.guCrafted += stack.getCount();
        data.nhanKhi = data.calcNhanKhi();
        player.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
    }

    // --- Tick: hút Thiên Khí & Địa Khí khi đang thăng tiên (phase 2) ---
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (player.tickCount % 20 != 0) return; // mỗi giây 1 lần

        CoTienData data = player.getData(CoTienAttachments.CO_TIEN_DATA.get());
        if (data.thangTienPhase != 2) return; // chỉ hút khi đang phase Nạp Khí

        double thienRate = calcThienKhiRate(player, serverLevel);
        double diaRate = calcDiaKhiRate(player, serverLevel);

        data.thienKhi += thienRate;
        data.diaKhi += diaRate;
        player.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
    }

    private static double calcThienKhiRate(Player player, ServerLevel level) {
        double rate = 0;
        int y = (int) player.getY();

        if (y > 150) rate += (y - 150) * 0.05;         // sống Y cao
        if (level.isThundering()) rate += 5.0;           // bão = hút nhanh, rủi ro cao
        else if (level.isRaining()) rate += 1.0;

        boolean isDay = level.getDayTime() % 24000 < 12000;
        rate += isDay ? 1.0 : 0.5;                      // ban ngày tốt hơn

        return Math.max(rate, 0.1);
    }

    private static double calcDiaKhiRate(Player player, ServerLevel level) {
        double rate = 0;
        int y = (int) player.getY();

        if (y < 0) rate += Math.abs(y) * 0.05;          // càng sâu càng tốt

        // Check block xung quanh (bán kính 3) — trận pháp tăng Địa Khí
        int diamondCount = countNearbyBlocks(player, level,
                net.minecraft.world.level.block.Blocks.DIAMOND_BLOCK,
                net.minecraft.world.level.block.Blocks.GOLD_BLOCK,
                net.minecraft.world.level.block.Blocks.EMERALD_BLOCK);
        rate += diamondCount * 0.3;

        return Math.max(rate, 0.1);
    }

    @SuppressWarnings("deprecation")
    private static int countNearbyBlocks(Player player, ServerLevel level,
            net.minecraft.world.level.block.Block... blocks) {
        int count = 0;
        int px = (int) player.getX(), py = (int) player.getY(), pz = (int) player.getZ();
        var blockSet = java.util.Set.of(blocks);
        for (int dx = -3; dx <= 3; dx++)
            for (int dy = -3; dy <= 3; dy++)
                for (int dz = -3; dz <= 3; dz++) {
                    var pos = new net.minecraft.core.BlockPos(px + dx, py + dy, pz + dz);
                    if (blockSet.contains(level.getBlockState(pos).getBlock())) count++;
                }
        return count;
    }
}
