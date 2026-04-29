package com.andyanh.cotienaddon.event;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.init.CoTienAttachments;
import com.andyanh.cotienaddon.init.CoTienBlocks;
import com.andyanh.cotienaddon.init.CoTienItems;
import com.andyanh.cotienaddon.system.PhucDiaManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;

import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = CoTienAddon.MODID)
public class PhucDiaEventHandler {

    private static final int WAVE_TICKS = 600; // 30 giây mỗi wave

    // Boss Bar per player
    private static final Map<UUID, ServerBossEvent> BOSS_BARS = new HashMap<>();

    private static void showBossBar(ServerPlayer sp, String text, BossEvent.BossBarColor color) {
        removeBossBar(sp);
        var bar = new ServerBossEvent(Component.literal(text), color, BossEvent.BossBarOverlay.NOTCHED_10);
        bar.addPlayer(sp);
        BOSS_BARS.put(sp.getUUID(), bar);
    }

    private static void updateBossBar(ServerPlayer sp, float progress) {
        var bar = BOSS_BARS.get(sp.getUUID());
        if (bar != null) bar.setProgress(Math.max(0f, Math.min(1f, progress)));
    }

    private static void removeBossBar(ServerPlayer sp) {
        var bar = BOSS_BARS.remove(sp.getUUID());
        if (bar != null) bar.removeAllPlayers();
    }

