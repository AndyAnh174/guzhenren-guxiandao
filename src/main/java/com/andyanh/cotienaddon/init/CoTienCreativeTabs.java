package com.andyanh.cotienaddon.init;

import com.andyanh.cotienaddon.CoTienAddon;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
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
                        output.accept(CoTienItems.TIEN_NGUYEN.get());
                    })
                    .build());
}
