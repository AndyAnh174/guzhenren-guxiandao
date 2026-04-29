package com.andyanh.cotienaddon.entity;

import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.init.CoTienAttachments;
import com.andyanh.cotienaddon.init.CoTienItems;
import com.andyanh.cotienaddon.util.GuTierDetector;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.Objects;

public class DiaSinhEntity extends PathfinderMob implements RangedAttackMob {

    public static final int SKIN_COUNT = 14;
    private static final EntityDataAccessor<Integer> SKIN_INDEX =
            SynchedEntityData.defineId(DiaSinhEntity.class, EntityDataSerializers.INT);

    private String ownerUUID = "";

    // Kho nhận đồ từ Thạch Nhân — kích thước thay đổi theo storageLevel (0-3 → 27/36/45/54 slot)
    private int storageLevel = 0;
    private net.minecraft.world.SimpleContainer storage = new net.minecraft.world.SimpleContainer(27);

    public net.minecraft.world.SimpleContainer getStorage() { return storage; }
    public int getStorageLevel() { return storageLevel; }

    public static int getSlotsForLevel(int level) { return 27 + 9 * Math.min(3, Math.max(0, level)); }

    /** Nâng cấp kho lên level mới, copy items sang container mới */
    public void setStorageLevel(int level) {
        level = Math.min(3, Math.max(0, level));
        if (level <= this.storageLevel) return;
        int newSize = getSlotsForLevel(level);
        net.minecraft.world.SimpleContainer newStorage = new net.minecraft.world.SimpleContainer(newSize);
        for (int i = 0; i < storage.getContainerSize() && i < newSize; i++) {
            newStorage.setItem(i, storage.getItem(i).copy());
        }
        this.storage = newStorage;
        this.storageLevel = level;
    }

    public void addToStorage(net.minecraft.world.item.ItemStack stack) {
        for (int i = 0; i < storage.getContainerSize(); i++) {
            net.minecraft.world.item.ItemStack slot = storage.getItem(i);
            if (slot.isEmpty()) { storage.setItem(i, stack.copy()); return; }
            if (net.minecraft.world.item.ItemStack.isSameItemSameComponents(slot, stack)
                    && slot.getCount() < slot.getMaxStackSize()) {
                int take = Math.min(slot.getMaxStackSize() - slot.getCount(), stack.getCount());
                slot.grow(take);
                stack.shrink(take);
                if (stack.isEmpty()) return;
            }
        }
        spawnAtLocation(stack); // overflow → drop
    }
    private long lastInteractTime = 0;
    private long lastQuestShowTime = 0; // lần cuối hiện tiến độ (dùng cho abandon confirm)

