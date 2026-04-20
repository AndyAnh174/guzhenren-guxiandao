package com.andyanh.cotienaddon.init;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.data.CoTienData;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class CoTienAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CoTienAddon.MODID);

    public static final Supplier<AttachmentType<CoTienData>> CO_TIEN_DATA =
            ATTACHMENTS.register("co_tien_data", () ->
                    AttachmentType.builder(CoTienData::new)
                            .serialize(CoTienData.CODEC)
                            .copyOnDeath()
                            .build()
            );
}
