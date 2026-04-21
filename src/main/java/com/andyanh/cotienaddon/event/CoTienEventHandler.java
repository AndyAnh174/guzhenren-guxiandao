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
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = CoTienAddon.MODID)
public class CoTienEventHandler {

    // Snapshot of gu counts per tier in inventory, keyed by player UUID.
    // Updated every second to detect gu consumption regardless of how the mod consumes them.
    private static final Map<UUID, int[]> guSnapshot = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        // Scan inventory every second to detect gu consumption
        if (player.tickCount % 20 == 0) {
            scanGuInventory(player);
            absorbKhi(player, serverLevel);
        }
    }

    private static void scanGuInventory(Player player) {
        UUID uuid = player.getUUID();
        // current[tier-1] = total count of that tier gu in inventory (tiers 1-5)
        int[] current = new int[5];
        for (ItemStack stack : player.getInventory().items) {
            int tier = GuTierDetector.getTier(stack);
            if (tier >= 1 && tier <= 5) {
                current[tier - 1] += stack.getCount();
            }
        }

        int[] prev = guSnapshot.get(uuid);
        if (prev != null) {
            boolean changed = false;
            CoTienData data = player.getData(CoTienAttachments.CO_TIEN_DATA.get());
            for (int i = 0; i < 5; i++) {
                int consumed = prev[i] - current[i];
                if (consumed > 0) {
                    switch (i) {
                        case 0 -> data.guUsed_tier1 += consumed;
                        case 1 -> data.guUsed_tier2 += consumed;
                        case 2 -> data.guUsed_tier3 += consumed;
                        case 3 -> data.guUsed_tier4 += consumed;
                        case 4 -> data.guUsed_tier5 += consumed;
                    }
                    changed = true;
                    CoTienAddon.LOGGER.info("[CoTienAddon] {} consumed {} tier-{} gu",
                            player.getName().getString(), consumed, i + 1);
                }
            }
            if (changed) {
                data.nhanKhi = data.calcNhanKhi();
                player.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
            }
        }

        guSnapshot.put(uuid, current);
    }

    private static void absorbKhi(Player player, ServerLevel level) {
        CoTienData data = player.getData(CoTienAttachments.CO_TIEN_DATA.get());
        if (data.thangTienPhase != 2) return;

        double thienRate = calcThienKhiRate(player, level);
        double diaRate = calcDiaKhiRate(player, level);

        data.thienKhi += thienRate;
        data.diaKhi += diaRate;
        player.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        guSnapshot.remove(event.getEntity().getUUID());
    }

    private static double calcThienKhiRate(Player player, ServerLevel level) {
        double rate = 0;
        int y = (int) player.getY();
        if (y > 150) rate += (y - 150) * 0.05;
        if (level.isThundering()) rate += 5.0;
        else if (level.isRaining()) rate += 1.0;
        boolean isDay = level.getDayTime() % 24000 < 12000;
        rate += isDay ? 1.0 : 0.5;
        return Math.max(rate, 0.1);
    }

    private static double calcDiaKhiRate(Player player, ServerLevel level) {
        double rate = 0;
        int y = (int) player.getY();
        if (y < 0) rate += Math.abs(y) * 0.05;
        int gemCount = countNearbyBlocks(player, level,
                net.minecraft.world.level.block.Blocks.DIAMOND_BLOCK,
                net.minecraft.world.level.block.Blocks.GOLD_BLOCK,
                net.minecraft.world.level.block.Blocks.EMERALD_BLOCK);
        rate += gemCount * 0.3;
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
