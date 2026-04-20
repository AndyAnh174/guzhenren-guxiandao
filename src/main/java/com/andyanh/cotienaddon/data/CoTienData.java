package com.andyanh.cotienaddon.data;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CoTienData {

    public static final Codec<CoTienData> CODEC = CompoundTag.CODEC.xmap(
            CoTienData::deserializeNBT,
            CoTienData::serializeNBT
    );


    // --- Thăng Tiên State ---
    public double nhanKhi = 0;          // Nhân Khí tích lũy (container size)
    public double thienKhi = 0;         // Thiên Khí hiện tại
    public double diaKhi = 0;           // Địa Khí hiện tại
    public int thangTienPhase = 0;      // 0=chưa, 1=phá khiếu, 2=nạp khí, 3=ngưng khiếu, 4=hoàn thành
    public int phucDiaGrade = 0;        // 0=none, 1=hạ, 2=trung, 3=thượng, 4=siêu

    // --- Nhân Khí Tracking (gu usage per tier) ---
    public double guUsed_tier1 = 0;
    public double guUsed_tier2 = 0;
    public double guUsed_tier3 = 0;
    public double guUsed_tier4 = 0;
    public double guUsed_tier5 = 0;
    public double guCrafted = 0;

    // --- Phúc Địa ---
    public String phucDiaOwnerUUID = "";        // UUID chủ nhân dạng string
    public int phucDiaSlot = -1;                // Slot index trong shared dimension (-1 = chưa assign)
    public List<String> whitelist = new ArrayList<>();
    public Map<String, Integer> permissions = new HashMap<>();  // UUID -> permission bitfield

    // --- Tiên Nguyên ---
    public double tienNguyen = 0;

    // Permission bits
    public static final int PERM_BUILD       = 1;       // Bit 0: Xây/Phá hủy
    public static final int PERM_CONTAINERS  = 1 << 1;  // Bit 1: Tương tác Vật chứa
    public static final int PERM_COMBAT      = 1 << 2;  // Bit 2: Sát thương Thực thể
    public static final int PERM_CORE        = 1 << 3;  // Bit 3: Truy cập Cốt lõi
    public static final int PERM_MANAGE      = 1 << 4;  // Bit 4: Quản lý Cấp cao

    public boolean hasPermission(UUID player, int permBit) {
        int bits = permissions.getOrDefault(player.toString(), 0);
        return (bits & permBit) != 0;
    }

    public void setPermission(UUID player, int permBit, boolean value) {
        String key = player.toString();
        int bits = permissions.getOrDefault(key, 0);
        permissions.put(key, value ? (bits | permBit) : (bits & ~permBit));
    }

    public double calcNhanKhi() {
        return guUsed_tier1 * 1
             + guUsed_tier2 * 3
             + guUsed_tier3 * 9
             + guUsed_tier4 * 27
             + guUsed_tier5 * 81
             + guCrafted * 10;
    }

    public int calcPhucDiaGrade() {
        double nk = calcNhanKhi();
        if (nk >= 100000) return 4;
        if (nk >= 10000)  return 3;
        if (nk >= 1000)   return 2;
        return 1;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("nhanKhi", nhanKhi);
        tag.putDouble("thienKhi", thienKhi);
        tag.putDouble("diaKhi", diaKhi);
        tag.putInt("thangTienPhase", thangTienPhase);
        tag.putInt("phucDiaGrade", phucDiaGrade);
        tag.putDouble("guUsed1", guUsed_tier1);
        tag.putDouble("guUsed2", guUsed_tier2);
        tag.putDouble("guUsed3", guUsed_tier3);
        tag.putDouble("guUsed4", guUsed_tier4);
        tag.putDouble("guUsed5", guUsed_tier5);
        tag.putDouble("guCrafted", guCrafted);
        tag.putString("phucDiaOwner", phucDiaOwnerUUID);
        tag.putInt("phucDiaSlot", phucDiaSlot);
        tag.putDouble("tienNguyen", tienNguyen);

        CompoundTag wlTag = new CompoundTag();
        for (int i = 0; i < whitelist.size(); i++) {
            wlTag.putString("wl" + i, whitelist.get(i));
        }
        wlTag.putInt("size", whitelist.size());
        tag.put("whitelist", wlTag);

        CompoundTag permTag = new CompoundTag();
        permissions.forEach(permTag::putInt);
        tag.put("permissions", permTag);

        return tag;
    }

    public static CoTienData deserializeNBT(CompoundTag tag) {
        CoTienData data = new CoTienData();
        data.nhanKhi = tag.getDouble("nhanKhi");
        data.thienKhi = tag.getDouble("thienKhi");
        data.diaKhi = tag.getDouble("diaKhi");
        data.thangTienPhase = tag.getInt("thangTienPhase");
        data.phucDiaGrade = tag.getInt("phucDiaGrade");
        data.guUsed_tier1 = tag.getDouble("guUsed1");
        data.guUsed_tier2 = tag.getDouble("guUsed2");
        data.guUsed_tier3 = tag.getDouble("guUsed3");
        data.guUsed_tier4 = tag.getDouble("guUsed4");
        data.guUsed_tier5 = tag.getDouble("guUsed5");
        data.guCrafted = tag.getDouble("guCrafted");
        data.phucDiaOwnerUUID = tag.getString("phucDiaOwner");
        data.phucDiaSlot = tag.contains("phucDiaSlot") ? tag.getInt("phucDiaSlot") : -1;
        data.tienNguyen = tag.getDouble("tienNguyen");

        CompoundTag wlTag = tag.getCompound("whitelist");
        int wlSize = wlTag.getInt("size");
        for (int i = 0; i < wlSize; i++) {
            data.whitelist.add(wlTag.getString("wl" + i));
        }

        CompoundTag permTag = tag.getCompound("permissions");
        for (String key : permTag.getAllKeys()) {
            data.permissions.put(key, permTag.getInt(key));
        }

        return data;
    }
}
