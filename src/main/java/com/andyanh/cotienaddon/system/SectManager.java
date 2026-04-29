package com.andyanh.cotienaddon.system;

import com.andyanh.cotienaddon.CoTienAddon;
import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = CoTienAddon.MODID)
public class SectManager {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer sp && !sp.level().isClientSide()) {
            if (sp.level().getGameTime() % 20 != 0) return; // Check every second

            SectSavedData sectData = SectSavedData.get(sp.level());
            SectSavedData.Sect sect = sectData.getSectOfPlayer(sp.getUUID());
            if (sect == null) return;

            // 1. Phuc Dia Buff (Aperture Healing)
            checkPhucDiaBuff(sp, sect);

            // 2. Team Proximity Buff
            checkTeamBuff(sp, sect);
        }
    }

    private static void checkPhucDiaBuff(ServerPlayer sp, SectSavedData.Sect sect) {
        if (sect.homePos != null && sect.homeDimension != null) {
            String currentDim = sp.level().dimension().location().toString();
            if (currentDim.equals(sect.homeDimension)) {
                double distSq = sp.blockPosition().distSqr(sect.homePos);
                if (distSq < 100 * 100) { // Radius 100
                    GuzhenrenModVariables.PlayerVariables vars = sp.getData(GuzhenrenModVariables.PLAYER_VARIABLES);
                    int amount = sect.type.buffAmount;
                    vars.zhenyuan += amount;
                    vars.jingli += amount;
                    vars.markSyncDirty();
                }
            }
        }
    }

    private static void checkTeamBuff(ServerPlayer sp, SectSavedData.Sect sect) {
        // Find members of the same sect within 20 blocks
        AABB area = sp.getBoundingBox().inflate(20);
        List<ServerPlayer> nearbyMembers = sp.level().getEntitiesOfClass(ServerPlayer.class, area, p -> {
            if (p.getUUID().equals(sp.getUUID())) return true;
            SectSavedData.Sect pSect = SectSavedData.get(p.level()).getSectOfPlayer(p.getUUID());
            return pSect != null && pSect.id.equals(sect.id);
        });

        if (nearbyMembers.size() >= 4) {
            // Apply buffs
            sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 1, false, false));
            sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, false));
        }
    }
}
