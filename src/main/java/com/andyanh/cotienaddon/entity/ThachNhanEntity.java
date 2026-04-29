package com.andyanh.cotienaddon.entity;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.init.CoTienAttachments;
import com.andyanh.cotienaddon.init.CoTienBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ThachNhanEntity extends PathfinderMob {

    // Synched data
    private static final EntityDataAccessor<Integer> HP_LEVEL =
            SynchedEntityData.defineId(ThachNhanEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATK_LEVEL =
            SynchedEntityData.defineId(ThachNhanEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SPD_LEVEL =
            SynchedEntityData.defineId(ThachNhanEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> OWNER_UUID =
            SynchedEntityData.defineId(ThachNhanEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> IN_COMBAT =
            SynchedEntityData.defineId(ThachNhanEntity.class, EntityDataSerializers.BOOLEAN);

    // Inventory: 27 slots (dùng ChestMenu.threeRows cần đúng 27)
    private final SimpleContainer inventory = new SimpleContainer(27);

    private int deliverCooldown = 0;

    public ThachNhanEntity(EntityType<? extends ThachNhanEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.getPersistentData().putBoolean("cotien_spawned", true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HP_LEVEL, 0);
        builder.define(ATK_LEVEL, 0);
        builder.define(SPD_LEVEL, 0);
        builder.define(OWNER_UUID, "");
        builder.define(IN_COMBAT, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, true));
        goalSelector.addGoal(2, new MineOreGoal(this));
        goalSelector.addGoal(3, new DeliverToDialinhGoal(this));
        goalSelector.addGoal(5, new RandomStrollGoal(this, 0.6));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // Combat mode khi có Kiếp/Tai
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false,
                mob -> mob instanceof Enemy && isInCombat()));
    }

    // ── Stats ──────────────────────────────────────────────────────────────

    public int getHpLevel()  { return entityData.get(HP_LEVEL); }
    public int getAtkLevel() { return entityData.get(ATK_LEVEL); }
    public int getSpdLevel() { return entityData.get(SPD_LEVEL); }
    public String getOwnerUUID() { return entityData.get(OWNER_UUID); }
    public boolean isInCombat() { return entityData.get(IN_COMBAT); }

    public void setOwnerUUID(String uuid) { entityData.set(OWNER_UUID, uuid); }
    public void setInCombat(boolean v)    { entityData.set(IN_COMBAT, v); }

    /** Skin level 1-5 based on max upgrade level */
    public int getSkinLevel() {
        int max = Math.max(getHpLevel(), Math.max(getAtkLevel(), getSpdLevel()));
        return Math.min(5, max + 1);
    }

    /** Max HP = 200 + hpLevel * 150 */
    public double getMaxHp()     { return 200 + getHpLevel() * 150.0; }
    /** Attack = 8 + atkLevel * 6 */
    public double getAttackDmg() { return 8 + getAtkLevel() * 6.0; }
    /** Mining ticks per block: 100 - spdLevel * 18 (min 20 = ~1s) */
    public int getMiningSpeed()  { return Math.max(20, 100 - getSpdLevel() * 18); }

    public static int getUpgradeCost(int currentLevel) {
        return switch (currentLevel) {
            case 0 -> 30;
            case 1 -> 60;
            case 2 -> 120;
            case 3 -> 250;
            default -> Integer.MAX_VALUE;
        };
    }

    public void applyStats() {
        if (!level().isClientSide) {
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(getMaxHp());
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(getAttackDmg());
            if (getHealth() > getMaxHp()) setHealth((float) getMaxHp());
        }
    }

    public boolean upgradeHp(double availableTN) {
        int lvl = getHpLevel();
        int cost = getUpgradeCost(lvl);
        if (lvl >= 4 || availableTN < cost) return false;
        entityData.set(HP_LEVEL, lvl + 1);
        applyStats();
        return true;
    }

    public boolean upgradeAtk(double availableTN) {
        int lvl = getAtkLevel();
        int cost = getUpgradeCost(lvl);
        if (lvl >= 4 || availableTN < cost) return false;
        entityData.set(ATK_LEVEL, lvl + 1);
        applyStats();
        return true;
    }

    public boolean upgradeSpd(double availableTN) {
        int lvl = getSpdLevel();
        int cost = getUpgradeCost(lvl);
        if (lvl >= 4 || availableTN < cost) return false;
        entityData.set(SPD_LEVEL, lvl + 1);
        return true;
    }

    // ── Inventory ──────────────────────────────────────────────────────────

    public SimpleContainer getInventory() { return inventory; }

    public boolean isInventoryFull() {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).isEmpty()) return false;
        }
        return true;
    }

    public boolean hasItems() {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (!inventory.getItem(i).isEmpty()) return true;
        }
        return false;
    }

    public void addToInventory(ItemStack stack) {
        // Try to merge first
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack slot = inventory.getItem(i);
            if (slot.isEmpty()) {
                inventory.setItem(i, stack.copy());
                return;
            }
            if (ItemStack.isSameItemSameComponents(slot, stack) && slot.getCount() < slot.getMaxStackSize()) {
                int space = slot.getMaxStackSize() - slot.getCount();
                int take = Math.min(space, stack.getCount());
                slot.grow(take);
                stack.shrink(take);
                if (stack.isEmpty()) return;
            }
        }
        // Drop overflow
        spawnAtLocation(stack);
    }

    // ── Mining ─────────────────────────────────────────────────────────────

    private static final List<Block> ORE_TARGETS = List.of(
        Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
        Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
        Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
        Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
        Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
        Blocks.COAL_ORE,
        Blocks.ANCIENT_DEBRIS
        // guzhenren + cotienaddon ores thêm dưới
    );

    public static boolean isOreBlock(BlockState state) {
        if (ORE_TARGETS.stream().anyMatch(state::is)) return true;
        if (state.is(CoTienBlocks.KHOI_TIEN_NGUYEN.get())) return true;
        // Check mod ore by registry name
        var loc = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (loc != null) {
            String path = loc.toString();
            return path.contains("yuankuang") || path.contains("ore") || path.contains("kuang");
        }
        return false;
    }

    public BlockPos findNearestOre(int radius) {
        if (!(level() instanceof ServerLevel sl)) return null;
        BlockPos myPos = blockPosition();
        BlockPos nearest = null;
        double bestDist = Double.MAX_VALUE;
        // Tìm theo chiều ngang, nhưng quét sâu xuống 55 block (Y=0..54 từ mặt đất)
        for (int dx = -radius; dx <= radius; dx += 2) {
            for (int dz = -radius; dz <= radius; dz += 2) {
                for (int dy = -55; dy <= 5; dy++) {  // quét xuống sâu đến Y~0
                    BlockPos p = myPos.offset(dx, dy, dz);
                    if (p.getY() < 1) continue;
                    if (isOreBlock(sl.getBlockState(p))) {
                        // Ưu tiên gần theo chiều ngang
                        double hDist = (dx * dx + dz * dz);
                        if (hDist < bestDist) { bestDist = hDist; nearest = p; }
                    }
                }
            }
        }
        return nearest;
    }

    // ── Interaction ────────────────────────────────────────────────────────

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!level().isClientSide && player instanceof ServerPlayer sp) {
            if (sp.isShiftKeyDown()) {
                // Shift+click → mở kho như rương
                final ThachNhanEntity self = this;
                sp.openMenu(new net.minecraft.world.SimpleMenuProvider(
                        (id, playerInv, p) -> net.minecraft.world.inventory.ChestMenu.threeRows(id, playerInv, self.inventory),
                        net.minecraft.network.chat.Component.literal("§8⚒ Kho Thạch Nhân")));
            } else {
                // Click thường → upgrade screen
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp,
                        new com.andyanh.cotienaddon.network.OpenThachNhanScreenPacket(this.getId()));
            }
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    // ── NBT ────────────────────────────────────────────────────────────────

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("hpLevel", getHpLevel());
        tag.putInt("atkLevel", getAtkLevel());
        tag.putInt("spdLevel", getSpdLevel());
        tag.putString("ownerUUID", getOwnerUUID());
        CompoundTag inv = new CompoundTag();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (!inventory.getItem(i).isEmpty()) {
                inv.put("slot" + i, inventory.getItem(i).save(level().registryAccess()));
            }
        }
        tag.put("inventory", inv);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(HP_LEVEL, tag.getInt("hpLevel"));
        entityData.set(ATK_LEVEL, tag.getInt("atkLevel"));
        entityData.set(SPD_LEVEL, tag.getInt("spdLevel"));
        entityData.set(OWNER_UUID, tag.getString("ownerUUID"));
        if (tag.contains("inventory")) {
            CompoundTag inv = tag.getCompound("inventory");
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                if (inv.contains("slot" + i)) {
                    inventory.setItem(i, ItemStack.parseOptional(level().registryAccess(), inv.getCompound("slot" + i)));
                }
            }
        }
        applyStats();
    }

    // ── AI Goals ───────────────────────────────────────────────────────────

    // ── Mining State Machine ───────────────────────────────────────────────
    private int mineState = 0;
    private BlockPos targetOre = null;
    private BlockPos shaftTop = null;
    private int actionTimer = 0;

    private void tickMining() {
        if (level().isClientSide) return;
        if (!(level() instanceof ServerLevel sl)) return;
        if (isInCombat()) { if (mineState != 0) resetMining(sl); return; }

        actionTimer--;

        switch (mineState) {

            // STATE 0: Tìm quặng, điều hướng tới cột trên bề mặt
            case 0 -> {
                if (actionTimer > 0) return;
                if (isInventoryFull()) { actionTimer = 100; return; }
                targetOre = findNearestOre(30);
                if (targetOre == null) { actionTimer = 30; return; }
                // WORLD_SURFACE returns Y of first non-air block above ground + 1
                // shaftTop là vị trí đứng trên mặt đất (Y = surface height)
                int sy = sl.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                        targetOre.getX(), targetOre.getZ());
                shaftTop = new BlockPos(targetOre.getX(), sy, targetOre.getZ());
                getNavigation().moveTo(shaftTop.getX() + 0.5, shaftTop.getY() + 0.5, shaftTop.getZ() + 0.5, 1.0);
                mineState = 1;
                actionTimer = 150; // tối đa 7.5s để đi tới
            }

            // STATE 1: Đi bộ đến cột trên mặt đất
            case 1 -> {
                if (targetOre == null) { mineState = 0; return; }
                double hd = (getX()-shaftTop.getX()-0.5)*(getX()-shaftTop.getX()-0.5)
                          + (getZ()-shaftTop.getZ()-0.5)*(getZ()-shaftTop.getZ()-0.5);
                if (hd < 3.0 || actionTimer <= 0) {
                    getNavigation().stop();
                    getLookControl().setLookAt(targetOre.getX(), targetOre.getY(), targetOre.getZ(), 30, 30);
                    mineState = 2;
                    actionTimer = getMiningSpeed();
                }
            }

            // STATE 2: Đứng tại bề mặt, animation đào xuống, phá quặng từ xa
            case 2 -> {
                if (targetOre == null) { resetMining(sl); return; }

                // Luôn nhìn xuống hướng ore
                getLookControl().setLookAt(targetOre.getX(), targetOre.getY(), targetOre.getZ(), 30, 30);

                if (actionTimer > 0) {
                    // Animation: swing + particle dọc shaft mỗi 6 tick
                    if (actionTimer % 6 == 0) {
                        swing(InteractionHand.MAIN_HAND);
                        // Particle chạy dọc từ mặt đất xuống ore
                        int totalDy = shaftTop.getY() - targetOre.getY();
                        int progress = (int)((1.0 - (double)actionTimer / getMiningSpeed()) * totalDy);
                        int particleY = shaftTop.getY() - progress;
                        sl.sendParticles(new net.minecraft.core.particles.BlockParticleOption(
                                net.minecraft.core.particles.ParticleTypes.BLOCK,
                                net.minecraft.world.level.block.Blocks.STONE.defaultBlockState()),
                                shaftTop.getX()+0.5, particleY, shaftTop.getZ()+0.5,
                                3, 0.15, 0.15, 0.15, 0);
                    }
                    return;
                }

                // Đào! Phá quặng từ bề mặt
                BlockState oreState = sl.getBlockState(targetOre);
                if (isOreBlock(oreState)) {
                    List<ItemStack> drops = Block.getDrops(oreState, sl, targetOre, null);
                    sl.removeBlock(targetOre, false);
                    // Particle bùng nổ tại ore
                    sl.sendParticles(new net.minecraft.core.particles.BlockParticleOption(
                            net.minecraft.core.particles.ParticleTypes.BLOCK, oreState),
                            targetOre.getX()+0.5, targetOre.getY()+0.5, targetOre.getZ()+0.5,
                            20, 0.4, 0.4, 0.4, 0.1);
                    sl.playSound(null, targetOre, oreState.getSoundType().getBreakSound(),
                            SoundSource.BLOCKS, 1.5f, 0.8f);
                    for (ItemStack drop : drops) addToInventory(drop);
                }
                swing(InteractionHand.MAIN_HAND);
                mineState = 0;
                targetOre = null; shaftTop = null;
                actionTimer = 15;
            }
        }
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        // Miễn nhiễm damage môi trường (ngạt đá, rơi, lửa, void...)
        var type = source.type();
        if (type == level().damageSources().inWall().type()) return false;
        if (type == level().damageSources().fellOutOfWorld().type()) return false;
        if (type == level().damageSources().fall().type()) return false;
        if (type == level().damageSources().lava().type()) return false;
        if (type == level().damageSources().onFire().type()) return false;
        return super.hurt(source, amount);
    }

    private void resetMining(ServerLevel sl) {
        getNavigation().stop();
        mineState = 0; targetOre = null; shaftTop = null;
        actionTimer = 20;
    }

    @Override
    public void tick() {
        super.tick();
        tickMining();
    }

    // Placeholder goal để không crash
    static class MineOreGoal extends Goal {
        MineOreGoal(ThachNhanEntity mob) {}
        @Override public boolean canUse() { return false; }
    }

    static class DeliverToDialinhGoal extends Goal {
        private final ThachNhanEntity mob;
        private DiaSinhEntity targetDialinh;

        DeliverToDialinhGoal(ThachNhanEntity mob) {
            this.mob = mob;
            setFlags(java.util.EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!mob.hasItems()) return false;
            if (mob.isInCombat()) return false;
            // Find Địa Linh of same owner in range
            if (!(mob.level() instanceof ServerLevel sl)) return false;
            var list = sl.getEntitiesOfClass(DiaSinhEntity.class,
                    mob.getBoundingBox().inflate(60),
                    e -> mob.getOwnerUUID().equals(e.getOwnerUUID()));
            if (list.isEmpty()) return false;
            targetDialinh = list.get(0);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return targetDialinh != null && targetDialinh.isAlive() && mob.hasItems() && !mob.isInCombat();
        }

        @Override
        public void tick() {
            if (targetDialinh == null) return;
            mob.getLookControl().setLookAt(targetDialinh, 10, 10);
            mob.getNavigation().moveTo(targetDialinh, 1.0);

            if (mob.distanceToSqr(targetDialinh) < 9.0) {
                // Transfer items to Địa Linh inventory
                for (int i = 0; i < mob.inventory.getContainerSize(); i++) {
                    ItemStack stack = mob.inventory.getItem(i);
                    if (!stack.isEmpty()) {
                        targetDialinh.addToStorage(stack.copy());
                        mob.inventory.setItem(i, ItemStack.EMPTY);
                    }
                }
                mob.level().playSound(null, mob.blockPosition(),
                        SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.5f, 1.2f);
                targetDialinh = null;
            }
        }
    }
}
