package com.andyanh.cotienaddon.system;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.init.CoTienAttachments;
import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = CoTienAddon.MODID)
public class ThangTienManager {

    private static final Set<UUID> ngungKhieuPending = new HashSet<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (sp.tickCount % 20 != 0) return;

        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
        if (data.thangTienPhase == 2) {
            checkNapKhi(sp, data);
        }
    }

    private static void checkNapKhi(ServerPlayer sp, CoTienData data) {
        double nk = data.calcNhanKhi();
        double thienMax = nk * 0.5;
        double diaMax = nk * 0.5;

        // Cân bằng kiểm tra: nếu Thiên hoặc Địa vượt quá 1.5x Nhân Khí → chết
        if (data.thienKhi > nk * 1.5 || data.diaKhi > nk * 1.5) {
            failAscension(sp, data, "mất cân bằng Khí");
            return;
        }

        // Nếu cả hai đạt đủ → chuyển sang phase 3
        if (data.thienKhi >= thienMax && data.diaKhi >= diaMax) {
            startNgungKhieu(sp, data);
            return;
        }

        // Hiệu ứng Nạp Khí
        sp.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, true, false));
        sp.level().playSound(null, sp.blockPosition(),
                SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.5f, 1.5f);
    }

    private static void startNgungKhieu(ServerPlayer sp, CoTienData data) {
        data.thangTienPhase = 3;
        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
        ngungKhieuPending.add(sp.getUUID());

        sp.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                "gui.cotienaddon.msg.ngung_khieu"));

        // Tìm Bản Mệnh Cổ trong inventory → "phóng" vào vòng xoáy
        net.guzhenren.GuzhenrenMod.queueServerWork(60, () -> completeAscension(sp));
    }

    public static void completeAscension(ServerPlayer sp) {
        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
        if (data.thangTienPhase != 3) return;

        // Kiểm tra Bản Mệnh Cổ
        var guzhenrenVars = sp.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
        double benMingGu = guzhenrenVars.benminggu;
        if (benMingGu <= 0) {
            failAscension(sp, data, "không có Bản Mệnh Cổ");
            return;
        }

        // Xóa Bản Mệnh Cổ khỏi inventory (convert thành Tiên Cổ sau)
        // TODO: tìm item benminggu trong inventory, replace bằng TienCo item

        // Set zhuanshu = 6 (Cổ Tiên)
        guzhenrenVars.zhuanshu = 6.0;
        guzhenrenVars.markSyncDirty();

        data.thangTienPhase = 4;
        data.phucDiaGrade = data.calcPhucDiaGrade();
        data.phucDiaOwnerUUID = sp.getUUID().toString();
        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);

        // Assign Phúc Địa slot trong shared dimension
        PhucDiaManager.assignSlotOnAscension(sp);
        ngungKhieuPending.remove(sp.getUUID());

        sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 4));
        sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 3));
        sp.level().playSound(null, sp.blockPosition(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1f, 1f);
        sp.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                "gui.cotienaddon.msg.ascend_success",
                gradeDisplayName(data.phucDiaGrade)));

        CoTienAddon.LOGGER.info("[CoTienAddon] {} ascended to Co Tien! Grade: {}",
                sp.getName().getString(), data.phucDiaGrade);
    }

    public static void failAscension(ServerPlayer sp, CoTienData data, String reason) {
        data.thangTienPhase = 0;
        data.thienKhi = 0;
        data.diaKhi = 0;
        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
        ngungKhieuPending.remove(sp.getUUID());

        sp.hurt(sp.damageSources().magic(), Float.MAX_VALUE);
        sp.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                "gui.cotienaddon.msg.ascend_fail", reason));
        CoTienAddon.LOGGER.warn("[CoTienAddon] {} failed ascension: {}", sp.getName().getString(), reason);
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
