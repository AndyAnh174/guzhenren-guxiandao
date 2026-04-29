package com.andyanh.cotienaddon.init;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.entity.DiaSinhEntity;
import com.andyanh.cotienaddon.entity.ThachNhanEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

public class CoTienEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, CoTienAddon.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<DiaSinhEntity>> DIA_LINH =
            ENTITIES.register("dia_linh", () -> EntityType.Builder
                    .<DiaSinhEntity>of(DiaSinhEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(10)
                    .build("dia_linh"));

    public static final DeferredHolder<EntityType<?>, EntityType<ThachNhanEntity>> THACH_NHAN =
            ENTITIES.register("thach_nhan", () -> EntityType.Builder
                    .<ThachNhanEntity>of(ThachNhanEntity::new, MobCategory.MISC)
                    .sized(0.8f, 1.9f)
                    .clientTrackingRange(10)
                    .build("thach_nhan"));
}
