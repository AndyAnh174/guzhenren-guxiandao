package com.andyanh.cotienaddon;

import com.andyanh.cotienaddon.client.entity.DiaSinhRenderer;
import com.andyanh.cotienaddon.client.entity.ThachNhanRenderer;
import com.andyanh.cotienaddon.entity.DiaSinhEntity;
import com.andyanh.cotienaddon.entity.ThachNhanEntity;
import com.andyanh.cotienaddon.init.*;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.slf4j.Logger;

@Mod(CoTienAddon.MODID)
public class CoTienAddon {

    public static final String MODID = "cotienaddon";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CoTienAddon(IEventBus modEventBus) {
        CoTienAttachments.ATTACHMENTS.register(modEventBus);
        CoTienBlocks.BLOCKS.register(modEventBus);
        CoTienItems.ITEMS.register(modEventBus);
        CoTienEntities.ENTITIES.register(modEventBus);
        CoTienCreativeTabs.TABS.register(modEventBus);
        LOGGER.info("[CoTienAddon] Loaded - Co Tien system initializing...");
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void onEntityAttributes(EntityAttributeCreationEvent event) {
            event.put(CoTienEntities.DIA_LINH.get(), DiaSinhEntity.createAttributes().build());
            event.put(CoTienEntities.THACH_NHAN.get(), ThachNhanEntity.createAttributes().build());
        }
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(CoTienEntities.DIA_LINH.get(), DiaSinhRenderer::new);
            event.registerEntityRenderer(CoTienEntities.THACH_NHAN.get(), ThachNhanRenderer::new);
        }
    }
}
