package com.andyanh.cotienaddon.event;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.system.SectSavedData;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = CoTienAddon.MODID)
public class SectEventHandler {

    @SubscribeEvent
    public static void onLivingIncomingDamage(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player target && event.getSource().getEntity() instanceof Player attacker) {
            SectSavedData data = SectSavedData.get(target.level());
            SectSavedData.Sect targetSect = data.getSectOfPlayer(target.getUUID());
            SectSavedData.Sect attackerSect = data.getSectOfPlayer(attacker.getUUID());

            if (targetSect != null && attackerSect != null && targetSect.id.equals(attackerSect.id)) {
                // Same sect - cancel damage
                event.setCanceled(true);
            }
        }
    }
}
