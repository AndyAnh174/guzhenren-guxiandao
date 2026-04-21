package com.andyanh.cotienaddon.item;

import com.andyanh.cotienaddon.CoTienAddon;
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
    public static final int MAX_DURABILITY = 500;
    public static final int COST_PER_USE   = 25;
    public static final int REPAIR_PER_WINE = 1;
    public static final double TIEN_NGUYEN_COST = 1.0;

    public DinhTienDuItem() {
        super(new Item.Properties().stacksTo(1).durability(MAX_DURABILITY));
    }

    // benminggu = 45.0 = Không Đạo (custom path, beyond mod's 1-44 range)
    public static final double BENMINGGU_KHONG_DAO = 45.0;

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

            vars.benminggu = BENMINGGU_KHONG_DAO;
            vars.markSyncDirty();

            player.sendSystemMessage(Component.literal("§b☯ Định Tiên Du Cổ nhận ra chủ nhân — Không Đạo khai mở!"));
            CoTienAddon.LOGGER.info("[DinhTienDu] {} bound benminggu=45 (Không Đạo)", player.getName().getString());
        } catch (Exception ignored) {}
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(player.getItemInHand(hand));

        ItemStack stack = player.getItemInHand(hand);
        ItemStack offhand = player.getOffhandItem();

        // If holding wine in offhand → feed the gu
        if (isWine(offhand)) {
            if (!level.isClientSide) {
                feedWine(stack, offhand, (ServerPlayer) player);
            }
            return InteractionResultHolder.success(stack);
        }

        // Otherwise open coordinate input screen (client side)
        if (level.isClientSide) {
            net.minecraft.client.Minecraft.getInstance().setScreen(
                    new com.andyanh.cotienaddon.client.DinhTienDuScreen());
        }

        return InteractionResultHolder.success(stack);
    }

    private static void feedWine(ItemStack gu, ItemStack wine, ServerPlayer sp) {
        int currentDamage = gu.getDamageValue();
        if (currentDamage == 0) {
            sp.sendSystemMessage(Component.literal("§e[Định Tiên Du] Đã đầy, không cần cho uống rượu!"));
            return;
        }

        int repaired = Math.min(REPAIR_PER_WINE, currentDamage);
        gu.setDamageValue(currentDamage - repaired);

        if (!sp.isCreative()) {
            wine.shrink(1);
        }

        double pct = (double)(MAX_DURABILITY - gu.getDamageValue()) / MAX_DURABILITY * 100;
        sp.sendSystemMessage(Component.literal(
                String.format("§b[Định Tiên Du] Uống một bình rượu... §7(Hạn sử dụng: %.1f%%)", pct)));
    }

    /**
     * Called from server packet handler to execute teleport after 2s delay.
     * Checks requirements and schedules the teleport.
     */
    public static boolean requestTeleport(ServerPlayer sp, double x, double y, double z) {
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
                    "§c[Định Tiên Du] Hạn sử dụng đã cạn! Hãy cho uống rượu để hồi phục."));
            return false;
        }

        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
        if (data.tienNguyen < TIEN_NGUYEN_COST) {
            sp.sendSystemMessage(Component.literal(
                    String.format("§c[Định Tiên Du] Không đủ Tiên Nguyên! Cần %.0f, hiện có %.1f",
                            TIEN_NGUYEN_COST, data.tienNguyen)));
            return false;
        }

        // Deduct resources
        gu.setDamageValue(gu.getDamageValue() + COST_PER_USE);
        data.tienNguyen -= TIEN_NGUYEN_COST;
        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);

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
                // Refund resources
                finalGu.setDamageValue(Math.max(0, finalGu.getDamageValue() - COST_PER_USE));
                CoTienData d2 = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
                d2.tienNguyen += TIEN_NGUYEN_COST;
                sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), d2);
                return;
            }

            sp.teleportTo(x, y, z);
            if (sp.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL,
                        x, y + 1, z, 40, 0.5, 1, 0.5, 0.2);
            }
            sp.level().playSound(null, BlockPos.containing(x, y, z),
                    net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1f, 1f);
            sp.sendSystemMessage(Component.literal(
                    String.format("§a✦ Định Tiên Du đã dịch chuyển! (%.0f, %.0f, %.0f)", x, y, z)));
            CoTienAddon.LOGGER.info("[DinhTienDu] {} teleported to {},{},{}", sp.getName().getString(), x, y, z);
        });

        return true;
    }

    public static boolean isWine(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = stack.getItemHolder().unwrapKey()
                .map(k -> k.location()).orElse(null);
        if (id == null || !id.getNamespace().equals("guzhenren")) return false;
        String path = id.getPath();
        // Wine items end in "jiu" — exclude gu worm items (chong, nang, xie, etc.)
        return path.endsWith("jiu") && !path.contains("chong") && !path.contains("nang")
                && !path.contains("xie") && !path.contains("gu");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        int remaining = MAX_DURABILITY - stack.getDamageValue();
        double pct = (double) remaining / MAX_DURABILITY * 100;
        tooltip.add(Component.literal(String.format("§b■ Hạn sử dụng: %.1f%%  (còn %d/%d)", pct, remaining, MAX_DURABILITY)));
        tooltip.add(Component.literal("§aChuyển số: §fLục Chuyển - Cổ Tiên"));
        tooltip.add(Component.literal("§aLưu phái: §fKhông Đạo"));
        tooltip.add(Component.literal("§dCông dụng: Định vị không gian, dịch chuyển tức thì tới tọa độ đã định. Có thể lưu tối đa 5 địa điểm."));
        tooltip.add(Component.literal("§6Mỗi lần dùng: -" + COST_PER_USE + " hạn sử dụng  -1 Tiên Nguyên"));
        tooltip.add(Component.literal("§aThức ăn: §fRượu (mỗi bình hồi +" + REPAIR_PER_WINE + " hạn sử dụng)"));
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
