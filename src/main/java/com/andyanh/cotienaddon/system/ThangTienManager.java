package com.andyanh.cotienaddon.system;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.init.CoTienAttachments;
import net.guzhenren.network.GuzhenrenModVariables;
import net.guzhenren.procedures.SetupAnimationsProcedure;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = CoTienAddon.MODID)
public class ThangTienManager {

    private static final Set<UUID> ngungKhieuPending = new HashSet<>();

    public static final double DAO_NGAN_DAI_TONG_SU = 100_000.0; // ngưỡng Chuẩn Vô thượng Đại Tông Sư

    public record DaoCheckResult(int count, String topName, double topValue) {}

    /** Kiểm tra bao nhiêu lưu phái đạo đạt Đại Tông Sư (>50,000) */
    public static DaoCheckResult checkDaoNganCondition(net.guzhenren.network.GuzhenrenModVariables.PlayerVariables gv) {
        // Tất cả liupai_*dao fields và tên hiển thị
        double[] values = {
            gv.liupai_xingdao,  gv.liupai_tiandao,  gv.liupai_fengdao,  gv.liupai_leidao,
            gv.liupai_shuidao,  gv.liupai_yandao,    gv.liupai_mudao,    gv.liupai_tudao,
            gv.liupai_guangdao, gv.liupai_andao,     gv.liupai_jiandao,  gv.liupai_liandao,
            gv.liupai_hundao,   gv.liupai_yundao,    gv.liupai_yundao2,  gv.liupai_bingxuedao,
            gv.liupai_jindao,   gv.liupai_rendao,    gv.liupai_zhidao,   gv.liupai_zhendao,
            gv.liupai_qidao,    gv.liupai_nudao,     gv.liupai_lidao,    gv.liupai_yingdao,
            gv.liupai_huadao,   gv.liupai_yuedao,    gv.liupai_xuedao,   gv.liupai_dandao,
            gv.liupai_bingdao,  gv.liupai_huandao,   gv.liupai_dudao,    gv.liupai_mengdao,
            gv.liupai_daodao,   gv.Liupai_gudao,     gv.liupai_xudao,    gv.liupai_feixingdao,
            gv.liupai_bianhuadao, gv.liupai_toudao,  gv.liupai_shidao,   gv.liupai_xindao,
            gv.liupai_lvdao,    gv.liupai_yindao,    gv.liupai_jindao2,  gv.liupai_zhoudao
        };
        String[] names = {
            "Hành đạo","Thiên đạo","Phong đạo","Lôi đạo",
            "Thủy đạo","Viêm đạo","Mộc đạo","Thổ đạo",
            "Quang đạo","Ám đạo","Kiếm đạo","Luyện đạo",
            "Hồn đạo","Vân đạo","Vân đạo 2","Băng tuyết đạo",
            "Kim đạo","Nhân đạo","Trí đạo","Trấn đạo",
            "Khí đạo","Nộ đạo","Lực đạo","Ảnh đạo",
            "Hoa đạo","Nguyệt đạo","Huyết đạo","Đan đạo",
            "Băng đạo","Hoán đạo","Độc đạo","Mộng đạo",
            "Đạo đạo","Cốt đạo","Hư đạo","Phi hành đạo",
            "Biến hóa đạo","Thấu đạo","Thực đạo","Tâm đạo",
            "Lục đạo","Âm đạo","Kim đạo 2","Trụ đạo"
        };
        int count = 0;
        String topName = "Không";
        double topValue = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > topValue) { topValue = values[i]; topName = names[i]; }
            if (values[i] > DAO_NGAN_DAI_TONG_SU) count++;
        }
        return new DaoCheckResult(count, topName, topValue);
    }

    // NAP_KHI_DURATION: 3 minutes = 3600 ticks
    private static final int NAP_KHI_DURATION = 3600;
    // Mob wave every 30 seconds (600 ticks) during phase 2
    private static final int MOB_WAVE_INTERVAL = 600;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());

        if (data.thangTienPhase == 1) {
            if (sp.tickCount % 5 == 0) {
                refreshPhase1Animation(sp);
            }
        } else if (data.thangTienPhase == 2) {
            // True hover: zero gravity + zero velocity every tick
            sp.setNoGravity(true);
            sp.setDeltaMovement(0, 0, 0);

            // Increment nap khi timer every tick
            data.napKhiTick++;
            sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);

            if (sp.tickCount % 5 == 0) {
                refreshPhase2Animation(sp);
            }
            if (sp.tickCount % 20 == 0) {
                checkNapKhi(sp, data);
            }
            if (sp.tickCount % 5 == 0) {
                spawnQiParticles(sp, data);
            }
        }
    }

    private static void refreshPhase1Animation(ServerPlayer sp) {
        // Phase 1: flying up — keep animation + slow effects
        PacketDistributor.sendToPlayer(sp, new SetupAnimationsProcedure.GuzhenrenModAnimationMessage(
                "dazuo3", sp.getId(), false));
        sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 15, 255, false, false));
        sp.addEffect(new MobEffectInstance(MobEffects.GLOWING, 15, 0, true, false));
    }

    private static void refreshPhase2Animation(ServerPlayer sp) {
        // Phase 2: true hover — animation + slowness (no Levitation, gravity handled via setNoGravity)
        PacketDistributor.sendToPlayer(sp, new SetupAnimationsProcedure.GuzhenrenModAnimationMessage(
                "dazuo3", sp.getId(), false));
        sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 15, 255, false, false));
        sp.addEffect(new MobEffectInstance(MobEffects.GLOWING, 15, 0, true, false));
    }

    private static void spawnQiParticles(ServerPlayer sp, CoTienData data) {
        if (!(sp.level() instanceof ServerLevel level)) return;
        double x = sp.getX(), y = sp.getY() + 1, z = sp.getZ();
        double nk = data.calcNhanKhi();

        // Thiên Khí: END_ROD from above
        for (int i = 0; i < 6; i++) {
            double ox = (Math.random() - 0.5) * 8, oz = (Math.random() - 0.5) * 8;
            level.sendParticles(ParticleTypes.END_ROD,
                    x + ox, y + 10 + Math.random() * 5, z + oz,
                    1, 0, -0.4, 0, 0.05);
        }
        // Địa Khí: HAPPY_VILLAGER from below
        for (int i = 0; i < 6; i++) {
            double ox = (Math.random() - 0.5) * 6, oz = (Math.random() - 0.5) * 6;
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    x + ox, y - 4 - Math.random() * 3, z + oz,
                    1, 0, 0.3, 0, 0.05);
        }
        // Nhân Khí: ENCHANT swirl
        level.sendParticles(ParticleTypes.ENCHANT,
                x, y + 0.5, z, 15, 2.0, 1.5, 2.0, 0.2);
        if (nk > 100) {
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    x, y + 0.5, z, 3, 1.0, 1.0, 1.0, 0.05);
        }
    }


    private static void checkNapKhi(ServerPlayer sp, CoTienData data) {
        double nk = data.calcNhanKhi();

        // If no Nhân Khí (no gu used), wait full duration then proceed
        if (nk <= 0) {
            if (data.napKhiTick >= NAP_KHI_DURATION) {
                startNgungKhieu(sp, data);
            }
            return;
        }

        double thienMax = nk * 0.5;
        double diaMax   = nk * 0.5;

        // Balance check: fail if khi is severely imbalanced (over 2x Nhân Khí)
        if (data.thienKhi > nk * 2.0 || data.diaKhi > nk * 2.0) {
            failAscension(sp, data, "mất cân bằng Khí (Thiên=" +
                    String.format("%.1f", data.thienKhi) + " Địa=" +
                    String.format("%.1f", data.diaKhi) + " max=" +
                    String.format("%.1f", nk * 2.0) + ")");
            return;
        }

        // Need minimum NAP_KHI_DURATION ticks before proceeding
        if (data.napKhiTick < NAP_KHI_DURATION) {
            // Progress message every 30 seconds
            if (data.napKhiTick % 600 == 0 && data.napKhiTick > 0) {
                int remaining = (NAP_KHI_DURATION - data.napKhiTick) / 20;
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§b⏳ Đang nạp Khí... còn " + remaining + "s | Thiên=" +
                        String.format("%.1f", data.thienKhi) + " Địa=" +
                        String.format("%.1f", data.diaKhi)));
            }
            sp.level().playSound(null, sp.blockPosition(),
                    SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.4f, 1.5f);
            return;
        }

        // Full duration passed - check if enough khi absorbed
        if (data.thienKhi >= thienMax && data.diaKhi >= diaMax) {
            startNgungKhieu(sp, data);
        } else {
            // Didn't absorb enough — warn but keep waiting (don't fail)
            sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§e⚠ Khí chưa đủ! Cần Thiên≥" + String.format("%.1f", thienMax) +
                    " Địa≥" + String.format("%.1f", diaMax) +
                    " | Hiện tại: Thiên=" + String.format("%.1f", data.thienKhi) +
                    " Địa=" + String.format("%.1f", data.diaKhi)));
        }
    }

    private static void startNgungKhieu(ServerPlayer sp, CoTienData data) {
        data.thangTienPhase = 3;
        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
        ngungKhieuPending.add(sp.getUUID());

        sp.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                "gui.cotienaddon.msg.ngung_khieu"));

        if (sp.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.FLASH, sp.getX(), sp.getY() + 1, sp.getZ(), 3, 0, 0, 0, 0);
        }

        net.guzhenren.GuzhenrenMod.queueServerWork(60, () -> completeAscension(sp));
    }

    public static void completeAscension(ServerPlayer sp) {
        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
        if (data.thangTienPhase != 3) return;

        var guzhenrenVars = sp.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
        double benMingGu = guzhenrenVars.benminggu;
        if (benMingGu <= 0) {
            failAscension(sp, data, "không có Bản Mệnh Cổ");
            return;
        }

        // 1. Upgrade to Co Tien status
        guzhenrenVars.zhuanshu = 6.0;
        // Boost zuida_hunpo (Hồn Phách)
        guzhenrenVars.zuida_hunpo = Math.max(guzhenrenVars.zuida_hunpo * 2, guzhenrenVars.zuida_hunpo + 5000);
        guzhenrenVars.markSyncDirty();

        // 2. Boost Scoreboard "smz" (Sinh Mệnh Trị → max_health) và "gjl" (Công Kích Lực → attack_damage)
        //    PlayerShuXingProcedure đọc từ đây để set attribute thực, không phải vars.gongjili
        if (sp.level() instanceof ServerLevel sl) {
            var sb = sl.getScoreboard();
            var criteria = net.minecraft.world.scores.criteria.ObjectiveCriteria.DUMMY;
            var renderType = net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType.INTEGER;

            // smz (Sinh Mệnh Trị / HP)
            var smzObj = sb.getObjective("smz");
            if (smzObj == null)
                smzObj = sb.addObjective("smz", criteria, Component.literal("smz"), renderType, true, null);
            var smzScore = sb.getOrCreatePlayerScore(
                net.minecraft.world.scores.ScoreHolder.forNameOnly(sp.getScoreboardName()), smzObj);
            int curSmz = smzScore.get();
            // Nếu HP > 5000 thì x2, nếu thấp hơn thì +5000
            if (curSmz > 5000) smzScore.set(curSmz * 2);
            else smzScore.set(curSmz + 5000);

            // gjl (Công Kích Lực / ATK)
            var gjlObj = sb.getObjective("gjl");
            if (gjlObj == null)
                gjlObj = sb.addObjective("gjl", criteria, Component.literal("gjl"), renderType, true, null);
            var gjlScore = sb.getOrCreatePlayerScore(
                net.minecraft.world.scores.ScoreHolder.forNameOnly(sp.getScoreboardName()), gjlObj);
            int curGjl = gjlScore.get();
            // Nếu Dame > 10000 thì x2, nếu thấp hơn thì +10000
            if (curGjl > 10000) gjlScore.set(curGjl * 2);
            else gjlScore.set(curGjl + 10000);

            // Apply ngay vào attribute thực
            net.guzhenren.procedures.PlayerShuXingProcedure.execute(sl, sp.getX(), sp.getY(), sp.getZ(), sp);
            sp.setHealth(sp.getMaxHealth()); // fill HP mới sau khi attribute tăng
        }

        // 3. Set Custom Nameplate
        sp.setCustomName(Component.literal("§b[Tiên Cổ] " + sp.getName().getString()));
        sp.setCustomNameVisible(true);

        data.thangTienPhase = 4;
        data.phucDiaGrade = data.calcPhucDiaGrade();
        data.phucDiaOwnerUUID = sp.getUUID().toString();
        data.napKhiTick = 0;
        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);

        PhucDiaManager.assignSlotOnAscension(sp);
        ngungKhieuPending.remove(sp.getUUID());

        removeAscensionEffects(sp);
        sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 4));
        sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 3));

        if (sp.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1, sp.getZ(), 100, 1, 2, 1, 0.5);
        }

        sp.level().playSound(null, sp.blockPosition(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1f, 1f);
        sp.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                "gui.cotienaddon.msg.ascend_success",
                gradeDisplayName(data.phucDiaGrade)));

        CoTienAddon.LOGGER.info("[CoTienAddon] {} ascended to Co Tien! Stats doubled, Name updated. Grade: {}",
                sp.getName().getString(), data.phucDiaGrade);
    }

    public static void failAscension(ServerPlayer sp, CoTienData data, String reason) {
        data.thangTienPhase = 0;
        data.thienKhi = 0;
        data.diaKhi = 0;
        data.napKhiTick = 0;
        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
        ngungKhieuPending.remove(sp.getUUID());

        removeAscensionEffects(sp);

        sp.hurt(sp.damageSources().magic(), Float.MAX_VALUE);
        sp.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                "gui.cotienaddon.msg.ascend_fail", reason));
        CoTienAddon.LOGGER.warn("[CoTienAddon] {} failed ascension: {}", sp.getName().getString(), reason);
    }

    private static void removeAscensionEffects(ServerPlayer sp) {
        sp.setNoGravity(false);
        sp.removeEffect(MobEffects.LEVITATION);
        sp.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        sp.removeEffect(MobEffects.GLOWING);
    }

    public static void startAscension(ServerPlayer sp) {
        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
        data.thangTienPhase = 1;
        data.phucDiaGrade = data.calcPhucDiaGrade();
        data.napKhiTick = 0;
        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);

        PacketDistributor.sendToPlayer(sp, new SetupAnimationsProcedure.GuzhenrenModAnimationMessage(
                "dazuo3", sp.getId(), true));
        sp.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 80, 4, false, false));
        sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 255, false, false));

        sp.level().playSound(null, sp.blockPosition(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 0.8f);
        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§b✦ Phá Toái Tiên Khiếu — bay lên hội tụ Thiên Địa Nhân Khí!"));

        net.guzhenren.GuzhenrenMod.queueServerWork(80, () -> {
            CoTienData d2 = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
            if (d2.thangTienPhase == 1) {
                d2.thangTienPhase = 2;
                sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), d2);
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§b☯ Bắt đầu Nạp Khí — thiền định 3 phút để hội tụ Thiên Địa Nhân Khí!"));
                // Kích hoạt Thiên Kiếp ngay khi Nạp Khí bắt đầu
                com.andyanh.cotienaddon.event.PhucDiaEventHandler.startThangTienKiep(sp, d2);
                CoTienAddon.LOGGER.info("[CoTienAddon] {} entered Nap Khi phase + Thien Kiep triggered", sp.getName().getString());
            }
        });
    }

    private static String gradeDisplayName(int grade) {
        return switch (grade) {
            case 1 -> "Hạ đẳng";
            case 2 -> "Trung đẳng";
            case 3 -> "Thượng đẳng";
            case 4 -> "Siêu đẳng";
            default -> "Không xác định";
        };
    }
}