    public DiaSinhEntity(EntityType<? extends DiaSinhEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        setCustomName(Component.literal("§2☯ §aĐịa Linh §2☯"));
        setCustomNameVisible(true);
        if (!level.isClientSide()) equipWeapon();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100000.0)
                .add(Attributes.MOVEMENT_SPEED, 0.55)
                .add(Attributes.ATTACK_DAMAGE, 40.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 20.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.FloatGoal(this));
        goalSelector.addGoal(1, new RangedAttackGoal(this, 1.3, 25, 30.0f));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.4, true));
        goalSelector.addGoal(5, new net.minecraft.world.entity.ai.goal.RandomStrollGoal(this, 0.5));
        goalSelector.addGoal(6, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, net.minecraft.world.entity.player.Player.class, 8.0f));
        // Bảo vệ chủ nhân: tấn công bất kỳ thực thể tấn công owner
        targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal(this));
        targetSelector.addGoal(2, new ProtectOwnerTargetGoal(this));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false,
                e -> e instanceof Enemy && !(e instanceof DiaSinhEntity)));
    }

    // Goal: tấn công kẻ vừa gây hại cho chủ nhân
    static class ProtectOwnerTargetGoal extends net.minecraft.world.entity.ai.goal.target.TargetGoal {
        private final DiaSinhEntity dialih;
        private net.minecraft.world.entity.LivingEntity attacker;

        ProtectOwnerTargetGoal(DiaSinhEntity mob) {
            super(mob, true, false);
            this.dialih = mob;
        }

        @Override
        public boolean canUse() {
            String ownerUUID = dialih.ownerUUID;
            if (ownerUUID.isEmpty()) return false;
            if (!(dialih.level() instanceof ServerLevel sl)) return false;
            var owner = sl.getServer().getPlayerList().getPlayer(java.util.UUID.fromString(ownerUUID));
            if (owner == null) return false;
            var lastHurt = owner.getLastHurtByMob();
            if (lastHurt == null || lastHurt == dialih) return false;
            // Nếu owner vừa bị tấn công trong vòng 5 giây
            if (owner.tickCount - owner.getLastHurtByMobTimestamp() > 100) return false;
            attacker = lastHurt;
            return true;
        }

        @Override
        public void start() { dialih.setTarget(attacker); super.start(); }
    }

    // Scale stats theo phucDiaLevel của owner và skill cấp
    public void updateStatsFromOwner(CoTienData ownerData) {
        int lvl = ownerData.phucDiaLevel;
        double hp  = 100000.0 + lvl * 10000.0 + ownerData.dialinhSkillHp * 50000.0;
        double dmg = 40.0 + lvl * 3.0 + ownerData.productionLevel * 2.0 + ownerData.dialinhSkillDamage * 20.0;
        AttributeInstance hpAttr  = getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance dmgAttr = getAttribute(Attributes.ATTACK_DAMAGE);
        if (hpAttr  != null) { hpAttr.setBaseValue(hp); setHealth((float) hp); }
        if (dmgAttr != null) dmgAttr.setBaseValue(dmg);
        // Đặt tên tùy chọn nếu đã đặt
        if (!ownerData.dialinhCustomName.isEmpty()) {
            setCustomName(Component.literal("§2☯ §a" + ownerData.dialinhCustomName + " §2☯"));
        }
        // Sync kho storage level từ CoTienData (đề phòng entity load lại)
        if (ownerData.dialinhStorageLevel > storageLevel) {
            setStorageLevel(ownerData.dialinhStorageLevel);
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (level() instanceof ServerLevel sl) {
            var jade = new DustParticleOptions(new Vector3f(0.1f, 0.95f, 0.45f), 1.8f);
            sl.sendParticles(jade, getX(), getEyeY(), getZ(), 12, 0.4, 0.4, 0.4, 0.05);
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, getX(), getEyeY(), getZ(), 4, 0.2, 0.2, 0.2, 0.3);
            double steps = 8;
            for (int i = 1; i <= steps; i++) {
                double f = i / steps;
                sl.sendParticles(jade,
                        getX() + (target.getX() - getX()) * f,
                        getEyeY() + (target.getEyeY() - getEyeY()) * f,
                        getZ() + (target.getZ() - getZ()) * f,
                        1, 0.1, 0.1, 0.1, 0);
            }
        }
        Arrow arrow = new Arrow(EntityType.ARROW, level());
        arrow.setOwner(this);
        arrow.setPos(getX(), getEyeY() - 0.1, getZ());
        arrow.setBaseDamage(25.0);
        arrow.setCritArrow(true);
        double dx = target.getX() - getX();
        double dy = target.getY(0.3333) - getEyeY();
        double dz = target.getZ() - getZ();
        double hDist = Math.sqrt(dx * dx + dz * dz) * 0.2;
        arrow.shoot(dx, dy + hDist, dz, 3.0f, 1.0f);
        level().addFreshEntity(arrow);
    }

    public void equipWeapon() {
        var itemId = ResourceLocation.tryParse("guzhenren:wu_zhuanjianqi");
        var item = itemId != null ? BuiltInRegistries.ITEM.get(itemId) : null;
        if (item != null && item != Items.AIR) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(item));
        } else {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (level().isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;

        // Cooldown 500ms để tránh double-fire
        long now = System.currentTimeMillis();
        if (now - lastInteractTime < 500) return InteractionResult.SUCCESS;
        lastInteractTime = now;

        // === Cô Hồn Địa Linh — Chấp Niệm ===
        if (isOrphaned()) {
            handleChapNiemInteraction(sp);
            return InteractionResult.SUCCESS;
        }

        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
        boolean isOwner = sp.getUUID().toString().equals(ownerUUID);

        // Shift+click: xem kho CHỈ KHI đã nhận chủ (bond complete) và không có quest đang làm
        if (sp.isShiftKeyDown() && data.dialinhBondComplete && !data.hasActiveQuest() && !data.isQuestComplete()) {
            final DiaSinhEntity self = this;
            int rows = 3 + storageLevel; // 3-6 rows
            net.minecraft.world.inventory.MenuType<?> menuType = switch (rows) {
                case 4  -> net.minecraft.world.inventory.MenuType.GENERIC_9x4;
                case 5  -> net.minecraft.world.inventory.MenuType.GENERIC_9x5;
                case 6  -> net.minecraft.world.inventory.MenuType.GENERIC_9x6;
                default -> net.minecraft.world.inventory.MenuType.GENERIC_9x3;
            };
            sp.openMenu(new net.minecraft.world.SimpleMenuProvider(
                    (id, playerInv, p) -> new net.minecraft.world.inventory.ChestMenu(
                            menuType, id, playerInv, self.storage, rows),
                    net.minecraft.network.chat.Component.literal("§2☯ Kho Địa Linh §7[" + (rows*9) + " slot]")));
            return InteractionResult.SUCCESS;
        }

        // Nộp Cổ Trùng Ngũ Chuyển (tier-5 gu) nếu đang quest WUZHUAN_GU
        if (!player.isShiftKeyDown() && data.dialinhQuestType == CoTienData.QUEST_WUZHUAN_GU && data.hasActiveQuest()) {
            ItemStack held = player.getItemInHand(hand);
            if (GuTierDetector.getTier(held) == 5 && held.getCount() > 0) {
                int remaining = (int)(data.dialinhQuestGoal - data.dialinhQuestProgress);
                int toSubmit = Math.min(held.getCount(), remaining);
                if (!player.isCreative()) held.shrink(toSubmit); // creative giữ item, vẫn tính progress
                data.dialinhQuestProgress += toSubmit;
                sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                sp.sendSystemMessage(Component.literal("§e[Địa Linh] Nhận §f" + toSubmit
                        + " §eCổ Trùng Ngũ Chuyển. Tiến độ: "
                        + (int)data.dialinhQuestProgress + "/" + (int)data.dialinhQuestGoal));
                if (data.isQuestComplete()) {
                    sp.sendSystemMessage(Component.literal("§a☯ Nhiệm vụ hoàn thành! [Shift+Click để nhận thưởng]"));
                }
                return InteractionResult.SUCCESS;
            }
        }

        // Nộp Đạo Ngân nếu đang Chấp Niệm quest
        if (!player.isShiftKeyDown() && data.dialinhQuestType == CoTienData.QUEST_CLAIM_ORPHANED && data.hasActiveQuest()) {
            ItemStack held = player.getItemInHand(hand);
            if (held.getItem() == com.andyanh.cotienaddon.init.CoTienItems.DAO_NGAN.get() && held.getCount() > 0) {
                int remaining = (int)(data.dialinhQuestGoal - data.dialinhQuestProgress);
                int toSubmit = Math.min(held.getCount(), remaining);
                held.shrink(toSubmit);
                data.dialinhQuestProgress += toSubmit;
                sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                sp.sendSystemMessage(Component.literal("§5[Chấp Niệm] Nhận §f" + toSubmit
                        + " §5Đạo Ngân. Tiến độ: "
                        + (int)data.dialinhQuestProgress + "/" + (int)data.dialinhQuestGoal));
                if (data.isQuestComplete()) completeChapNiem(sp, data);
                return InteractionResult.SUCCESS;
            }
        }

        // Nộp TN nếu đang quest Tiên Nguyên và cầm TN trong tay
        if (!player.isShiftKeyDown() && data.dialinhQuestType == CoTienData.QUEST_TIEN_NGUYEN && data.hasActiveQuest()) {
            ItemStack held = player.getItemInHand(hand);
            if (held.getItem() == CoTienItems.TIEN_NGUYEN.get() && held.getCount() > 0) {
                int remaining = (int)(data.dialinhQuestGoal - data.dialinhQuestProgress);
                int toSubmit = Math.min(held.getCount(), remaining);
                held.shrink(toSubmit);
                data.dialinhQuestProgress += toSubmit;
                sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                sp.sendSystemMessage(Component.literal("§b[Địa Linh] Nhận " + toSubmit + " Tiên Nguyên. Tiến độ: "
                        + (int)data.dialinhQuestProgress + "/" + (int)data.dialinhQuestGoal));
                if (data.isQuestComplete()) {
                    sp.sendSystemMessage(Component.literal("§a☯ Nhiệm vụ hoàn thành! [Shift+Click để nhận thưởng]"));
                }
                return InteractionResult.SUCCESS;
            }
        }

        if (player.isShiftKeyDown()) {
            handleQuestInteraction(sp, data, isOwner);
        } else {
            showInfo(sp, data, isOwner);
        }
        return InteractionResult.SUCCESS;
    }

    private void handleChapNiemInteraction(ServerPlayer sp) {
        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
        if (data.thangTienPhase < 4) {
            sp.sendSystemMessage(Component.literal("§5[Cô Hồn Địa Linh] §dChỉ Cổ Tiên mới có thể Chấp Niệm!"));
            return;
        }

        // Kiểm tra xem player đã có quest Chấp Niệm chưa
        if (data.hasActiveQuest() && data.dialinhQuestType == CoTienData.QUEST_CLAIM_ORPHANED) {
            // Đang làm quest → hiện tiến độ
            if (data.isQuestComplete()) {
                // Hoàn thành → sang tên chủ
                completeChapNiem(sp, data);
            } else {
                sp.sendSystemMessage(Component.literal(
                    "§5[Chấp Niệm] §dTiến độ: §f" + data.questDescription()));
                sp.sendSystemMessage(Component.literal("§7  Tiếp tục hoàn thành để nhận Phúc Địa này!"));
            }
            return;
        }

        // Giao quest Chấp Niệm ngẫu nhiên
        int questType = level().random.nextInt(3); // 0=kill, 1=TN, 2=Đạo Ngân
        switch (questType) {
            case 0 -> {
                data.dialinhQuestType = CoTienData.QUEST_KILL_MOBS;
                data.dialinhQuestGoal = 20 + level().random.nextInt(30);
                data.dialinhQuestRewardTN = 0;
            }
            case 1 -> {
                data.dialinhQuestType = CoTienData.QUEST_TIEN_NGUYEN;
                data.dialinhQuestGoal = 100 + level().random.nextInt(200);
                data.dialinhQuestRewardTN = 0;
            }
            default -> {
                // Đạo Ngân quest — custom type stored as QUEST_CLAIM_ORPHANED
                data.dialinhQuestType = CoTienData.QUEST_CLAIM_ORPHANED;
                data.dialinhQuestGoal = 10 + level().random.nextInt(15); // 10-25 Đạo Ngân
                data.dialinhQuestRewardTN = 0;
            }
        }
        data.dialinhQuestProgress = 0;
        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
        sp.sendSystemMessage(Component.literal("§5[Cô Hồn Địa Linh] §dTa sẽ thử thách ngươi:"));
        sp.sendSystemMessage(Component.literal("§f  " + data.questDescription()));
        sp.sendSystemMessage(Component.literal("§7  Hoàn thành để nhận Phúc Địa này!"));
    }

    private void completeChapNiem(ServerPlayer sp, CoTienData data) {
        // Sang tên chủ
        data.phucDiaOwnerUUID = sp.getUUID().toString();
        data.dialinhQuestType = CoTienData.QUEST_NONE;
        data.dialinhQuestProgress = 0; data.dialinhQuestGoal = 0;
        data.hasDialinh = true;
        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);

        // Địa Linh nhận chủ mới
        this.ownerUUID = sp.getUUID().toString();
        setCustomName(Component.literal("§2☯ §aĐịa Linh §2☯"));
        setCustomNameVisible(true);

        sp.sendSystemMessage(Component.literal("§a§l✦ CHẤP NIỆM THÀNH CÔNG! §r§aPhúc Địa vô chủ đã thuộc về bạn!"));
        sp.sendSystemMessage(Component.literal("§7  Dùng §f/cotien dialinhname <tên> §7để đặt tên Địa Linh."));
        if (level() instanceof net.minecraft.server.level.ServerLevel sl) {
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.TOTEM_OF_UNDYING,
                    getX(), getEyeY(), getZ(), 30, 0.5, 0.5, 0.5, 0.3);
        }
        level().playSound(null, blockPosition(),
                net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                net.minecraft.sounds.SoundSource.PLAYERS, 1f, 1f);
    }

    private void showInfo(ServerPlayer sp, CoTienData data, boolean isOwner) {
        String displayName = data.dialinhCustomName.isEmpty() ? "Địa Linh" : data.dialinhCustomName;
        sp.sendSystemMessage(Component.literal("§2════ ☯ " + displayName + " ☯ ════"));
        if (isOwner) {
            String[] grades = {"", "Hạ đẳng", "Trung đẳng", "Thượng đẳng", "Siêu đẳng"};
            int g = Math.max(0, Math.min(4, data.phucDiaGrade));
            sp.sendSystemMessage(Component.literal("§a• Phúc Địa: §f" + (g>0?grades[g]:"?") + " §7(Cấp " + data.phucDiaLevel + ")"));
            // Hiển thị sức mạnh Địa Linh hiện tại
            double hp  = 100000.0 + data.phucDiaLevel * 10000.0 + data.dialinhSkillHp * 50000.0;
            double dmg = 40.0 + data.phucDiaLevel * 3.0 + data.productionLevel * 2.0 + data.dialinhSkillDamage * 20.0;
            sp.sendSystemMessage(Component.literal("§6• Sức mạnh Địa Linh: §fHP=" + String.format("%.0f", hp)
                    + " §7| §fDMG=" + String.format("%.0f", dmg)));
            sp.sendSystemMessage(Component.literal("§7  (Kỹ năng: DMG Lv" + data.dialinhSkillDamage + " | Sinh Lực Lv" + data.dialinhSkillHp + ")"));
            sp.sendSystemMessage(Component.literal("§b• Tiên Nguyên: §f" + String.format("%.1f", data.tienNguyen)));
            if (data.phucDiaDamagePenalty > 0.01) {
                sp.sendSystemMessage(Component.literal("§c• Tổn hại Phúc Địa: §f" + String.format("%.0f%%", data.phucDiaDamagePenalty*100)));
            }
            // Quest status
            if (data.isQuestComplete()) {
                sp.sendSystemMessage(Component.literal("§a• ✓ Nhiệm vụ hoàn thành! §7[Shift+Click để nhận thưởng]"));
            } else if (data.hasActiveQuest()) {
                sp.sendSystemMessage(Component.literal("§e• Nhiệm vụ: §f" + data.questDescription()));
                if (data.dialinhQuestType == CoTienData.QUEST_TIEN_NGUYEN) {
                    sp.sendSystemMessage(Component.literal("§7  [Cầm Tiên Nguyên + Click để nộp]"));
                }
            } else {
                sp.sendSystemMessage(Component.literal("§7• §7[Shift+Click để nhận nhiệm vụ]"));
            }
            // Bond status
            if (data.dialinhBondComplete) {
                sp.sendSystemMessage(Component.literal("§d• ✦ Đã nhận chủ — §7dùng §f/cotien dialinhname <tên> §7để đặt tên"));
            } else if (data.dialinhBondQuestCount > 0) {
                sp.sendSystemMessage(Component.literal("§d• Nhận chủ: §f" + data.dialinhBondQuestsDone + "/" + data.dialinhBondQuestCount));
            }
        } else {
            sp.sendSystemMessage(Component.literal("§7• Đây không phải Địa Linh của bạn."));
        }
        sp.sendSystemMessage(Component.literal("§2════════════════════"));
    }

    private void handleQuestInteraction(ServerPlayer sp, CoTienData data, boolean isOwner) {
        if (!isOwner) {
            sp.sendSystemMessage(Component.literal("§c[Địa Linh] Ngươi không phải chủ nhân của ta!"));
            return;
        }

        // === Thu thưởng ===
        if (data.isQuestComplete()) {
            int reward = data.dialinhQuestRewardTN;
            data.tienNguyen += reward;
            data.phucDiaDamagePenalty = Math.max(0, data.phucDiaDamagePenalty - 0.2);
            // Tiến bond quest
            if (data.dialinhBondQuestCount > 0) {
                data.dialinhBondQuestsDone++;
                if (data.dialinhBondQuestsDone >= data.dialinhBondQuestCount) {
                    data.dialinhBondComplete = true;
                    data.dialinhQuestType = CoTienData.QUEST_NONE;
                    data.dialinhQuestProgress = 0; data.dialinhQuestGoal = 0; data.dialinhQuestRewardTN = 0;
                    sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                    sp.sendSystemMessage(Component.literal("§a☯ Nhận §b" + reward + " §aTiên Nguyên."));
                    sp.sendSystemMessage(Component.literal("§d✦ TA ĐÃ NHẬN NGƯƠI LÀM CHỦ NHÂN! ✦"));
                    sp.sendSystemMessage(Component.literal("§d  Dùng §f/cotien dialinhname <tên> §dđể đặt tên cho ta."));
                    if (level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, getX(), getEyeY()+0.5, getZ(), 50, 0.6, 0.6, 0.6, 0.5);
                    }
                    return;
                }
                // Tự động tạo quest tiếp theo trong chuỗi
                data.dialinhQuestType = CoTienData.QUEST_NONE;
                sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                sp.sendSystemMessage(Component.literal("§a☯ Nhận §b" + reward + " §aTiên Nguyên. Còn " + (data.dialinhBondQuestCount - data.dialinhBondQuestsDone) + " nhiệm vụ nữa."));
                data.generateDialinhQuest(level().random, data.phucDiaGrade);
                sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                sp.sendSystemMessage(Component.literal("§e[Địa Linh] Nhiệm vụ mới: §f" + data.questDescription()));
                return;
            }
            // Quest thông thường (không phải bond)
            data.dialinhQuestType = CoTienData.QUEST_NONE;
            data.dialinhQuestProgress = 0; data.dialinhQuestGoal = 0; data.dialinhQuestRewardTN = 0;
            sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
            sp.sendSystemMessage(Component.literal("§a☯ Nhận §b" + reward + " §aTiên Nguyên."));
            return;
        }

        // === Tiến độ quest đang làm ===
        if (data.hasActiveQuest()) {
            long cur = System.currentTimeMillis();
            if (cur - lastQuestShowTime < 4000) {
                // Click lần 2 trong 4s → xác nhận bỏ quest
                if (data.tienNguyen >= 20) {
                    data.tienNguyen -= 20;
                } // bỏ miễn phí nếu không đủ TN
                data.dialinhQuestType = CoTienData.QUEST_NONE;
                data.dialinhQuestProgress = 0; data.dialinhQuestGoal = 0; data.dialinhQuestRewardTN = 0;
                if (data.dialinhBondQuestCount > 0) { data.dialinhBondQuestCount = 0; data.dialinhBondQuestsDone = 0; }
                sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                sp.sendSystemMessage(Component.literal("§c[Địa Linh] Nhiệm vụ bị bỏ! Shift+Click để nhận nhiệm vụ mới."));
                lastQuestShowTime = 0;
            } else {
                // Click lần 1 → hiện tiến độ
                lastQuestShowTime = cur;
                sp.sendSystemMessage(Component.literal("§e[Địa Linh] Tiến độ nhiệm vụ:"));
                sp.sendSystemMessage(Component.literal("§f  " + data.questDescription()));
                sp.sendSystemMessage(Component.literal("§7  [Shift+Click §clại trong 4s §7để §cbỏ nhiệm vụ§7]"));
            }
            return;
        }

        // === Không có quest ===
        // Nếu bond đã hoàn thành
        if (data.dialinhBondComplete) {
            // Tạo quest thông thường
            data.generateDialinhQuest(level().random, data.phucDiaGrade);
            sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
            String[] types = {"「Cổ Trùng」", "「Tiên Nguyên」", "「Thanh Trừ」"};
            sp.sendSystemMessage(Component.literal("§2[Địa Linh] §aNhiệm vụ " + types[data.dialinhQuestType] + ":"));
            sp.sendSystemMessage(Component.literal("§f  " + data.questDescription()));
            return;
        }

        // Bắt đầu bond quest chain
        if (data.dialinhBondQuestCount == 0) {
            int count = 1 + level().random.nextInt(3); // 1-3
            data.dialinhBondQuestCount = count;
            data.dialinhBondQuestsDone = 0;
            String[] dialogue = {
                "§2[Địa Linh] §aTa thấy ngươi có duyên với ta. Ta giao ngươi §e1 nhiệm vụ §ađể chứng minh.",
                "§2[Địa Linh] §aTa thấy ngươi rất cố gắng. Ta giao ngươi §e2 nhiệm vụ §ađể chứng minh.",
                "§2[Địa Linh] §cTên ma đầu nhà ngươi thật phiền! Ta sẽ cho ngươi §e3 nhiệm vụ §cđể chứng minh!"
            };
            sp.sendSystemMessage(Component.literal(dialogue[count - 1]));
            data.generateDialinhQuest(level().random, data.phucDiaGrade);
            sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
            sp.sendSystemMessage(Component.literal("§f  Nhiệm vụ 1/" + count + ": " + data.questDescription()));
        } else {
            // Đang trong bond chain nhưng không có quest (giữa 2 quest)
            data.generateDialinhQuest(level().random, data.phucDiaGrade);
            sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
            sp.sendSystemMessage(Component.literal("§e[Địa Linh] Nhiệm vụ " + (data.dialinhBondQuestsDone+1) + "/" + data.dialinhBondQuestCount + ": §f" + data.questDescription()));
        }
    }

    public boolean isOrphaned() { return "ORPHANED".equals(ownerUUID); }

    /** Tick cho Cô Hồn Địa Linh — tự duy trì Phúc Địa vô chủ */
    private void tickOrphaned() {
        if (!isOrphaned()) return;
        // Cập nhật tên mỗi 200 tick
        if (tickCount % 200 == 0) {
            setCustomName(Component.literal("§5☠ §dCô Hồn Địa Linh §5☠"));
            setCustomNameVisible(true);
        }
        // Phát ra particle ma quỷ mỗi 20 tick
        if (tickCount % 20 == 0 && level() instanceof net.minecraft.server.level.ServerLevel sl) {
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL,
                    getX(), getEyeY(), getZ(), 3, 0.3, 0.3, 0.3, 0.02);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;

        if (isOrphaned()) { tickOrphaned(); return; }

        // Cập nhật stats từ owner mỗi 1 phút (khi Phúc Địa được nâng cấp)
        if (tickCount % 1200 == 0) {
            for (var player : level().getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(8192))) {
                if (player.getUUID().toString().equals(ownerUUID)) {
                    CoTienData d = player.getData(CoTienAttachments.CO_TIEN_DATA.get());
                    updateStatsFromOwner(d);
                    break;
                }
            }
        }

        if (tickCount % 100 != 0) return;

        // Buff lân cận cho chủ nhân
        for (var player : level().getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(10))) {
            if (player.getUUID().toString().equals(ownerUUID)) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 40, 0, false, false));
            }
        }

        // Địa Linh luôn ở max HP (hurt() đã block damage, đây để xử lý edge cases)
        if (tickCount % 100 == 0 && getHealth() < getMaxHealth()) {
            setHealth(getMaxHealth());
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SKIN_INDEX, 0);
    }

    public int getSkinIndex() { return entityData.get(SKIN_INDEX); }
    public void setSkinIndex(int idx) { entityData.set(SKIN_INDEX, idx % SKIN_COUNT); }
    public String getOwnerUUID() { return ownerUUID; }
    public void setOwnerUUID(String uuid) { this.ownerUUID = uuid; }

    private void showStorageContents(ServerPlayer sp) {
        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("§2☯ [Địa Linh] Kho đồ:"));
        boolean empty = true;
        for (int i = 0; i < storage.getContainerSize(); i++) {
            net.minecraft.world.item.ItemStack stack = storage.getItem(i);
            if (!stack.isEmpty()) {
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "  §7[" + i + "] §f" + stack.getDisplayName().getString() + " §7x" + stack.getCount()));
                empty = false;
            }
        }
        if (empty) sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("  §7(Trống)"));
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SkinIndex", getSkinIndex());
        tag.putString("OwnerUUID", ownerUUID);
        tag.putInt("dialinhStorageLevel", storageLevel);
        CompoundTag stTag = new CompoundTag();
        for (int i = 0; i < storage.getContainerSize(); i++) {
            if (!storage.getItem(i).isEmpty())
                stTag.put("s" + i, storage.getItem(i).save(level().registryAccess()));
        }
        tag.put("storage", stTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setSkinIndex(tag.contains("SkinIndex") ? tag.getInt("SkinIndex") : 0);
        ownerUUID = tag.contains("OwnerUUID") ? tag.getString("OwnerUUID") : "";
        // Restore storage level first, then load items
        int savedLevel = tag.contains("dialinhStorageLevel") ? tag.getInt("dialinhStorageLevel") : 0;
        if (savedLevel > 0) {
            int targetSize = getSlotsForLevel(savedLevel);
            storage = new net.minecraft.world.SimpleContainer(targetSize);
            storageLevel = savedLevel;
        }
        if (tag.contains("storage")) {
            CompoundTag stTag = tag.getCompound("storage");
            for (int i = 0; i < storage.getContainerSize(); i++) {
                if (stTag.contains("s" + i))
                    storage.setItem(i, net.minecraft.world.item.ItemStack.parseOptional(level().registryAccess(), stTag.getCompound("s" + i)));
            }
        }
        if (!level().isClientSide()) equipWeapon();
    }

    @Override public boolean isPushable() { return false; }
    @Override public boolean isPickable() { return true; }
    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        // Cô Hồn bất tử
        if (isOrphaned()) return false;
        // Không nhận damage từ player (tránh bị giết nhầm)
        if (source.getEntity() instanceof Player) return false;
        // Không nhận damage từ void/fall/drown — NPC bất tử với môi trường
        if (source == damageSources().fellOutOfWorld()
                || source == damageSources().drown()
                || source == damageSources().fall()) return false;
        // Giảm sát thương nhận vào 80% (Địa Linh rất tanky)
        return super.hurt(source, amount * 0.2f);
    }
}
