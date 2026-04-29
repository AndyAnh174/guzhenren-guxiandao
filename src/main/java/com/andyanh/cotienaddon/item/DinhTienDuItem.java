package com.andyanh.cotienaddon.item;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.client.CoTienClientHandler;
import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.init.CoTienAttachments;
import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class DinhTienDuItem extends Item {

    // Durability: 500 units. Each teleport costs 25. Full bar = 20 teleports.
    // Each wine restores 1 durability → 15 wine ≈ 1.2 extra uses (tiny)
    public static final int MAX_DURABILITY = 100;
    public static final int COST_PER_USE   = 50;
    public static final int REPAIR_PER_TIENNGUYEN = 100;
    public static final double TIEN_NGUYEN_COST = 1.0;

    public DinhTienDuItem() {
        super(new Item.Properties().stacksTo(1).durability(MAX_DURABILITY));
    }

    // benminggu = 45.0 = Vũ Đạo (custom path, beyond mod's 1-44 range)
    public static final double BENMINGGU_VU_DAO = 45.0;

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player player)) return;
        try {
            Holder<net.minecraft.world.item.enchantment.Enchantment> benMingGu =
                    level.registryAccess()
                         .lookupOrThrow(Registries.ENCHANTMENT)
                         .getOrThrow(ResourceKey.create(Registries.ENCHANTMENT,
                                 ResourceLocation.parse("guzhenren:ben_ming_gu")));
            if (stack.getEnchantmentLevel(benMingGu) == 0) return;

            GuzhenrenModVariables.PlayerVariables vars = player.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
            if (vars.benminggu != 0.0) return;

            vars.benminggu = BENMINGGU_VU_DAO;
            vars.markSyncDirty();

            player.sendSystemMessage(Component.literal("§b☯ Định Tiên Du Cổ nhận ra chủ nhân — Vũ Đạo khai mở!"));
            CoTienAddon.LOGGER.info("[DinhTienDu] {} bound benminggu=45 (Vũ Đạo)", player.getName().getString());
        } catch (Exception ignored) {}
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(player.getItemInHand(hand));

        ItemStack stack = player.getItemInHand(hand);
        ItemStack offhand = player.getOffhandItem();

        // If holding Tien Nguyen in offhand → feed the gu
        if (isTienNguyen(offhand)) {
            if (!level.isClientSide) {
                feedTienNguyen(stack, offhand, (ServerPlayer) player);
            }
            return InteractionResultHolder.success(stack);
        }

        // Otherwise open coordinate input screen (client side)
        if (level.isClientSide) {
            CoTienClientHandler.openDinhTienDuScreen();
        }

        return InteractionResultHolder.success(stack);
    }

    private static void feedTienNguyen(ItemStack gu, ItemStack tienNguyen, ServerPlayer sp) {
        int currentDamage = gu.getDamageValue();
        if (currentDamage == 0) {
            sp.sendSystemMessage(Component.literal("§e[Định Tiên Du] Đã đầy, không cần nạp Tiên Nguyên!"));
            return;
        }

        int repaired = Math.min(REPAIR_PER_TIENNGUYEN, currentDamage);
        gu.setDamageValue(currentDamage - repaired);

        if (!sp.isCreative()) {
            tienNguyen.shrink(1);
        }

        double pct = (double)(MAX_DURABILITY - gu.getDamageValue()) / MAX_DURABILITY * 100;
        sp.sendSystemMessage(Component.literal(
                String.format("§b[Định Tiên Du] Nạp Tiên Nguyên... §7(Hạn sử dụng: %.1f%%)", pct)));
    }

    /**
     * Called from server packet handler to execute teleport after 2s delay.
     * Checks requirements and schedules the teleport.
     */
    public static boolean requestTeleport(ServerPlayer sp, double x, double y, double z, String dimensionId) {
        ItemStack gu = sp.getMainHandItem();
        if (!(gu.getItem() instanceof DinhTienDuItem)) {
            // Also check offhand
            gu = sp.getOffhandItem();
            if (!(gu.getItem() instanceof DinhTienDuItem)) {
                sp.sendSystemMessage(Component.literal("§c[Định Tiên Du] Không tìm thấy Cổ trong tay!"));
                return false;
            }
        }

        if (gu.getDamageValue() >= MAX_DURABILITY) {
            sp.sendSystemMessage(Component.literal(
                    "§c[Định Tiên Du] Hạn sử dụng đã cạn! Hãy nạp Tiên Nguyên để hồi phục."));
            return false;
        }

        // Deduct only durability (Tien Nguyen is now a 'fuel' for repair, not a per-use cost)
        gu.setDamageValue(gu.getDamageValue() + COST_PER_USE);

        // Record position to detect movement
        double startX = sp.getX(), startY = sp.getY(), startZ = sp.getZ();

        sp.sendSystemMessage(Component.literal(
                String.format("§b☯ Định Tiên Du đang định vị... (%.0f, %.0f, %.0f) — Đứng yên 2 giây!", x, y, z)));

        // Spawn particles during charge-up
        if (sp.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                    sp.getX(), sp.getY() + 1, sp.getZ(), 30, 0.5, 1, 0.5, 0.3);
        }

        final ItemStack finalGu = gu;
        net.guzhenren.GuzhenrenMod.queueServerWork(40, () -> {
            if (!sp.isAlive()) return;
            // Cancel if player moved
            double moved = Math.abs(sp.getX() - startX) + Math.abs(sp.getZ() - startZ);
            if (moved > 1.5) {
                sp.sendSystemMessage(Component.literal("§c[Định Tiên Du] Di chuyển → hủy dịch chuyển!"));
                // Refund durability
                finalGu.setDamageValue(Math.max(0, finalGu.getDamageValue() - COST_PER_USE));
                return;
            }

            // Cross-dimension teleport
            net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimKey =
                net.minecraft.resources.ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION,
                    net.minecraft.resources.ResourceLocation.parse(
                        (dimensionId == null || dimensionId.isEmpty()) ? "minecraft:overworld" : dimensionId));
            net.minecraft.server.level.ServerLevel targetLevel = sp.server.getLevel(dimKey);
            if (targetLevel == null) targetLevel = sp.server.overworld();

            if (targetLevel == sp.level()) {
                sp.teleportTo(x, y, z);
            } else {
                sp.teleportTo(targetLevel, x, y, z, sp.getYRot(), sp.getXRot());
            }
            final net.minecraft.server.level.ServerLevel finalTarget = targetLevel;
            if (finalTarget instanceof net.minecraft.server.level.ServerLevel sl) {
                sl.sendParticles(sp, net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL,
                        true, x, y + 1, z, 40, 0.5, 1, 0.5, 0.2);
            }
            sp.level().playSound(null, BlockPos.containing(x, y, z),
                    net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1f, 1f);
            
            // Increase Trụ Đạo (dahen_zhoudao) by 1 + Vũ Đạo (liupai_yudao) by 0.05
            GuzhenrenModVariables.PlayerVariables vars = sp.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
            vars.dahen_zhoudao += 1.0;
            vars.liupai_yudao += 0.05;
            vars.markSyncDirty();

            sp.sendSystemMessage(Component.literal(
                    String.format("§a✦ Định Tiên Du đã dịch chuyển! (%.0f, %.0f, %.0f) | §eTrụ Đạo +1 | §eVũ Đạo +0.05", x, y, z)));
            CoTienAddon.LOGGER.info("[DinhTienDu] {} teleported to {},{},{} and gained Trụ Đạo + Vũ Đạo", sp.getName().getString(), x, y, z);
        });

        return true;
    }

    public static boolean isTienNguyen(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = stack.getItemHolder().unwrapKey()
                .map(k -> k.location()).orElse(null);
        if (id == null || !id.getNamespace().equals("cotienaddon")) return false;
        return id.getPath().equals("tien_nguyen");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        int remaining = MAX_DURABILITY - stack.getDamageValue();
        double pct = (double) remaining / MAX_DURABILITY * 100;
        tooltip.add(Component.literal(String.format("§b■ Hạn sử dụng: %.1f%%  (còn %d/%d)", pct, remaining, MAX_DURABILITY)));
        tooltip.add(Component.literal("§aChuyển số: §fLục Chuyển - Cổ Tiên"));
        tooltip.add(Component.literal("§aLưu phái: §fVũ Đạo"));
        tooltip.add(Component.literal("§dCông dụng: Định vị không gian, dịch chuyển tức thì tới tọa độ đã định. Có thể lưu tối đa 5 địa điểm."));
        tooltip.add(Component.literal("§6Mỗi lần dùng: -" + COST_PER_USE + " hạn sử dụng"));
        tooltip.add(Component.literal("§aThức ăn: §fTiên Nguyên (mỗi đơn vị hồi +" + REPAIR_PER_TIENNGUYEN + " hạn sử dụng)"));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * (MAX_DURABILITY - stack.getDamageValue()) / MAX_DURABILITY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float f = (float)(MAX_DURABILITY - stack.getDamageValue()) / MAX_DURABILITY;
        // Blue-cyan gradient: full = cyan, empty = red
        int r = (int)(255 * (1 - f));
        int g = (int)(200 * f);
        int b = (int)(255 * f);
        return (r << 16) | (g << 8) | b;
    }
}
