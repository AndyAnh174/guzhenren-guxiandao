package com.andyanh.cotienaddon.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GuTierDetector {

    // Tag chứa tất cả gu tier theo số thứ tự từ mod Guzhenren
    private static final TagKey<Item> TAG_TIER5 = ItemTags.create(ResourceLocation.fromNamespaceAndPath("guzhenren", "gushiguchong5"));
    private static final TagKey<Item> TAG_TIER4 = ItemTags.create(ResourceLocation.fromNamespaceAndPath("guzhenren", "gushiguchong4"));
    private static final TagKey<Item> TAG_TIER3 = ItemTags.create(ResourceLocation.fromNamespaceAndPath("guzhenren", "gushiguchong3"));
    private static final TagKey<Item> TAG_TIER2 = ItemTags.create(ResourceLocation.fromNamespaceAndPath("guzhenren", "gushiguchong2"));
    private static final TagKey<Item> TAG_TIER1 = ItemTags.create(ResourceLocation.fromNamespaceAndPath("guzhenren", "gushiguchong1"));

    public static int getTier(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        ResourceLocation id = stack.getItemHolder().unwrapKey()
                .map(k -> k.location()).orElse(null);
        if (id == null || !id.getNamespace().equals("guzhenren")) return 0;

        // Ưu tiên check tag trước (bao gồm cả gu không có tier trong tên)
        if (stack.is(TAG_TIER5)) return 5;
        if (stack.is(TAG_TIER4)) return 4;
        if (stack.is(TAG_TIER3)) return 3;
        if (stack.is(TAG_TIER2)) return 2;
        if (stack.is(TAG_TIER1)) return 1;

        // Fallback: kiểm tra path (cho gu có tier trong tên)
        String path = id.getPath();
        if (path.contains("wuzhuan"))  return 5;
        if (path.contains("sizhuan"))  return 4;
        if (path.contains("sanzhuan")) return 3;
        if (path.contains("erzhuan"))  return 2;
        if (path.contains("yizhuan"))  return 1;

        return 0;
    }

    public static boolean isGufang(ItemStack stack) {
        ResourceLocation id = stack.getItemHolder().unwrapKey()
                .map(k -> k.location()).orElse(null);
        if (id == null || !id.getNamespace().equals("guzhenren")) return false;
        String path = id.getPath();
        return path.contains("gufang") || path.contains("cangufang");
    }
}
