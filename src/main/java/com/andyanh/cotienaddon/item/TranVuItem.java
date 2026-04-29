package com.andyanh.cotienaddon.item;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.init.CoTienAttachments;
import com.andyanh.cotienaddon.init.CoTienBlocks;
import com.andyanh.cotienaddon.init.CoTienItems;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class TranVuItem extends Item {

    public static final int MAX_DURABILITY    = 200;
    public static final int COST_PER_USE      = 100; // nửa thanh
    public static final int REPAIR_PER_NGUYEN = 200; // 1 Tiên Nguyên = full bar = 2 lần dùng
    public static final int SEAL_RADIUS       = 20;
    public static final int SEAL_TICKS        = 200; // 10 giây

    public TranVuItem() {
        super(new Item.Properties().stacksTo(1).durability(MAX_DURABILITY));
    }

    // benminggu = 45.0 = Vũ Đạo (custom path)
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

            player.sendSystemMessage(Component.literal("§b☯ Trấn Vũ Cổ nhận ra chủ nhân — Vũ Đạo khai mở!"));
            CoTienAddon.LOGGER.info("[TranVu] {} bound benminggu=45 (Vũ Đạo)", player.getName().getString());
        } catch (Exception ignored) {}
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(player.getItemInHand(hand));

        ItemStack stack = player.getItemInHand(hand);
        ItemStack offhand = player.getOffhandItem();

        // Tay phụ cầm Tiên Nguyên → nạp vào cổ
        if (!offhand.isEmpty() && offhand.is(
                net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("cotienaddon:tien_nguyen")))) {
            if (!level.isClientSide) {
                feedTienNguyen(stack, offhand, (ServerPlayer) player);
            }
            return InteractionResultHolder.success(stack);
        }

        // Kích hoạt Băng Phong Trận
        if (!level.isClientSide) {
            activateBangPhongTran(stack, (ServerPlayer) player);
        }
        return InteractionResultHolder.success(stack);
    }

    private static void feedTienNguyen(ItemStack gu, ItemStack nguyen, ServerPlayer sp) {
        int dmg = gu.getDamageValue();
        if (dmg == 0) {
            sp.sendSystemMessage(Component.literal("§e[Trấn Vũ] Đã đầy, không cần nạp thêm!"));
            return;
        }
        int repaired = Math.min(REPAIR_PER_NGUYEN, dmg);
        gu.setDamageValue(dmg - repaired);
        if (!sp.isCreative()) nguyen.shrink(1);

        int uses = (MAX_DURABILITY - gu.getDamageValue()) / COST_PER_USE;
        sp.sendSystemMessage(Component.literal(
                String.format("§b[Trấn Vũ] Nạp Tiên Nguyên — còn %d lần dùng.", uses)));
    }

    private static void activateBangPhongTran(ItemStack stack, ServerPlayer sp) {
        if (stack.getDamageValue() + COST_PER_USE > MAX_DURABILITY) {
            sp.sendSystemMessage(Component.literal("§c[Trấn Vũ] Hạn sử dụng không đủ! Nạp Tiên Nguyên vào tay phụ."));
            return;
        }

        stack.setDamageValue(stack.getDamageValue() + COST_PER_USE);

        // Seal mọi entity trong bán kính
        Level level = sp.level();
        List<net.minecraft.world.entity.LivingEntity> nearby = level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                sp.getBoundingBox().inflate(SEAL_RADIUS),
                e -> !e.getUUID().equals(sp.getUUID()));

        int sealed = 0;
        for (net.minecraft.world.entity.LivingEntity target : nearby) {
            target.getPersistentData().putInt("tran_vu_sealed", SEAL_TICKS);
            if (target instanceof Player p) {
                p.sendSystemMessage(Component.literal("§9☯ Không gian xung quanh bị trấn áp — Vũ đạo thất linh!"));
            }
            // Spawn BlockDisplay quan tài băng bao quanh target
            if (level instanceof ServerLevel sl) {
                spawnCoffinDisplay(sl, target);
            }
            sealed++;
        }

        // Particles bao quanh vùng
        if (level instanceof ServerLevel sl) {
            for (int i = 0; i < 60; i++) {
                double angle = Math.PI * 2 * i / 60;
                double px = sp.getX() + SEAL_RADIUS * Math.cos(angle);
                double pz = sp.getZ() + SEAL_RADIUS * Math.sin(angle);
                sl.sendParticles(ParticleTypes.SNOWFLAKE, px, sp.getY() + 1, pz, 2, 0, 0.5, 0, 0.02);
                sl.sendParticles(ParticleTypes.ENCHANT, px, sp.getY() + 1, pz, 1, 0, 0.5, 0, 0.1);
            }
            // Particles trung tâm
            sl.sendParticles(ParticleTypes.SNOWFLAKE, sp.getX(), sp.getY() + 1, sp.getZ(), 40, 1, 1, 1, 0.05);
        }

        level.playSound(null, BlockPos.containing(sp.getX(), sp.getY(), sp.getZ()),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.2f, 0.6f);

        // Add 1 Vũ Đạo marks
        try {
            Class<?> clazz = Class.forName("net.guzhenren.network.GuzhenrenModVariables");
            Object vars = sp.getData((net.neoforged.neoforge.attachment.AttachmentType<?>) clazz.getField("PLAYER_VARIABLES").get(null));
            java.lang.reflect.Field yuDaoField = clazz.getField("liupai_yudao");
            double currentYuDao = yuDaoField.getDouble(vars);
            yuDaoField.setDouble(vars, currentYuDao + 1.0);
            java.lang.reflect.Method syncMethod = clazz.getMethod("markSyncDirty");
            syncMethod.invoke(vars);
            sp.sendSystemMessage(Component.literal("§e✦ Vũ Đạo +1"));
        } catch (Exception e) {
            CoTienAddon.LOGGER.error("Failed to add Vũ Đạo marks: " + e.getMessage());
        }

        int uses = (MAX_DURABILITY - stack.getDamageValue()) / COST_PER_USE;
        sp.sendSystemMessage(Component.literal(
                String.format("§b☯ Băng Phong Trận khai triển — %d player bị phong ấn! (còn %d lần dùng)", sealed, uses)));
    }

    public static void spawnCoffinDisplay(ServerLevel level, net.minecraft.world.entity.LivingEntity target) {
        removeCoffinDisplay(level, target); // Xóa cái cũ nếu có
        Display.BlockDisplay display = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, level);
        display.setPos(target.getX(), target.getY(), target.getZ());

        // Set block state + transformation via NBT (readAdditionalSaveData widened by AT)
        net.minecraft.nbt.CompoundTag nbt = new net.minecraft.nbt.CompoundTag();

        // Block state
        net.minecraft.nbt.CompoundTag blockStateTag = new net.minecraft.nbt.CompoundTag();
        blockStateTag.putString("Name", "cotienaddon:quan_tai_bang");
        nbt.put("block_state", blockStateTag);

        // Transformation: left_rotation, translation, scale, right_rotation (CompoundTag format)
        net.minecraft.nbt.CompoundTag transform = new net.minecraft.nbt.CompoundTag();
        net.minecraft.nbt.ListTag leftRot  = floatList(0f, 0f, 0f, 1f);
        net.minecraft.nbt.ListTag rightRot = floatList(0f, 0f, 0f, 1f);
        net.minecraft.nbt.ListTag translation = floatList(-0.6f, -0.1f, -0.35f);
        net.minecraft.nbt.ListTag scale = floatList(1.2f, 2.2f, 0.7f);
        transform.put("left_rotation",  leftRot);
        transform.put("translation",    translation);
        transform.put("scale",          scale);
        transform.put("right_rotation", rightRot);
        nbt.put("transformation", transform);
        nbt.putString("billboard", "fixed");

        display.readAdditionalSaveData(nbt);
        display.getPersistentData().putString("tran_vu_target_uuid", target.getUUID().toString());
        level.addFreshEntity(display);
    }

    private static net.minecraft.nbt.ListTag floatList(float... values) {
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (float v : values) list.add(net.minecraft.nbt.FloatTag.valueOf(v));
        return list;
    }

    public static void removeCoffinDisplay(ServerLevel level, net.minecraft.world.entity.LivingEntity target) {
        String uuidStr = target.getUUID().toString();
        var displays = level.getEntitiesOfClass(Display.BlockDisplay.class, target.getBoundingBox().inflate(5.0),
                e -> uuidStr.equals(e.getPersistentData().getString("tran_vu_target_uuid")));
        for (var d : displays) d.discard();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        int remaining = MAX_DURABILITY - stack.getDamageValue();
        int uses = remaining / COST_PER_USE;
        double pct = (double) remaining / MAX_DURABILITY * 100;
        tooltip.add(Component.literal(String.format("§b■ Hạn sử dụng: %.1f%%  (còn %d lần)", pct, uses)));
        tooltip.add(Component.literal("§aChuyển số: §fThất Chuyển - Cổ Tiên"));
        tooltip.add(Component.literal("§aLưu phái: §fVũ Đạo"));
        tooltip.add(Component.literal("§dCông dụng: Triển khai Băng Phong Trận, phong ấn không gian bán kính 20 block. Mục tiêu không thể dùng bất kỳ loại dịch chuyển nào."));
        tooltip.add(Component.literal("§6Mỗi lần dùng: -" + COST_PER_USE + " hạn sử dụng  (nửa thanh)"));
        tooltip.add(Component.literal("§aThức ăn: §f1 Tiên Nguyên = 2 lần dùng"));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) { return true; }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * (MAX_DURABILITY - stack.getDamageValue()) / MAX_DURABILITY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float f = (float)(MAX_DURABILITY - stack.getDamageValue()) / MAX_DURABILITY;
        int r = (int)(100 * (1 - f));
        int g = (int)(200 * f);
        int b = 255;
        return (r << 16) | (g << 8) | b;
    }
}