    // --- Tiên Nguyên + Thiên Kiếp/Địa Tai tick ---
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer sp)) return;
        if (!(player.level() instanceof ServerLevel sl)) return;

        CoTienData data = player.getData(CoTienAttachments.CO_TIEN_DATA.get());
        if (data.thangTienPhase < 4) return;

        var pd = player.getPersistentData();
        boolean inPhucDia = PhucDiaManager.isPhucDiaDimension(sl.dimension());

        // Xử lý event đang diễn ra (mỗi tick) — cả trong PhucDia lẫn khi đang Thăng Tiên phase 2
        if (inPhucDia || data.thangTienPhase == 2) {
            tickKiepEvent(sp, sl, pd, data);
        }

        // Cập nhật nameplate Danh Hiệu Tôn mỗi 200 tick (daode có thể thay đổi)
        if (data.thangTienPhase >= 4 && data.tonHieuEnabled && player.tickCount % 200 == 0) {
            applyTonHieuNameplate(sp, data);
        }

        // Mỗi giây
        if (player.tickCount % 20 == 0) {
            // Penalty decay
            if (data.phucDiaDamagePenalty > 0)
                data.phucDiaDamagePenalty = Math.max(0.0, data.phucDiaDamagePenalty - 0.00017);

            double rate = PhucDiaManager.getTienNguyenBaseRate(data.phucDiaGrade)
                          * data.getProductionMultiplier()
                          * (1.0 - data.phucDiaDamagePenalty);
            data.tienNguyen += rate;

            // Áp dụng hệ sinh thái (time + weather)
            if (inPhucDia) {
                applyEcosystem(sl, sp, data);
            }

            // Trigger Kiếp/Tai
            if (inPhucDia && !pd.contains("kiep_ticks") && !pd.contains("ditai_ticks")) {
                // Siêu đẳng: ngưỡng thấp hơn, nguy hiểm hơn
                double threshold = data.phucDiaGrade >= 4
                        ? Math.max(100.0, 200.0 - data.defenseLevel * 30.0)
                        : 500.0 - data.defenseLevel * 80.0;
                if (data.thienKhi >= threshold) {
                    startThienKiep(sp, data);
                    data.thienKhi = 0;
                } else if (data.diaKhi >= threshold) {
                    startDiaTai(sp, data);
                    data.diaKhi = 0;
                }
            }
            player.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
        }

        // Quặng hiếm auto-spawn (mỗi 10 phút) cho Thượng/Siêu đẳng
        if (inPhucDia && player.tickCount % 12000 == 0 && data.phucDiaGrade >= 3) {
            spawnRareOres(sl, sp, data);
        }

        // Nguyên Thạch spawn mỗi 10 phút cho mọi grade
        // Lần đầu vào Phúc Địa: spawn ngay sau 5 giây (tick 100)
        var pd2 = player.getPersistentData();
        if (inPhucDia) {
            if (!pd2.getBoolean("ore_spawned_once")) {
                if (player.tickCount % 20 == 0) {
                    long inTime = pd2.getLong("phuc_dia_enter_time");
                    if (inTime == 0) pd2.putLong("phuc_dia_enter_time", System.currentTimeMillis());
                    else if (System.currentTimeMillis() - inTime > 5000) {
                        spawnNguyenThach(sl, sp, data);
                        pd2.putBoolean("ore_spawned_once", true);
                        pd2.putLong("ore_last_spawn", System.currentTimeMillis());
                    }
                }
            } else if (player.tickCount % 20 == 0) {
                long lastSpawn = pd2.getLong("ore_last_spawn");
                if (System.currentTimeMillis() - lastSpawn > 600_000L) { // 10 phút
                    spawnNguyenThach(sl, sp, data);
                    pd2.putLong("ore_last_spawn", System.currentTimeMillis());
                }
            }
        } else {
            // Reset khi ra khỏi Phúc Địa
            pd2.remove("ore_spawned_once");
            pd2.remove("phuc_dia_enter_time");
        }
    }

    // Thiên Kiếp: 6 loại wave
    private static final String[][] KIEP_WAVE_NAMES = {
        {"§c☁ THIÊN KIẾP — Sét Tiên Lôi",           "Sét bão + Hào Điện Lang + Phantom"},
        {"§c☁ THIÊN KIẾP — Hỏa Thiên Nộ",           "Mưa lửa + Lôi Điện Lang + Blaze"},
        {"§c☁ THIÊN KIẾP — Vô Hình Ám Sát",         "Vex + Điện Lang + Xương Trắng ẩn giết"},
        {"§c☁ THIÊN KIẾP — Xương Trắng Cuồng Lôi",  "Bão sét + Wither Skeleton + Điện Hùng"},
        {"§c☁ THIÊN KIẾP — Hư Ảnh Tứ Thánh",        "Tứ Hư Ảnh Thần Thú + Phantom đại quân"},
        {"§c§l☁ THIÊN KIẾP — LÔI VƯƠNG GIÁNG THẾ",  "Long Quyển Phong + Lôi Điện Lang + bão sét cực đại"},
    };
    // Địa Tai: 6 loại wave — dùng mạnh thú Cổ Chân Nhân
    private static final String[][] DITAI_WAVE_NAMES = {
        {"§6⚡ ĐỊA TAI — Địa Chấn Huyết Sát",  "Nổ địa chấn + Hồng Hùng + Magma"},
        {"§6⚡ ĐỊA TAI — Dã Thú Bạo Nộ",       "Điện Hùng + Hội Hùng + Lôi Quán Đầu Lang"},
        {"§6⚡ ĐỊA TAI — Thú Vương Giáng Thế",  "Kim Nhân Vương Hổ + Tiểu Kim Nhân Vương Hổ"},
        {"§6⚡ ĐỊA TAI — Địa Long Thức Tỉnh",   "Thủy Long thức giấc + Địa chấn magma bùng phát"},
        {"§6⚡ ĐỊA TAI — Hỏa Thú Cuồng Bạo",    "Hỏa Nhãn Hùng + Liệt Diễm Hùng + Liêu Nguyên Hỏa Hùng"},
        {"§6§l⚡ ĐỊA TAI — VẠN THÚ GIÁNG LÂM",  "Ngũ Túc Điểu + Cự Xỉ Kim Ô + Thú Vương tối thượng"},
    };

    private static void tickKiepEvent(ServerPlayer sp, ServerLevel sl,
            net.minecraft.nbt.CompoundTag pd, CoTienData data) {
        // === Thiên Kiếp ===
        if (pd.contains("kiep_ticks")) {
            int ticks = pd.getInt("kiep_ticks") - 1;
            int totalWaves = pd.contains("kiep_waves") ? pd.getInt("kiep_waves") : 3;
            int totalTicks = totalWaves * WAVE_TICKS;

            if (ticks <= 0) {
                pd.remove("kiep_ticks"); pd.remove("kiep_waves");
                assessDamage(sp, sl, data, true);
                sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                return;
            }
            pd.putInt("kiep_ticks", ticks);

            int elapsed = totalTicks - ticks;
            int currentWave = elapsed / WAVE_TICKS;
            int waveElapsed = elapsed % WAVE_TICKS;
            int waveType = currentWave % KIEP_WAVE_NAMES.length;
            int defense = data.defenseLevel;

            // Cập nhật boss bar (progress = % còn lại)
            updateBossBar(sp, (float) ticks / totalTicks);

            // Thông báo đầu wave + 1% boss
            if (waveElapsed == 0) {
                sp.sendSystemMessage(Component.literal(
                    KIEP_WAVE_NAMES[waveType][0] + " §7(Wave " + (currentWave+1) + "/" + totalWaves + ")"));
                updateBossBar(sp, (float) ticks / totalTicks);
                trySpawnBoss(sl, sp);
            }

            // Sét mọi wave (tần suất tăng theo wave)
            int lightningInterval = Math.max(10, 40 - currentWave * 4);
            if (waveElapsed % lightningInterval == 0) {
                spawnLightning(sl, sp, 1 + currentWave / 3);
                if (defense < 5) sp.hurt(sl.damageSources().lightningBolt(), Math.max(0.5f, 2f - defense * 0.3f));
            }

            // Mưa lửa cho Hỏa Thiên Nộ (wave type 1) và boss wave
            if (waveType == 1 && waveElapsed % 15 == 0) {
                spawnFireballs(sl, sp, 2 + currentWave);
            }

            // Mob theo loại wave
            if (waveElapsed % 180 == 60) {
                int cW = currentWave;
                switch (waveType) {
                    case 0 -> { // Sét Tiên Lôi: Hào Điện Lang + Phantom
                        spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.HAO_DIAN_LANG.get(), 1+cW/2, 12);
                        spawnMob(sl, sp, EntityType.PHANTOM, 2+cW/3, 14);
                    }
                    case 1 -> { // Hỏa Thiên Nộ: Lôi Điện Lang + Blaze + mưa lửa
                        spawnFireballs(sl, sp, 4+cW);
                        spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.LEI_DIAN_LANG.get(), 1+cW/3, 10);
                        spawnMob(sl, sp, EntityType.BLAZE, 2+cW/2, 8);
                    }
                    case 2 -> { // Vô Hình Ám Sát: Vex + Điện Lang + Wither Skeleton
                        spawnMob(sl, sp, EntityType.VEX, 3+cW/2, 8);
                        spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.DIAN_LANG.get(), 2+cW/3, 10);
                        spawnMob(sl, sp, EntityType.WITHER_SKELETON, 1+cW/4, 10);
                    }
                    case 3 -> { // Xương Trắng Cuồng Lôi: Wither Skeleton + Điện Hùng + bão sét
                        spawnMob(sl, sp, EntityType.WITHER_SKELETON, 2+cW/2, 8);
                        spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.DIAN_XIONG.get(), 1+cW/3, 12);
                        spawnLightning(sl, sp, 3+cW);
                    }
                    case 4 -> { // Hư Ảnh Tứ Thánh: 4 loại Hư Ảnh + Phantom
                        spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.XU_YING_XIONG.get(), 1, 10);
                        spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.XU_YING_GUI.get(), 1, 10);
                        spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.XU_YING_MA.get(), 1, 10);
                        spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.XU_YING_NIU.get(), 1, 10);
                        spawnMob(sl, sp, EntityType.PHANTOM, 2+cW, 14);
                    }
                    case 5 -> { // Lôi Vương Giáng Thế: Long Quyển Phong + Lôi Điện Lang + bão sét cực
                        spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.LONG_JUAN_FENG.get(), 1+cW/4, 15);
                        spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.LEI_DIAN_LANG.get(), 2+cW/2, 10);
                        spawnLightning(sl, sp, 6+cW*2);
                        spawnFireballs(sl, sp, 2+cW);
                    }
                }
            }
        }

        // === Địa Tai ===
        if (pd.contains("ditai_ticks")) {
            int ticks = pd.getInt("ditai_ticks") - 1;
            int totalWaves = pd.contains("ditai_waves") ? pd.getInt("ditai_waves") : 3;
            int totalTicks = totalWaves * WAVE_TICKS;

            if (ticks <= 0) {
                pd.remove("ditai_ticks"); pd.remove("ditai_waves");
                assessDamage(sp, sl, data, false);
                sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                return;
            }
            pd.putInt("ditai_ticks", ticks);

            int elapsed = totalTicks - ticks;
            int currentWave = elapsed / WAVE_TICKS;
            int waveElapsed = elapsed % WAVE_TICKS;
            int waveType = currentWave % DITAI_WAVE_NAMES.length;
            int defense = data.defenseLevel;

            // Cập nhật boss bar
            updateBossBar(sp, (float) ticks / totalTicks);

            if (waveElapsed == 0) {
                sp.sendSystemMessage(Component.literal(
                    DITAI_WAVE_NAMES[waveType][0] + " §7(Wave " + (currentWave+1) + "/" + totalWaves + ")"));
                trySpawnBoss(sl, sp);
            }

            // Địa chấn + magma dưới chân
            int shakeInterval = Math.max(30, 100 - currentWave * 15);
            if (waveElapsed % shakeInterval == 0) {
                if (defense < 4) {
                    float power = 0.8f + currentWave * 0.4f;
                    sl.explode(null, sp.getX()+(sl.random.nextDouble()-0.5)*6,
                            sp.getY()-1, sp.getZ()+(sl.random.nextDouble()-0.5)*6,
                            power, net.minecraft.world.level.Level.ExplosionInteraction.NONE);
                    sp.hurt(sl.damageSources().generic(), Math.max(0.5f, 2f - defense * 0.3f));
                }
                // Magma blocks dưới chân (1-3 block)
                spawnMagmaUnderFeet(sl, sp, 1 + currentWave / 2);
            }

            // Mob theo loại wave
            if (waveElapsed % 200 == 80) {
                int cW = currentWave;
                switch (waveType) {
                    case 0 -> { // Địa Chấn Huyết Sát: Hồng Hùng + Hùng + magma bổ sung
                        spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.HONG_XIONG.get(), 1+cW/2, 10);
                        spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.XIONG.get(), 2+cW/2, 8);
                        spawnMagmaUnderFeet(sl, sp, 2+cW);
                    }
                    case 1 -> { // Dã Thú Bạo Nộ: Điện Hùng + Hội Hùng + Lôi Quán Đầu Lang
                        spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.DIAN_XIONG.get(), 1+cW/3, 10);
                        spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.HUI_XIONG.get(), 2+cW/3, 8);
                        spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.LEI_GUAN_TOU_LANG.get(), 1+cW/3, 12);
                    }
                    case 2 -> { // Thú Vương Giáng Thế: Kim Nhân Vương Hổ boss
                        spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.JINRENWANGHU.get(), 1, 12);
                        spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.XIAO_JIN_REN_WANG_HU.get(), 2+cW/2, 10);
                        spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.HONG_XIONG.get(), 1+cW/2, 8);
                    }
                    case 3 -> { // Địa Long Thức Tỉnh: Thủy Long + địa chấn magma
                        spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.SHUI_LONG.get(), 1+cW/3, 12);
                        spawnMagmaUnderFeet(sl, sp, 3+cW);
                        spawnLightning(sl, sp, 2+cW/2); // "sấm long"
                    }
                    case 4 -> { // Hỏa Thú Cuồng Bạo: Hỏa Nhãn Hùng + Liệt Diễm Hùng + Liêu Nguyên Hỏa Hùng
                        spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.HUOYANXIONG.get(), 2+cW/2, 8);
                        spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.LIEYANXIONG.get(), 1+cW/3, 10);
                        spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.LIAOYUANHUOXIONG.get(), 1, 12);
                    }
                    case 5 -> { // Vạn Thú Giáng Lâm: Ngũ Túc Điểu + Cự Xỉ Kim Ô (cực mạnh)
                        if (data.phucDiaGrade >= 4 || data.thangTienPhase == 2) {
                            spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.WU_ZU_NIAO.get(), 1, 15);
                            spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.JU_CHI_JIN_WU_3.get(), 1+cW/4, 12);
                        } else {
                            // Grade thấp: fallback Thú Vương
                            spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.JINRENWANGHU.get(), 1, 12);
                            spawnMob(sl, sp, net.guzhenren.init.GuzhenrenModEntities.SHUI_LONG.get(), 1, 12);
                        }
                    }
                }
            }
        }
    }

    private static void spawnLightning(ServerLevel sl, ServerPlayer sp, int count) {
        for (int i = 0; i < count; i++) {
            double ox = (sl.random.nextDouble() - 0.5) * 24;
            double oz = (sl.random.nextDouble() - 0.5) * 24;
            var bolt = EntityType.LIGHTNING_BOLT.create(sl);
            if (bolt != null) {
                bolt.setPos(sp.getX()+ox, sp.getY(), sp.getZ()+oz);
                sl.addFreshEntity(bolt);
            }
        }
    }

    private static void spawnMob(ServerLevel sl, ServerPlayer sp,
            net.minecraft.world.entity.EntityType<?> type, int count, double radius) {
        for (int i = 0; i < count; i++) {
            double ox = (sl.random.nextDouble() - 0.5) * radius * 2;
            double oz = (sl.random.nextDouble() - 0.5) * radius * 2;
            int y = sl.getHeight(Heightmap.Types.WORLD_SURFACE, (int)(sp.getX()+ox), (int)(sp.getZ()+oz));
            var mob = type.create(sl);
            if (mob != null) {
                mob.setPos(sp.getX()+ox, type == EntityType.PHANTOM ? sp.getY()+8 : y, sp.getZ()+oz);
                mob.getPersistentData().putBoolean("cotien_spawned", true);
                if (mob instanceof net.minecraft.world.entity.Mob m) m.setTarget(sp);
                sl.addFreshEntity(mob);
            }
        }
    }

    private static void spawnFireballs(ServerLevel sl, ServerPlayer sp, int count) {
        for (int i = 0; i < count; i++) {
            double ox = (sl.random.nextDouble() - 0.5) * 30;
            double oz = (sl.random.nextDouble() - 0.5) * 30;
            double startY = sp.getY() + 20 + sl.random.nextDouble() * 10;
            var fireball = new SmallFireball(sl,
                    sp.getX() + ox, startY, sp.getZ() + oz,
                    new Vec3(0, -1, 0));
            fireball.setOwner(null);
            sl.addFreshEntity(fireball);
        }
    }

    private static void spawnMagmaUnderFeet(ServerLevel sl, ServerPlayer sp, int count) {
        for (int i = 0; i < count; i++) {
            double ox = (sl.random.nextDouble() - 0.5) * 6;
            double oz = (sl.random.nextDouble() - 0.5) * 6;
            var pos = BlockPos.containing(sp.getX() + ox, sp.getY() - 1, sp.getZ() + oz);
            // Chỉ đặt nếu là đất/đá (không phá hủy quá nhiều)
            var existing = sl.getBlockState(pos).getBlock();
            if (existing == Blocks.GRASS_BLOCK || existing == Blocks.DIRT
                    || existing == Blocks.STONE || existing == Blocks.DEEPSLATE) {
                sl.setBlock(pos, Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void trySpawnBoss(ServerLevel sl, ServerPlayer sp) {
        if (sl.random.nextFloat() > 0.01f) return; // 1% chance
        net.minecraft.world.entity.EntityType<?>[] bossPool = {
            net.guzhenren.init.GuzhenrenModEntities.SHI_KONG_ZHEN_YU_MANG.get(),
            net.guzhenren.init.GuzhenrenModEntities.ZHOU_SHI_LING_RAN.get(),
            net.guzhenren.init.GuzhenrenModEntities.YINYANGSHENGSIHE.get(),
            net.guzhenren.init.GuzhenrenModEntities.XUEYINGXIONGHE.get(),
            net.guzhenren.init.GuzhenrenModEntities.LINGBOHE.get(),
        };
        var bossType = bossPool[sl.random.nextInt(bossPool.length)];
        double ox = (sl.random.nextDouble() - 0.5) * 20;
        double oz = (sl.random.nextDouble() - 0.5) * 20;
        int y = sl.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,
                (int)(sp.getX()+ox), (int)(sp.getZ()+oz));
        var boss = bossType.create(sl);
        if (boss != null) {
            boss.setPos(sp.getX()+ox, y, sp.getZ()+oz);
            boss.getPersistentData().putBoolean("cotien_spawned", true);
            if (boss instanceof net.minecraft.world.entity.Mob m) m.setTarget(sp);
            sl.addFreshEntity(boss);
            sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§c§l☆ VẠN THÚ VƯƠNG GIÁNG LÂM! §r§c(boss xuất hiện trong Kiếp/Tai)"));
            CoTienAddon.LOGGER.info("[PhucDia] Boss {} spawned for {}",
                    bossType.getDescriptionId(), sp.getName().getString());
        }
    }

    private static void assessDamage(ServerPlayer sp, ServerLevel sl, CoTienData data, boolean isKiep) {
        removeBossBar(sp);
        setThachNhanCombat(sp, sl, false);
        // Khi đang Thăng Tiên phase 2: Kiếp/Tai chỉ là thử thách, không ảnh hưởng Phúc Địa
        if (data.thangTienPhase == 2) {
            sp.sendSystemMessage(Component.literal(
                "§a✦ Vượt qua Thiên Kiếp Thăng Tiên! Tiếp tục ngưng tụ Thiên Địa Khí..."));
            return;
        }

        // Đếm quái hostile còn sống trong bán kính 200 block
        var aabb = sp.getBoundingBox().inflate(200);
        int survivors = sl.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, aabb,
                e -> e instanceof net.minecraft.world.entity.monster.Enemy && e.isAlive()).size();
        double penalty = Math.min(0.8, survivors * 0.05);
        data.phucDiaDamagePenalty = Math.min(1.0, data.phucDiaDamagePenalty + penalty);

        String eventName = isKiep ? "§c☁ Thiên Kiếp" : "§6⚡ Địa Tai";
        if (survivors == 0) {
            // WIN: tăng Đạo Ngân (liupai_*dao) theo loại Kiếp/Tai
            var gv = sp.getData(net.guzhenren.network.GuzhenrenModVariables.PLAYER_VARIABLES);
            double daoBonus = 100.0 + data.phucDiaGrade * 80.0; // 180-420 mỗi lần thắng
            String daoName;
            if (isKiep) {
                gv.liupai_tiandao += daoBonus; // Thiên Kiếp → Thiên đạo
                daoName = "Thiên đạo";
            } else {
                gv.liupai_tudao += daoBonus;   // Địa Tai → Thổ đạo
                daoName = "Thổ đạo";
            }
            gv.markSyncDirty();
            sp.setData(net.guzhenren.network.GuzhenrenModVariables.PLAYER_VARIABLES, gv);

            // Bonus: Tiên Cổ item cho Thượng/Siêu đẳng
            boolean getTienCo = sl.random.nextFloat() < data.phucDiaGrade * 0.15f;
            if (getTienCo) sp.getInventory().add(new ItemStack(CoTienItems.TIEN_CO.get(), 1));

            String reward = "§6+" + (int)daoBonus + " " + daoName + " Ngân" + (getTienCo ? " §a+§f1 Tiên Cổ §6✦" : "");
            sp.sendSystemMessage(Component.literal(
                    eventName + " §aqua đi — Phúc Địa bình yên! " + reward));
        } else {
            sp.sendSystemMessage(Component.literal(
                    eventName + " §cqua đi! §f" + survivors + " quái sống sót — Tiên Nguyên §c-"
                    + String.format("%.0f%%", penalty * 100) + " §7(phục hồi theo thời gian)"));
        }
        CoTienAddon.LOGGER.info("[PhucDia] {} ended for {} — survivors={}, penalty={}",
                isKiep ? "ThienKiep" : "DiaTai", sp.getName().getString(), survivors, penalty);
    }

    private static void setThachNhanCombat(ServerPlayer sp, ServerLevel sl, boolean combat) {
        var list = sl.getEntitiesOfClass(com.andyanh.cotienaddon.entity.ThachNhanEntity.class,
                sp.getBoundingBox().inflate(200),
                e -> sp.getUUID().toString().equals(e.getOwnerUUID()));
        for (var tn : list) {
            tn.setInCombat(combat);
            if (combat) {
                sp.sendSystemMessage(Component.literal("§8⚒ [Thạch Nhân] Chiến đấu bảo vệ Phúc Địa!"));
            }
        }
    }

    /** Thiên Kiếp khi Thăng Tiên — cường độ theo grade Phúc Địa */
    public static void startThangTienKiep(ServerPlayer sp, CoTienData data) {
        int grade = Math.max(1, data.phucDiaGrade);
        int waves = 2 + grade; // grade1=3, grade2=4, grade3=5, grade4=6 waves
        int totalTicks = waves * WAVE_TICKS;
        sp.getPersistentData().putInt("kiep_ticks", totalTicks);
        sp.getPersistentData().putInt("kiep_waves", waves);
        sp.sendSystemMessage(Component.literal(
            "§c§l⚡ ——— THIÊN KIẾP THĂNG TIÊN GIÁNG XUỐNG ——— §r§c(" + waves + " wave)"));
        sp.sendSystemMessage(Component.literal(
            "§7§o  Vượt qua để hoàn tất Thăng Tiên — thất bại sẽ cản trở Nạp Khí!"));
        showBossBar(sp, "§c☁ THIÊN KIẾP THĂNG TIÊN §7— " + waves + " wave", BossEvent.BossBarColor.RED);
        if (sp.level() instanceof ServerLevel sl) setThachNhanCombat(sp, sl, true);
        CoTienAddon.LOGGER.info("[ThangTien] Thien Kiep started for {} ({} waves)", sp.getName().getString(), waves);
    }

    public static void startThienKiep(ServerPlayer sp, CoTienData data) {
        int waves = data.phucDiaGrade >= 4
                ? 5 + sp.level().random.nextInt(11)
                : 3 + sp.level().random.nextInt(8);
        int totalTicks = waves * WAVE_TICKS;
        sp.getPersistentData().putInt("kiep_ticks", totalTicks);
        sp.getPersistentData().putInt("kiep_waves", waves);
        String hardcore = data.phucDiaGrade >= 4 ? " §c§l[HARDCORE]" : "";
        sp.sendSystemMessage(Component.literal(
            "§c§l☁ ——— THIÊN KIẾP GIÁNG XUỐNG ——— §r§c(" + waves + " wave, " + (waves*30) + "s)" + hardcore));
        showBossBar(sp, "§c☁ THIÊN KIẾP §7— " + waves + " wave", BossEvent.BossBarColor.RED);
        if (sp.level() instanceof ServerLevel sl) setThachNhanCombat(sp, sl, true);
        CoTienAddon.LOGGER.info("[PhucDia] Thien Kiep started for {} ({} waves, grade={})",
                sp.getName().getString(), waves, data.phucDiaGrade);
    }

    public static void startDiaTai(ServerPlayer sp, CoTienData data) {
        int waves = data.phucDiaGrade >= 4
                ? 3 + sp.level().random.nextInt(8)
                : 1 + sp.level().random.nextInt(5);
        int totalTicks = waves * WAVE_TICKS;
        sp.getPersistentData().putInt("ditai_ticks", totalTicks);
        sp.getPersistentData().putInt("ditai_waves", waves);
        String hardcore = data.phucDiaGrade >= 4 ? " §6§l[HARDCORE]" : "";
        sp.sendSystemMessage(Component.literal(
            "§6§l⚡ ——— ĐỊA TAI BÙNG PHÁT ——— §r§6(" + waves + " wave, " + (waves*30) + "s)" + hardcore));
        showBossBar(sp, "§6⚡ ĐỊA TAI §7— " + waves + " wave", BossEvent.BossBarColor.YELLOW);
        if (sp.level() instanceof ServerLevel sl) setThachNhanCombat(sp, sl, true);
        CoTienAddon.LOGGER.info("[PhucDia] Dia Tai started for {} ({} waves, grade={})",
                sp.getName().getString(), waves, data.phucDiaGrade);
    }

    private static void applyEcosystem(ServerLevel sl, ServerPlayer sp, CoTienData data) {
        // Cố định thời gian = trưa (6000)
        if (data.ecoFixedDay) {
            sl.setDayTime(6000);
        }
        // Không cho mưa
        if (!data.ecoAllowRain && sl.isRaining()) {
            sl.setWeatherParameters(6000, 0, false, false);
        }
        // Dọn dẹp quái vi phạm (mỗi tick có xác suất dọn dẹp toàn bộ slot)
        if (sl.random.nextFloat() < 0.1f) {
            var allMobs = sl.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, 
                new net.minecraft.world.phys.AABB(
                    data.phucDiaSlot * 8192.0 - 50, -64, -50, 
                    data.phucDiaSlot * 8192.0 + 8192 + 50, 320, 8192 + 50));
            for (var m : allMobs) {
                if (m.getPersistentData().getBoolean("cotien_spawned")) continue;
                if (m instanceof com.andyanh.cotienaddon.entity.ThachNhanEntity) continue;
                if (m instanceof com.andyanh.cotienaddon.entity.DiaSinhEntity) continue;
                
                var loc = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(m.getType());
                boolean isGz = loc != null && "guzhenren".equals(loc.getNamespace());
                
                if (isGz) {
                    if (!data.ecoGuzhenrenMobs) m.discard();
                } else if (m instanceof net.minecraft.world.entity.monster.Monster || m instanceof net.minecraft.world.entity.monster.Enemy) {
                    m.discard(); // Hostile vanilla luôn bị dọn
                } else {
                    if (!data.ecoPeacefulMobs) m.discard();
                }
            }
        }

        // Vùng an toàn 32 block xung quanh player: dọn dẹp ngay lập tức mọi quái KHÔNG PHẢI đồng minh
        var safeZoneMobs = sl.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, 
            sp.getBoundingBox().inflate(32));
        for (var m : safeZoneMobs) {
            if (m.getPersistentData().getBoolean("cotien_spawned")) continue;
            if (m instanceof com.andyanh.cotienaddon.entity.ThachNhanEntity) continue;
            if (m instanceof com.andyanh.cotienaddon.entity.DiaSinhEntity) continue;
            
            var loc = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(m.getType());
            boolean isGz = loc != null && "guzhenren".equals(loc.getNamespace());
            
            // Trong vùng an toàn: nếu hòa bình (ecoGuzhenrenMobs=false) thì dọn sạch quái GZ
            if (isGz && !data.ecoGuzhenrenMobs) m.discard();
            // Quái vanilla luôn bị dọn trong vùng an toàn
            if (m instanceof net.minecraft.world.entity.monster.Monster || m instanceof net.minecraft.world.entity.monster.Enemy) m.discard();
        }
    }

    private static void spawnRareOres(ServerLevel sl, ServerPlayer sp, CoTienData data) {
        int slot = data.phucDiaSlot;
        double cx = slot * 8192.0 + 4096.0;
        double cz = 4096.0;
        double radius = Math.min(PhucDiaManager.getBorderRadius(data.phucDiaGrade), 100.0);
        int oreCount = data.phucDiaGrade >= 4 ? 5 : 3;

        net.minecraft.world.level.block.Block[] grade3Ores = {
            net.minecraft.world.level.block.Blocks.DIAMOND_ORE,
            net.minecraft.world.level.block.Blocks.EMERALD_ORE,
            net.minecraft.world.level.block.Blocks.GOLD_ORE,
        };
        net.minecraft.world.level.block.Block[] grade4Ores = {
            net.minecraft.world.level.block.Blocks.DIAMOND_ORE,
            net.minecraft.world.level.block.Blocks.EMERALD_ORE,
            net.minecraft.world.level.block.Blocks.ANCIENT_DEBRIS,
            net.minecraft.world.level.block.Blocks.GOLD_ORE,
        };
        var ores = data.phucDiaGrade >= 4 ? grade4Ores : grade3Ores;

        int spawned = 0;
        for (int i = 0; i < oreCount * 10 && spawned < oreCount; i++) {
            int ox = (int)((sl.random.nextDouble() - 0.5) * radius * 2 + cx);
            int oz = (int)((sl.random.nextDouble() - 0.5) * radius * 2 + cz);
            int oy = 5 + sl.random.nextInt(25); // y=5-30, underground
            var pos = new BlockPos(ox, oy, oz);
            if (sl.getBlockState(pos).isAir() || sl.getBlockState(pos).getBlock()
                    == net.minecraft.world.level.block.Blocks.STONE) {
                var ore = ores[sl.random.nextInt(ores.length)];
                sl.setBlock(pos, ore.defaultBlockState(), 3);
                spawned++;
            }
        }
        if (spawned > 0) {
            sp.sendSystemMessage(Component.literal("§6[Phúc Địa] §f" + spawned + " quặng hiếm xuất hiện trong lòng đất!"));
        }
    }

    public static void spawnNguyenThach(ServerLevel sl, ServerPlayer sp, CoTienData data) {
        int slot = data.phucDiaSlot;
        double cx = slot * 8192.0 + 4096.0;
        double cz = 4096.0;
        double radius = Math.min(PhucDiaManager.getBorderRadius(data.phucDiaGrade), 80.0);

        // Số vein Nguyên Thạch (mod ore) theo grade: 3/6/10/16
        int veinCount = switch (data.phucDiaGrade) {
            case 1 -> 3;
            case 2 -> 6;
            case 3 -> 10;
            default -> 16;
        };
        // Tỉ lệ Khối Tiên Nguyên: 2-5% tùy grade
        float khoiTienNguyenChance = 0.01f + data.phucDiaGrade * 0.01f; // 2%/3%/4%/5%

        var modOreBlock = net.guzhenren.init.GuzhenrenModBlocks.FANGKUAIYUANSHIYUANKUANG.get();
        var stone = net.minecraft.world.level.block.Blocks.STONE;

        int spawnedOre = 0, spawnedKhoi = 0;

        for (int v = 0; v < veinCount * 20 && spawnedOre < veinCount; v++) {
            int ox = (int)((sl.random.nextDouble() - 0.5) * radius * 2 + cx);
            int oz = (int)((sl.random.nextDouble() - 0.5) * radius * 2 + cz);
            int oy = 5 + sl.random.nextInt(40); // Y=5..44 (stone layer)

            var center = new BlockPos(ox, oy, oz);
            if (!sl.getBlockState(center).is(stone)) continue;

            // Vein Nguyên Thạch 3-5 block
            int veinSize = 3 + sl.random.nextInt(3);
            boolean placedAny = false;
            for (int i = 0; i < veinSize * 5 && veinSize > 0; i++) {
                int dx = sl.random.nextInt(3) - 1;
                int dy = sl.random.nextInt(3) - 1;
                int dz = sl.random.nextInt(3) - 1;
                var pos = center.offset(dx, dy, dz);
                if (!sl.getBlockState(pos).is(stone)) continue;

                // 2-5% Khối Tiên Nguyên, còn lại là quặng Nguyên Thạch
                if (sl.random.nextFloat() < khoiTienNguyenChance) {
                    sl.setBlock(pos, CoTienBlocks.KHOI_TIEN_NGUYEN.get().defaultBlockState(), 3);
                    spawnedKhoi++;
                } else {
                    sl.setBlock(pos, modOreBlock.defaultBlockState(), 3);
                }
                veinSize--;
                placedAny = true;
            }
            if (placedAny) spawnedOre++;
        }

        if (spawnedOre > 0) {
            String khoi = spawnedKhoi > 0 ? " §6[+" + spawnedKhoi + " Khối Tiên Nguyên!]" : "";
            sp.sendSystemMessage(Component.literal(
                "§5[Phúc Địa] §fNguyên Thạch khoáng mạch mới xuất hiện! (" + spawnedOre + " mạch)" + khoi));
        }
    }

    // --- Chặn mob spawn trong Phúc Địa qua EntityJoinLevelEvent ---
    @SubscribeEvent
    public static void onEntityJoin(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof ServerLevel sl)) return;
        if (!PhucDiaManager.isPhucDiaDimension(sl.dimension())) return;

        var entity = event.getEntity();

        // Khi player vào PhucDia: sync Địa Linh với CoTienData của owner (bắt kịp upgrade mua ngoài)
        if (entity instanceof ServerPlayer sp) {
            CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
            sl.getEntitiesOfClass(com.andyanh.cotienaddon.entity.DiaSinhEntity.class,
                    new net.minecraft.world.phys.AABB(-8192, -512, -8192, 8192, 512, 8192),
                    e -> sp.getUUID().toString().equals(e.getOwnerUUID()))
                .forEach(e -> e.updateStatsFromOwner(data));
            // fall through — player vẫn được xử lý bình thường
            return;
        }

        // Khi DiaSinhEntity load vào dimension, sync storage level từ owner data
        if (entity instanceof com.andyanh.cotienaddon.entity.DiaSinhEntity dialinh) {
            String ownerStr = dialinh.getOwnerUUID();
            if (!ownerStr.isEmpty()) {
                try {
                    ServerPlayer owner = sl.getServer().getPlayerList().getPlayer(java.util.UUID.fromString(ownerStr));
                    if (owner != null) {
                        dialinh.updateStatsFromOwner(owner.getData(CoTienAttachments.CO_TIEN_DATA.get()));
                    }
                } catch (IllegalArgumentException ignored) {}
            }
            return;
        }

        if (entity instanceof com.andyanh.cotienaddon.entity.ThachNhanEntity) return;
        // Luôn cho phép projectile, item, lightning, v.v.
        if (!(entity instanceof net.minecraft.world.entity.Mob mob)) return;

        // Cho phép mob từ Kiếp/Tai event (có target player)
        if (mob.getPersistentData().getBoolean("cotien_spawned")) return;

        // Kiểm tra namespace
        var entityLoc = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        boolean isGuzhenren = entityLoc != null && "guzhenren".equals(entityLoc.getNamespace());

        // Lấy setting từ owner
        boolean allowPeaceful = true;
        boolean allowGuzhenren = true;
        UUID ownerUUID = PhucDiaManager.findZoneOwner(sl.getServer(), entity.getX(), entity.getZ());
        if (ownerUUID != null) {
            ServerPlayer owner = sl.getServer().getPlayerList().getPlayer(ownerUUID);
            if (owner != null) {
                CoTienData ownerData = owner.getData(CoTienAttachments.CO_TIEN_DATA.get());
                allowPeaceful = ownerData.ecoPeacefulMobs;
                allowGuzhenren = ownerData.ecoGuzhenrenMobs;
            }
        }

        if (isGuzhenren) {
            if (!allowGuzhenren) event.setCanceled(true);
        } else if (mob instanceof net.minecraft.world.entity.monster.Enemy) {
            // Quái vật vanilla tự nhiên KHÔNG BAO GIỜ được spawn trong Phúc Địa
            event.setCanceled(true);
        } else {
            // Động vật hòa bình vanilla
            if (!allowPeaceful) event.setCanceled(true);
        }
    }

    // --- Zone boundary + particle border ---
    @SubscribeEvent
    public static void onPlayerTickZone(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer sp)) return;
        if (!(player.level() instanceof ServerLevel sl)) return;

        // Ép ranh giới mỗi tick (chặn cả fly nhanh)
        PhucDiaManager.enforceZoneBoundary(sp);

        // Hiển thị ranh giới mỗi 5 tick (0.25s) để bức tường mượt hơn
        if (player.tickCount % 5 == 0 && PhucDiaManager.isPhucDiaDimension(sl.dimension())) {
            CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
            if (data.thangTienPhase >= 4 && data.phucDiaLevel < 10) {
                // Chủ nhân → dùng data của mình
                spawnBorderParticles(sl, sp, data);
            } else {
                // Khách → tìm owner và dùng data của owner
                java.util.UUID ownerUUID = PhucDiaManager.findZoneOwner(sp.server, sp.getX(), sp.getZ());
                if (ownerUUID != null && !ownerUUID.equals(sp.getUUID())) {
                    net.minecraft.server.level.ServerPlayer owner =
                            sp.server.getPlayerList().getPlayer(ownerUUID);
                    if (owner != null) {
                        CoTienData ownerData = owner.getData(CoTienAttachments.CO_TIEN_DATA.get());
                        if (ownerData.thangTienPhase >= 4 && ownerData.phucDiaLevel < 10) {
                            spawnBorderParticles(sl, sp, ownerData);
                        }
                    }
                }
            }
        }
    }

    private static final DustParticleOptions BORDER_DUST =
            new DustParticleOptions(new Vector3f(0.0f, 1.0f, 0.9f), 4.0f); // Xanh cyan cực sáng, size 4.0

    private static void spawnBorderParticles(ServerLevel sl, ServerPlayer sp, CoTienData data) {
        int slot = data.phucDiaSlot;
        double radius = PhucDiaManager.getBorderRadius(data.phucDiaGrade);
        double cx = slot * 8192.0 + 4096.0;
        double cz = 4096.0;
        double px = sp.getX(), pz = sp.getZ();
        double py = sp.getY();
        double viewDist = 32.0; // Khoảng cách nhìn thấy ranh giới

        int step = 1; // particle mỗi 1 block ngang (dày đặc hơn)

        // Vẽ bức tường dày hơn: 15 lớp từ chân đến đầu
        for (double yOffset = -2; yOffset <= 12; yOffset += 1.0) {
            double currentY = py + yOffset;
            
            // Cạnh Bắc (z = cz - radius)
            double northZ = cz - radius;
            if (Math.abs(northZ - pz) < viewDist) {
                for (double x = cx - radius; x <= cx + radius; x += step) {
                    if (Math.abs(x - px) < viewDist)
                        sl.sendParticles(sp, BORDER_DUST, true, x, currentY, northZ, 1, 0, 0, 0, 0);
                }
            }
            // Cạnh Nam (z = cz + radius)
            double southZ = cz + radius;
            if (Math.abs(southZ - pz) < viewDist) {
                for (double x = cx - radius; x <= cx + radius; x += step) {
                    if (Math.abs(x - px) < viewDist)
                        sl.sendParticles(sp, BORDER_DUST, true, x, currentY, southZ, 1, 0, 0, 0, 0);
                }
            }
            // Cạnh Tây (x = cx - radius)
            double westX = cx - radius;
            if (Math.abs(westX - px) < viewDist) {
                for (double z = cz - radius; z <= cz + radius; z += step) {
                    if (Math.abs(z - pz) < viewDist)
                        sl.sendParticles(sp, BORDER_DUST, true, westX, currentY, z, 1, 0, 0, 0, 0);
                }
            }
            // Cạnh Đông (x = cx + radius)
            double eastX = cx + radius;
            if (Math.abs(eastX - px) < viewDist) {
                for (double z = cz - radius; z <= cz + radius; z += step) {
                    if (Math.abs(z - pz) < viewDist)
                        sl.sendParticles(sp, BORDER_DUST, true, eastX, currentY, z, 1, 0, 0, 0, 0);
                }
            }
        }
    }

    // --- Tiên Đài: đặt xuống → đăng ký waypoint, đập → xóa waypoint ---
    @SubscribeEvent
    public static void onTienDaiPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (event.getLevel().isClientSide()) return;
        if (!event.getPlacedBlock().is(CoTienBlocks.TIEN_DAI.get())) return;

        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
        if (data.savedLocations.size() >= CoTienData.MAX_SAVED_LOCATIONS) {
            sp.sendSystemMessage(Component.literal("§c[Tiên Đài] Đã đạt tối đa 10 địa điểm!"));
            return;
        }

        var pos = event.getPos();
        String dimId = event.getLevel().dimensionType() != null
                ? ((ServerLevel) event.getLevel()).dimension().location().toString()
                : "minecraft:overworld";
        String name = "Tiên Đài #" + (data.savedLocations.size() + 1);
        data.savedLocations.add(new CoTienData.SavedLocation(name, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, dimId));
        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);

        // Sync to client
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp,
                new com.andyanh.cotienaddon.network.SyncCoTienPacket(
                        com.andyanh.cotienaddon.init.CoTienNetwork.buildSyncNBT(sp)));

        sp.sendSystemMessage(Component.literal("§b✦ [Tiên Đài] §f\"" + name + "\" §bđã được đăng ký! Mở Định Tiên Du (P2) để dịch chuyển."));
    }

    @SubscribeEvent
    public static void onTienDaiBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer sp)) return;
        if (!event.getState().is(CoTienBlocks.TIEN_DAI.get())) return;

        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
        var pos = event.getPos();
        // Xóa waypoint gần nhất với vị trí này
        data.savedLocations.removeIf(loc ->
                Math.abs(loc.x - pos.getX() - 0.5) < 1.5 &&
                Math.abs(loc.z - pos.getZ() - 0.5) < 1.5 &&
                Math.abs(loc.y - pos.getY() - 1) < 2.0);
        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);

        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp,
                new com.andyanh.cotienaddon.network.SyncCoTienPacket(
                        com.andyanh.cotienaddon.init.CoTienNetwork.buildSyncNBT(sp)));

        sp.sendSystemMessage(Component.literal("§7[Tiên Đài] Waypoint đã bị xóa."));
    }

    // --- Block break: kiểm tra PERM_BUILD ---
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (!PhucDiaManager.isPhucDiaDimension(serverLevel.dimension())) return;
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
        if (!PhucDiaManager.isPhucDiaDimension(serverLevel.dimension())) return;
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
        if (!PhucDiaManager.isPhucDiaDimension(serverLevel.dimension())) return;

        // Kiểm tra xem block có phải container không
        var state = serverLevel.getBlockState(event.getPos());
        if (state.getMenuProvider(serverLevel, event.getPos()) == null) return;

        if (!checkPermission(player, player.getX(), player.getZ(), CoTienData.PERM_CONTAINERS)) {
            event.setCanceled(true);
        }
    }

    // --- Thiên Kiếp trong Phúc Địa (legacy single-trigger, thay bằng startThienKiep) ---
    @Deprecated
    public static void triggerThienKiep(ServerPlayer sp, CoTienData data) {
        if (!(sp.level() instanceof ServerLevel sl)) return;
        sp.sendSystemMessage(Component.literal("§c☁ THIÊN KIẾP GIÁNG XUỐNG — Phúc Địa chấn động!"));

        // Giảm damage nếu có defenseLevel
        int mobCount = Math.max(1, 3 - data.defenseLevel / 2);
        double damage = Math.max(2.0, 8.0 - data.defenseLevel * 1.0);

        // Sét đánh xung quanh
        for (int i = 0; i < 3; i++) {
            double ox = (sl.random.nextDouble() - 0.5) * 20;
            double oz = (sl.random.nextDouble() - 0.5) * 20;
            BlockPos pos = BlockPos.containing(sp.getX() + ox, sp.getY(), sp.getZ() + oz);
            var bolt = EntityType.LIGHTNING_BOLT.create(sl);
            if (bolt != null) { bolt.setPos(pos.getX(), pos.getY(), pos.getZ()); sl.addFreshEntity(bolt); }
        }

        // Spawn 1-2 Thiên mob (Phantom — bay trên không, phù hợp Thiên Kiếp)
        for (int i = 0; i < Math.min(mobCount, 2); i++) {
            double ox = (sl.random.nextDouble() - 0.5) * 16;
            double oz = (sl.random.nextDouble() - 0.5) * 16;
            var mob = EntityType.PHANTOM.create(sl);
            if (mob != null) {
                mob.setPos(sp.getX()+ox, sp.getY()+8, sp.getZ()+oz);
                mob.setTarget(sp);
                sl.addFreshEntity(mob);
            }
        }

        // Damage trực tiếp
        sp.hurt(sl.damageSources().lightningBolt(), (float) damage);
        CoTienAddon.LOGGER.info("[PhucDia] Thien Kiep triggered for {} (thienKhi={}, defense={})",
                sp.getName().getString(), data.thienKhi, data.defenseLevel);
    }

    // --- Địa Tai trong Phúc Địa ---
    public static void triggerDiaTai(ServerPlayer sp, CoTienData data) {
        if (!(sp.level() instanceof ServerLevel sl)) return;
        sp.sendSystemMessage(Component.literal("§6⚡ ĐỊA TAI BÙNG PHÁT — Lòng đất chuyển động!"));

        int mobCount = Math.max(1, 3 - data.defenseLevel / 2);
        double damage = Math.max(2.0, 6.0 - data.defenseLevel * 0.8);

        // Spawn Địa mob
        // Spawn 1-2 Địa mob (Ravager — mạnh, thể hiện Địa Tai)
        for (int i = 0; i < Math.min(mobCount, 2); i++) {
            double ox = (sl.random.nextDouble() - 0.5) * 10;
            double oz = (sl.random.nextDouble() - 0.5) * 10;
            int y = sl.getHeight(Heightmap.Types.WORLD_SURFACE, (int)(sp.getX()+ox), (int)(sp.getZ()+oz));
            var mob = EntityType.RAVAGER.create(sl);
            if (mob != null) {
                mob.setPos(sp.getX()+ox, y, sp.getZ()+oz);
                mob.setTarget(sp);
                sl.addFreshEntity(mob);
            }
        }

        // Địa chấn nhẹ
        if (data.defenseLevel < 3) {
            sl.explode(null, sp.getX(), sp.getY() - 1, sp.getZ(), 1.5f,
                    net.minecraft.world.level.Level.ExplosionInteraction.NONE);
        }

        sp.hurt(sl.damageSources().generic(), (float) damage);
        CoTienAddon.LOGGER.info("[PhucDia] Dia Tai triggered for {} (diaKhi={}, defense={})",
                sp.getName().getString(), data.diaKhi, data.defenseLevel);
    }

    // --- Theo dõi kill quái cho quest Địa Linh (QUEST_KILL_MOBS) — bất kỳ dimension ---
    @SubscribeEvent
    public static void onMobDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.world.entity.monster.Enemy)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer sp)) return;

        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
        if (data.dialinhQuestType != CoTienData.QUEST_KILL_MOBS || data.isQuestComplete()) return;

        data.dialinhQuestProgress++;
        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
        if (data.isQuestComplete()) {
            sp.sendSystemMessage(Component.literal("§a☯ Địa Linh hài lòng — nhiệm vụ hoàn thành! [Shift+Click Địa Linh để nhận thưởng]"));
        } else if ((int)data.dialinhQuestProgress % 5 == 0) {
            sp.sendSystemMessage(Component.literal("§e[Địa Linh] Tiến độ: " + (int)data.dialinhQuestProgress + "/" + (int)data.dialinhQuestGoal));
        }
    }

    // --- PlayerEvent.Clone: xóa kiep/tai data trên entity MỚI sau respawn ---
    @SubscribeEvent
    public static void onPlayerClone(net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return; // chỉ xử lý khi chết, không phải đổi dimension
        var newPlayer = event.getEntity();
        var newPd = newPlayer.getPersistentData();

        // Xóa kiep/tai ticks từ entity mới (tránh boss bar hiện lại)
        newPd.remove("kiep_ticks");  newPd.remove("kiep_waves");
        newPd.remove("ditai_ticks"); newPd.remove("ditai_waves");
        removeBossBar((ServerPlayer) newPlayer);

        // Nếu đang Thăng Tiên phase 2 → reset tu vi trên entity mới
        if (!(newPlayer instanceof ServerPlayer sp)) return;
        if (newPd.getBoolean("need_tuvi_reset")) {
            newPd.remove("need_tuvi_reset");
            var gv = sp.getData(net.guzhenren.network.GuzhenrenModVariables.PLAYER_VARIABLES);
            gv.zhuanshu = 0; gv.jieduan = 0; gv.kongqiao = 0;
            gv.zhenyuan = 0; gv.zuida_zhenyuan = 0;
            gv.niantou  = 0; gv.niantou_rongliang = 0;
            gv.zuida_hunpo = 0; gv.gongjili = 0; gv.fangyuli = 0;
            gv.qiyun = 0; gv.renqi = 0; gv.benminggu = 0;
            gv.gushi_xiulian_dangqian = 0;
            gv.markSyncDirty();
            sp.setData(net.guzhenren.network.GuzhenrenModVariables.PLAYER_VARIABLES, gv);
            sp.setCustomName(null); sp.setCustomNameVisible(false);
            sp.sendSystemMessage(Component.literal(
                "§4§l✦ ĐỘ KIẾP THẤT BẠI — TU VI TAN VỠ! §r"));
            sp.sendSystemMessage(Component.literal(
                "§c  Thiên Đạo không dung! Toàn bộ tu vi quy về Không — phải bắt đầu lại từ đầu!"));
            CoTienAddon.LOGGER.warn("[ThangTien] {} respawned after Thien Kiep death — cultivation RESET",
                    sp.getName().getString());
        }
    }

    // --- Chết trong Kiếp/Tai → tụt cấp Phúc Địa ---
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (sp.level().isClientSide()) return;

        var pd = sp.getPersistentData();
        boolean inKiep  = pd.contains("kiep_ticks");
        boolean inDiaTai = pd.contains("ditai_ticks");
        if (!inKiep && !inDiaTai) return;

        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());

        // Xóa event
        pd.remove("kiep_ticks"); pd.remove("kiep_waves");
        pd.remove("ditai_ticks"); pd.remove("ditai_waves");
        removeBossBar(sp);
        if (sp.level() instanceof net.minecraft.server.level.ServerLevel sl)
            setThachNhanCombat(sp, sl, false);

        // === Cổ Tiên chết ngoài Kiếp/Tai → drop Orphaned Node ===
        if (!inKiep && !inDiaTai && data.thangTienPhase >= 4 && data.phucDiaSlot >= 0) {
            com.andyanh.cotienaddon.item.OrphanedNodeItem.create(data,
                    sp.getName().getString(), sp.getUUID().toString());
            var nodeStack = com.andyanh.cotienaddon.item.OrphanedNodeItem.create(
                    data, sp.getName().getString(), sp.getUUID().toString());
            sp.spawnAtLocation(nodeStack);
            sp.sendSystemMessage(Component.literal("§6§l✦ Phúc Địa vô chủ! Cô Hồn Tiên Địa Khế rơi xuống..."));
            // Mark Phúc Địa là orphaned
            data.phucDiaOwnerUUID = "ORPHANED";
            sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
            return;
        }

        // === Chết khi đang Thăng Tiên phase 2 → đánh dấu flag, reset thực sự sẽ xảy ra sau respawn ===
        if (data.thangTienPhase == 2) {
            // Reset CoTienData ngay bây giờ
            data.thangTienPhase = 0;
            data.thienKhi = 0; data.diaKhi = 0; data.napKhiTick = 0;
            data.nhanKhi  = 0;
            data.guUsed_tier1 = 0; data.guUsed_tier2 = 0; data.guUsed_tier3 = 0;
            data.guUsed_tier4 = 0; data.guUsed_tier5 = 0; data.guCrafted    = 0;
            sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
            // Đánh dấu để PlayerEvent.Clone reset Guzhenren vars trên entity mới
            pd.putBoolean("need_tuvi_reset", true);
            CoTienAddon.LOGGER.warn("[ThangTien] {} died in Thien Kiep — flagged for full cultivation RESET",
                    sp.getName().getString());
            return;
        }

        // === Chết trong Kiếp/Tai Phúc Địa thông thường → tụt grade ===
        if (data.phucDiaGrade > 1) {
            data.phucDiaGrade--;
            String eventName = inKiep ? "Thiên Kiếp" : "Địa Tai";
            sp.sendSystemMessage(Component.literal(
                "§4§l✦ THẤT BẠI! §r§cChết trong " + eventName
                + " — Phúc Địa tụt xuống " + getGradeName(data.phucDiaGrade) + "!"));
            sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
            CoTienAddon.LOGGER.info("[PhucDia] Player {} died in {} — grade reduced to {}",
                    sp.getName().getString(), eventName, data.phucDiaGrade);
        } else {
            double loss = data.tienNguyen * 0.5;
            data.tienNguyen = Math.max(0, data.tienNguyen - loss);
            String eventName = inKiep ? "Thiên Kiếp" : "Địa Tai";
            sp.sendSystemMessage(Component.literal(
                "§4§l✦ THẤT BẠI! §r§cChết trong " + eventName
                + " — mất §c" + String.format("%.0f", loss) + " §cTiên Nguyên!"));
            sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
        }
    }

    // Scoreboard team tự xử lý cả nameplate lẫn chat — không cần ServerChatEvent nữa

    /** Cập nhật nameplate + chat prefix qua Scoreboard Team (đúng cách trong multiplayer) */
    public static void applyTonHieuNameplate(ServerPlayer sp, CoTienData data) {
        net.minecraft.world.scores.Scoreboard sc = sp.server.getScoreboard();
        String teamKey = "cttm_" + sp.getStringUUID().substring(0, 8);
        String playerName = sp.getName().getString();

        // Xóa khỏi team cũ nếu có
        net.minecraft.world.scores.PlayerTeam oldTeam = sc.getPlayersTeam(playerName);
        if (oldTeam != null && oldTeam.getName().startsWith("cttm_")) {
            sc.removePlayerFromTeam(playerName, oldTeam);
        }

        if (!data.tonHieuEnabled || data.tonHieuName.isEmpty()) {
            // Xóa team nếu tồn tại
            net.minecraft.world.scores.PlayerTeam t = sc.getPlayerTeam(teamKey);
            if (t != null) sc.removePlayerTeam(t);
            return;
        }

        var gv = sp.getData(net.guzhenren.network.GuzhenrenModVariables.PLAYER_VARIABLES);
        String type = gv.daode >= 0 ? "Tiên Tôn" : "Ma Tôn";
        net.minecraft.network.chat.TextColor color =
                net.minecraft.network.chat.TextColor.fromRgb(data.tonHieuColor);

        // Tạo/cập nhật team
        net.minecraft.world.scores.PlayerTeam team = sc.getPlayerTeam(teamKey);
        if (team == null) team = sc.addPlayerTeam(teamKey);

        // Prefix hiển thị trước tên player (cả nameplate lẫn chat)
        net.minecraft.network.chat.MutableComponent prefix =
                Component.literal("✦ ")
                .withStyle(s -> s.withColor(color))
                .append(Component.literal(data.tonHieuName + " " + type + " §8| §r"));
        team.setPlayerPrefix(prefix);
        team.setNameTagVisibility(net.minecraft.world.scores.Team.Visibility.ALWAYS);
        team.setCollisionRule(net.minecraft.world.scores.Team.CollisionRule.ALWAYS);

        sc.addPlayerToTeam(playerName, team);
    }

    /** Chat prefix component cho Danh Hiệu Tôn */
    public static net.minecraft.network.chat.Component buildTonHieuChatPrefix(ServerPlayer sp, CoTienData data) {
        if (!data.tonHieuEnabled || data.tonHieuName.isEmpty()) return null;
        var gv = sp.getData(net.guzhenren.network.GuzhenrenModVariables.PLAYER_VARIABLES);
        String type = gv.daode >= 0 ? "Tiên Tôn" : "Ma Tôn";
        net.minecraft.network.chat.TextColor color =
                net.minecraft.network.chat.TextColor.fromRgb(data.tonHieuColor);
        return Component.literal("[" + data.tonHieuName + " " + type + "] ")
                .withStyle(s -> s.withColor(color));
    }

    private static String getGradeName(int grade) {
        return switch (grade) {
            case 1 -> "§7Hạ đẳng";
            case 2 -> "§aTriung đẳng";
            case 3 -> "§bThượng đẳng";
            case 4 -> "§6Siêu đẳng";
            default -> "§7Không xác định";
        };
    }

    // --- Cảnh báo khi Thiên/Địa Khí gần ngưỡng ---
    private static final java.util.Map<java.util.UUID, Long> lastWarnTime = new java.util.HashMap<>();

    @SubscribeEvent
    public static void onPlayerTickWarning(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer sp)) return;
        if (!(player.level() instanceof ServerLevel sl)) return;
        if (player.tickCount % 200 != 0) return; // mỗi 10 giây

        if (!PhucDiaManager.isPhucDiaDimension(sl.dimension())) return;

        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
        if (data.thangTienPhase < 4) return;

        double threshold = 500.0 - data.defenseLevel * 80.0;
        long now = System.currentTimeMillis();
        UUID uid = sp.getUUID();

        // Cảnh báo khi Khí đạt 80% ngưỡng
        if ((data.thienKhi >= threshold * 0.8 || data.diaKhi >= threshold * 0.8)
                && now - lastWarnTime.getOrDefault(uid, 0L) > 30000) {
            lastWarnTime.put(uid, now);

            String which = data.thienKhi >= threshold * 0.8 ? "Thiên Khí" : "Địa Khí";
            double val = data.thienKhi >= threshold * 0.8 ? data.thienKhi : data.diaKhi;
            // Địa Linh nói cảnh báo
            sp.sendSystemMessage(Component.literal("§2[地靈] §aNguy! " + which + " đang tích tụ ("
                    + String.format("%.0f", val) + "/" + String.format("%.0f", threshold)
                    + ") — hãy chuẩn bị cho Kiếp/Tai!"));
        }

        // Thông báo tiến độ quest ngẫu nhiên (10% mỗi 10s)
        if (data.hasActiveQuest() && sl.random.nextFloat() < 0.1f) {
            String[] nudge = {
                "Tiến độ nhiệm vụ: " + data.questDescription(),
                "Hoàn thành đi nào, ta chờ ngươi đó.",
                "Phúc Địa cần ngươi hành động!"
            };
            sp.sendSystemMessage(Component.literal("§2[地靈] §7" + nudge[sl.random.nextInt(nudge.length)]));
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
