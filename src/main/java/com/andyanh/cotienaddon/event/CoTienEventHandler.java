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
    public static void onEntityTick(net.neoforged.neoforge.event.tick.EntityTickEvent.Post event) {
        var entity = event.getEntity();
        if (entity.level().isClientSide() || !(entity.level() instanceof ServerLevel serverLevel)) return;

        if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
            var pd = living.getPersistentData();
            if (pd.contains("tran_vu_sealed")) {
                int ticks = pd.getInt("tran_vu_sealed") - 1;
                if (ticks <= 0) {
                    pd.remove("tran_vu_sealed");
                    if (living instanceof Player p) {
                        p.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "§b☯ Băng phong tan rã — không gian khôi phục!"));
                    }
                    com.andyanh.cotienaddon.item.TranVuItem.removeCoffinDisplay(serverLevel, living);
                } else {
                    pd.putInt("tran_vu_sealed", ticks);
                    // Khóa di chuyển ngang, giữ trọng lực
                    living.setDeltaMovement(0, Math.min(living.getDeltaMovement().y, 0), 0);
                    living.hurtMarked = true;

                    // Quan tài đi theo entity mỗi tick
                    String uuidStr = living.getUUID().toString();
                    var displays = serverLevel.getEntitiesOfClass(net.minecraft.world.entity.Display.BlockDisplay.class, living.getBoundingBox().inflate(5.0),
                            e -> uuidStr.equals(e.getPersistentData().getString("tran_vu_target_uuid")));
                    if (!displays.isEmpty()) {
                        displays.get(0).setPos(living.getX(), living.getY(), living.getZ());
                    } else {
                        com.andyanh.cotienaddon.item.TranVuItem.spawnCoffinDisplay(serverLevel, living);
                    }

                    // Particles hình quan tài băng mỗi 5 tick
                    if (ticks % 5 == 0) {
                        double px = living.getX(), py = living.getY(), pz = living.getZ();
                        for (int i = 0; i < 12; i++) {
                            double angle = Math.PI * 2 * i / 12;
                            double rx = px + 0.8 * Math.cos(angle);
                            double rz = pz + 0.8 * Math.sin(angle);
                            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE,
                                    rx, py + 0.1, rz, 1, 0, 0.3, 0, 0.01);
                            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE,
                                    rx, py + 1.0, rz, 1, 0, 0.3, 0, 0.01);
                            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE,
                                    rx, py + 1.8, rz, 1, 0, 0.3, 0, 0.01);
                        }
                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,
                                px, py + 2.2, pz, 6, 0.3, 0.1, 0.3, 0.05);
                    }
                }
            }
        }
    }

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
                    // Quest QUEST_WUZHUAN_GU: đếm tier-5 gu consumed
                    if (i == 4 && data.dialinhQuestType == CoTienData.QUEST_WUZHUAN_GU && !data.isQuestComplete()) {
                        data.dialinhQuestProgress += consumed;
                        if (data.isQuestComplete()) {
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    "§a☯ Nhiệm vụ hoàn thành! [Shift+Click Địa Linh để nhận thưởng]"));
                        }
                    }
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

        double lingmaiBonus = 1.0 + data.getLingmaiBonus(); // ×1.0 → ×3.5 ở cấp 5

        if (data.thangTienPhase == 2) {
            // Phase 2: thiền định thăng tiên — Linh Mạch tăng tốc hấp thụ
            data.thienKhi += calcThienKhiRate(player, level) * lingmaiBonus;
            data.diaKhi   += calcDiaKhiRate(player, level)   * lingmaiBonus;
            player.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);

        } else if (data.thangTienPhase == 4 && data.lingmaiLevel > 0) {
            // Phase 4 Cổ Tiên trong Phúc Địa: tích lũy thụ động (dùng cho Thiên Kiếp)
            boolean inPhucDia = com.andyanh.cotienaddon.system.PhucDiaManager
                    .isPhucDiaDimension(level.dimension());
            if (inPhucDia) {
                double passiveRate = data.getLingmaiBonus() * 0.5; // 0.25 → 1.25/s ở cấp 5
                data.thienKhi += passiveRate;
                data.diaKhi   += passiveRate;
                player.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
            }
        }
    }

    /** Chặn mọi loại teleport khi bị Băng Phong Trận — entity bị seal VÀ entity trong bán kính 50 block của một entity đang bị seal */
    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST)
    public static void onEntityTeleport(net.neoforged.neoforge.event.entity.EntityTeleportEvent event) {
        var entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        // Case 1: chính entity bị seal
        if (entity.getPersistentData().contains("tran_vu_sealed")) {
            event.setCanceled(true);
            if (entity instanceof Player player) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§9☯ Băng Phong Trận: không gian bị trấn áp — dịch chuyển thất bại!"));
            }
            return;
        }

        // Case 2: entity khác nằm trong bán kính 20 block của một entity đang seal
        if (!(entity.level() instanceof ServerLevel sl)) return;
        var nearby = sl.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                entity.getBoundingBox().inflate(20.0));
        for (var e : nearby) {
            if (e.getPersistentData().contains("tran_vu_sealed")) {
                event.setCanceled(true);
                return;
            }
        }
    }

    /** Chặn đổi dimension — entity bị seal VÀ entity trong bán kính 50 block của một entity đang bị seal */
    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST)
    public static void onChangeDimension(net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent event) {
        var entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        // Case 1: chính entity bị seal
        if (entity.getPersistentData().contains("tran_vu_sealed")) {
            event.setCanceled(true);
            if (entity instanceof Player player) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§9☯ Băng Phong Trận: không thể vượt qua không gian chiều!"));
            }
            return;
        }

        // Case 2: entity trong vùng seal
        if (!(entity.level() instanceof ServerLevel sl)) return;
        var nearby = sl.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                entity.getBoundingBox().inflate(20.0));
        for (var e : nearby) {
            if (e.getPersistentData().contains("tran_vu_sealed")) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp)) return;

        var gv = sp.getData(net.guzhenren.network.GuzhenrenModVariables.PLAYER_VARIABLES);
        var data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());

        // Bát Chuyển Đỉnh Phong (8.0, 4) hoặc Cửu Chuyển trở lên
        boolean isTon = (gv.zhuanshu >= 8.0 && gv.jieduan >= 4) || (gv.zhuanshu >= 9.0);

        if (isTon && data.tonHieuEnabled && !data.tonHieuName.isEmpty()) {
            String type = gv.daode >= 0 ? "Tiên Tôn" : "Ma Tôn";
            String fullName = data.tonHieuName + " " + type;
            int colorInt = data.tonHieuColor;

            net.minecraft.network.chat.Component titleComponent = net.minecraft.network.chat.Component.literal("✦ " + fullName + " ✦")
                    .withStyle(s -> s.withColor(colorInt).withBold(true));
            net.minecraft.network.chat.Component subtitleComponent = net.minecraft.network.chat.Component.literal("§lĐã Giáng Thế!")
                    .withStyle(s -> s.withColor(net.minecraft.network.chat.TextColor.fromRgb(0xFFFFFF)));

            net.minecraft.network.chat.Component broadcastMsg = net.minecraft.network.chat.Component.literal("§6§l[THÔNG BÁO] ")
                    .append(net.minecraft.network.chat.Component.literal("✦ ").withStyle(s -> s.withColor(colorInt)))
                    .append(net.minecraft.network.chat.Component.literal(fullName).withStyle(s -> s.withColor(colorInt).withBold(true)))
                    .append(net.minecraft.network.chat.Component.literal(" §fđã giáng thế tại thế giới này!"));

            // Gửi cho toàn bộ server
            for (net.minecraft.server.level.ServerPlayer p : sp.server.getPlayerList().getPlayers()) {
                // Gửi chat
                p.sendSystemMessage(broadcastMsg);
                
                // Gửi Title (Màn hình lớn)
                p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(titleComponent));
                p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(subtitleComponent));
                p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(10, 70, 20)); // fadeIn, stay, fadeOut

                // Âm thanh kịch tính
                p.playNotifySound(net.minecraft.sounds.SoundEvents.END_PORTAL_SPAWN, net.minecraft.sounds.SoundSource.AMBIENT, 1.0f, 1.0f);
                p.playNotifySound(net.minecraft.sounds.SoundEvents.WITHER_SPAWN, net.minecraft.sounds.SoundSource.AMBIENT, 0.5f, 0.8f);
            }
        }
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
