package com.andyanh.cotienaddon;

import com.andyanh.cotienaddon.init.CoTienAttachments;
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
        LOGGER.info("[CoTienAddon] Loaded - Co Tien system initializing...");
    }
}
