package com.andyanh.cotienaddon.init;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.item.DinhTienDuItem;
import com.andyanh.cotienaddon.item.ThangThanhVanTienGuItem;
import com.andyanh.cotienaddon.item.OrphanedNodeItem;
import com.andyanh.cotienaddon.item.TienNguyenItem;
import com.andyanh.cotienaddon.item.TranVuItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.Item;

public class CoTienItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, CoTienAddon.MODID);

    public static final DeferredHolder<Item, DinhTienDuItem> DINH_TIEN_DU =
            ITEMS.register("dinh_tien_du", DinhTienDuItem::new);

    public static final DeferredHolder<Item, ThangThanhVanTienGuItem> THANG_THANH_VAN =
            ITEMS.register("thang_thanh_van", ThangThanhVanTienGuItem::new);

    public static final DeferredHolder<Item, TienNguyenItem> TIEN_NGUYEN =
            ITEMS.register("tien_nguyen", TienNguyenItem::new);

    public static final DeferredHolder<Item, TranVuItem> TRAN_VU =
            ITEMS.register("tran_vu", TranVuItem::new);

    // Phase 4 rewards
    public static final DeferredHolder<Item, Item> DAO_NGAN =
            ITEMS.register("dao_ngan", () -> new Item(new Item.Properties().stacksTo(64)));

    public static final DeferredHolder<Item, Item> TIEN_CO =
            ITEMS.register("tien_co", () -> new Item(new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.EPIC)));

    // Phase 5 — Annex system
    public static final DeferredHolder<Item, OrphanedNodeItem> ORPHANED_NODE =
            ITEMS.register("orphaned_node", OrphanedNodeItem::new);

    // Tiên Đài — waystation
    public static final DeferredHolder<Item, BlockItem> TIEN_DAI =
            ITEMS.register("tien_dai", () -> new BlockItem(CoTienBlocks.TIEN_DAI.get(), new Item.Properties()));

    // Khối Tiên Nguyên — block item, khai thác trong Phúc Địa → drop Tiên Nguyên
    public static final DeferredHolder<Item, BlockItem> KHOI_TIEN_NGUYEN =
            ITEMS.register("khoi_tien_nguyen", () -> new BlockItem(CoTienBlocks.KHOI_TIEN_NGUYEN.get(), new Item.Properties()));
}
