package com.andyanh.cotienaddon.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class GuTierDetector {

    public static int getTier(ItemStack stack) {
        ResourceLocation id = stack.getItemHolder().unwrapKey()
                .map(k -> k.location()).orElse(null);
        if (id == null || !id.getNamespace().equals("guzhenren")) return 0;
        String path = id.getPath();
        if (path.contains("wuzhuan"))   return 5;
        if (path.contains("sizhuan"))   return 4;
        if (path.contains("sanzhuan"))  return 3;
        if (path.contains("erzhuan"))   return 2;
        if (path.contains("yizhuan"))   return 1;
        // weilianhua = pre-tier, không tính vào Nhân Khí
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
