package com.andyanh.cotienaddon.init;

import com.andyanh.cotienaddon.CoTienAddon;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CoTienCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CoTienAddon.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CO_TIEN_TAB =
            TABS.register("co_tien", () -> CreativeModeTab.builder()
                    .title(Component.literal("Cổ Tiên - Không Đạo"))
                    .icon(() -> CoTienItems.DINH_TIEN_DU.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(CoTienItems.DINH_TIEN_DU.get());
                        output.accept(CoTienItems.THANG_THANH_VAN.get());
                        output.accept(CoTienItems.TIEN_NGUYEN.get());
                        output.accept(CoTienItems.TRAN_VU.get());
                        output.accept(CoTienItems.DAO_NGAN.get());
                        output.accept(CoTienItems.TIEN_CO.get());
                        output.accept(CoTienItems.TIEN_DAI.get());
                        output.accept(CoTienItems.KHOI_TIEN_NGUYEN.get());
                    })
                    .build());

    // Inject addon items into the base mod's Vân Đạo and Vũ Đạo tabs
    @EventBusSubscriber(modid = CoTienAddon.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class TabInjector {
        private static final ResourceKey<CreativeModeTab> YUN_DAO_TAB =
                ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.parse("guzhenren:yun_dao"));
        private static final ResourceKey<CreativeModeTab> YU_DAO_TAB =
                ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.parse("guzhenren:yu_dao"));

        @SubscribeEvent
        public static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
            // Thăng Thanh Vân → Vân Đạo tab
            if (event.getTabKey() == YUN_DAO_TAB) {
                event.accept(CoTienItems.THANG_THANH_VAN.get());
            }
            // Định Tiên Du + Trấn Vũ Cổ → Vũ Đạo tab
            if (event.getTabKey() == YU_DAO_TAB) {
                event.accept(CoTienItems.DINH_TIEN_DU.get());
                event.accept(CoTienItems.TRAN_VU.get());
            }
        }
    }
}
