package com.andyanh.cotienaddon;

import com.andyanh.cotienaddon.init.CoTienAttachments;
import com.andyanh.cotienaddon.init.CoTienCreativeTabs;
import com.andyanh.cotienaddon.init.CoTienItems;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CoTienAddon.MODID)
public class CoTienAddon {

    public static final String MODID = "cotienaddon";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CoTienAddon(IEventBus modEventBus) {
        CoTienAttachments.ATTACHMENTS.register(modEventBus);
        CoTienItems.ITEMS.register(modEventBus);
        CoTienCreativeTabs.TABS.register(modEventBus);
        LOGGER.info("[CoTienAddon] Loaded - Co Tien system initializing...");
    }
}
