package com.andyanh.cotienaddon.init;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.item.DinhTienDuItem;
import com.andyanh.cotienaddon.item.TienNguyenItem;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.Item;

public class CoTienItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, CoTienAddon.MODID);

    public static final DeferredHolder<Item, DinhTienDuItem> DINH_TIEN_DU =
            ITEMS.register("dinh_tien_du", DinhTienDuItem::new);

    public static final DeferredHolder<Item, TienNguyenItem> TIEN_NGUYEN =
            ITEMS.register("tien_nguyen", TienNguyenItem::new);
}
