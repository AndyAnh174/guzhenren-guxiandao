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
    public Map<String, String> memberNames = new HashMap<>();   // UUID -> playerName

    // --- Tiên Nguyên ---
    public double tienNguyen = 0;

    // --- Phúc Địa Development ---
    public int phucDiaLevel = 0;        // 0-10: cấp bậc tổng, mỗi cấp ×2 bán kính
    public double phucDiaXP = 0;        // tổng Tiên Nguyên đã đầu tư
    public int productionLevel = 0;     // 0-5: năng suất Tiên Nguyên (×3 cost)
    public int timeLevel = 0;           // 0-5: tốc độ thời gian (randomTickSpeed, ×3 cost)
    public int defenseLevel = 0;        // 0-5: phòng hộ Thiên Kiếp (×2 cost)
    public int lingmaiLevel = 0;        // 0-5: Linh Mạch, tăng Thiên/Địa Khí gen (×3 cost)
    public int thachnhanSlots = 1;      // số slot Thạch Nhân (bắt đầu 1, mua thêm vô hạn)
    public int dialinhStorageLevel = 0; // 0-3: kho Địa Linh 27→36→45→54 slot
    public int dialinhSkillDamage = 0;  // Cấp sức mạnh tấn công Địa Linh
    public int dialinhSkillHp = 0;      // Cấp sinh lực Địa Linh
    public boolean hasDialinh = false;

    // Cost nâng cấp kỹ năng Địa Linh: 200 -> 400 -> 800...
    public double getDialinhSkillCost(int level) { return level >= 10 ? Double.MAX_VALUE : 200.0 * Math.pow(2, level); }

    // Cost tăng slot Thạch Nhân: 200 × 2^(slotIndex-1)
    public double getThachnhanSlotCost() { return 200.0 * Math.pow(2, thachnhanSlots - 1); }
    // Cost nâng kho Địa Linh: 300 → 600 → 1200
    public double getDialinhStorageCost() { return dialinhStorageLevel >= 3 ? Double.MAX_VALUE : 300.0 * Math.pow(2, dialinhStorageLevel); }
    public double phucDiaDamagePenalty = 0.0;

    // --- Hệ sinh thái Phúc Địa ---
    // --- Danh Hiệu Tôn (Tiên Tôn / Ma Tôn) ---
    public String  tonHieuName    = "";        // phần tên tùy chỉnh (e.g. "Huyền Thiên")
    public int     tonHieuColor   = 0xFFD700;  // màu RGB (default vàng)
    public boolean tonHieuEnabled = false;     // đã kích hoạt

    public String getTonHieuType(double daode) { return daode >= 0 ? "Tiên Tôn" : "Ma Tôn"; }
    public boolean isTienTon(double daode)      { return daode >= 0; }

    public boolean ecoFixedDay      = true;   // luôn ban ngày (6000)
    public boolean ecoAllowRain     = false;  // cho phép mưa
    public boolean ecoPeacefulMobs  = false;  // cho phép sinh vật hòa bình spawn tự nhiên
    public boolean ecoGuzhenrenMobs = false;  // cho phép Cổ Chân Nhân mobs (cổ trùng, quái) spawn

    // --- Địa Linh Quest ---
    public static final int QUEST_NONE = -1;
    public static final int QUEST_WUZHUAN_GU     = 0;  // cung cấp N cổ ngũ chuyển
    public static final int QUEST_TIEN_NGUYEN    = 1;  // đóng góp N Tiên Nguyên
    public static final int QUEST_KILL_MOBS      = 2;  // tiêu diệt N quái trong Phúc Địa
    public static final int QUEST_CLAIM_ORPHANED = 3;  // nộp N Đạo Ngân để Chấp Niệm
    public int    dialinhQuestType     = QUEST_NONE;
    public double dialinhQuestProgress = 0;
    public double dialinhQuestGoal     = 0;
    public int    dialinhQuestRewardTN = 0;
    public boolean dialinhQuestPending = false;

    // Bond quest chain (nhận chủ)
    public int  dialinhBondQuestCount = 0;  // số nhiệm vụ nhận chủ (1-3, 0=chưa bắt đầu)
    public int  dialinhBondQuestsDone = 0;  // số đã hoàn thành
    public boolean dialinhBondComplete = false; // đã nhận chủ xong
    public String dialinhCustomName = "";   // tên tùy chọn sau khi nhận chủ

    public boolean hasActiveQuest() { return dialinhQuestType >= 0 && dialinhQuestProgress < dialinhQuestGoal; }
    public boolean isQuestComplete(){ return dialinhQuestType >= 0 && dialinhQuestProgress >= dialinhQuestGoal; }

    public void generateDialinhQuest(net.minecraft.util.RandomSource rng, int grade) {
        int type = rng.nextInt(3);
        int g = Math.max(1, grade);
        switch (type) {
            case QUEST_WUZHUAN_GU   -> { dialinhQuestGoal = 5 + rng.nextInt(6) * g;  dialinhQuestRewardTN = (int)(dialinhQuestGoal * 20); }
            case QUEST_TIEN_NGUYEN  -> { dialinhQuestGoal = 30 + rng.nextInt(50) * g; dialinhQuestRewardTN = (int)(dialinhQuestGoal * 0.4); }
            case QUEST_KILL_MOBS    -> { dialinhQuestGoal = 8 + rng.nextInt(12);       dialinhQuestRewardTN = (int)(dialinhQuestGoal * 8); }
        }
        dialinhQuestType = type;
        dialinhQuestProgress = 0;
        dialinhQuestPending = false;
    }

    public String questDescription() {
        if (dialinhQuestType == QUEST_NONE) return "Chưa có nhiệm vụ";
        String[] names = {"Cổ Trùng Ngũ Chuyển", "Tiên Nguyên", "Quái Vật", "Đạo Ngân (Chấp Niệm)"};
        String[] units = {"con", "điểm", "con", "điểm"};
        int t = Math.min(dialinhQuestType, names.length - 1);
        String base = names[t] + ": " + (int)dialinhQuestProgress + "/" + (int)dialinhQuestGoal + " " + units[t];
        return dialinhQuestType == QUEST_CLAIM_ORPHANED ? base : base + " — Thưởng: §b" + dialinhQuestRewardTN + " TN";
    }

    // Công thức chung: base × multiplier^level
    private static double upgradeCost(int level, int maxLevel, double base, double mult) {
        if (level >= maxLevel) return Double.MAX_VALUE;
        return Math.round(base * Math.pow(mult, level));
    }

    public double getPhucDiaLevelUpCost()   { return upgradeCost(phucDiaLevel,   10, 100, 2.0); }
    public double getProductionUpgradeCost(){ return upgradeCost(productionLevel,  5, 100, 3.0); }
    public double getTimeUpgradeCost()      { return upgradeCost(timeLevel,        5, 300, 3.0); }
    public double getDefenseUpgradeCost()   { return upgradeCost(defenseLevel,     5, 500, 2.0); }
    public double getLingmaiUpgradeCost()   { return upgradeCost(lingmaiLevel,     5, 800, 3.0); }

    public double getProductionMultiplier() { return 1.0 + productionLevel * 0.5; }
    public double getLingmaiBonus()         { return lingmaiLevel * 0.5; } // +0.5 rate/cấp

    // --- Định Tiên Du saved locations ---
    public static class SavedLocation {
        public String name;
        public double x, y, z;
        public String dimensionId; // e.g. "minecraft:overworld", "minecraft:the_nether"
        public SavedLocation(String name, double x, double y, double z, String dimensionId) {
            this.name = name; this.x = x; this.y = y; this.z = z;
            this.dimensionId = dimensionId != null ? dimensionId : "minecraft:overworld";
        }
    }
    public List<SavedLocation> savedLocations = new ArrayList<>();
    public static final int MAX_SAVED_LOCATIONS = 10;

    // --- Phase 2 timer ---
    public int napKhiTick = 0;   // ticks spent in phase 2 (nap khi), needs ~3600 to complete

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

    // Client-side check (sync từ guzhenren vars qua SyncCoTienPacket)
    public double zhuanshu = 0;
    public double jieduan = 0;
    public double daode   = 0;   // đạo đức — xác định Tiên/Ma Tôn

    public boolean canStartAscension() {
        return zhuanshu >= 5.0 && jieduan >= 4.0 && thangTienPhase == 0 && phucDiaSlot == -1;
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
        tag.putInt("napKhiTick", napKhiTick);
        tag.putInt("phucDiaLevel", phucDiaLevel);
        tag.putDouble("phucDiaXP", phucDiaXP);
        tag.putInt("productionLevel", productionLevel);
        tag.putInt("timeLevel", timeLevel);
        tag.putInt("defenseLevel", defenseLevel);
        tag.putInt("lingmaiLevel", lingmaiLevel);
        tag.putInt("thachnhanSlots", thachnhanSlots);
        tag.putInt("dialinhStorageLevel", dialinhStorageLevel);
        tag.putInt("dialinhSkillDamage", dialinhSkillDamage);
        tag.putInt("dialinhSkillHp", dialinhSkillHp);
        tag.putBoolean("hasDialinh", hasDialinh);
        tag.putDouble("phucDiaDamagePenalty", phucDiaDamagePenalty);
        tag.putString("tonHieuName", tonHieuName);
        tag.putInt("tonHieuColor", tonHieuColor);
        tag.putBoolean("tonHieuEnabled", tonHieuEnabled);
        tag.putBoolean("ecoFixedDay", ecoFixedDay);
        tag.putBoolean("ecoAllowRain", ecoAllowRain);
        tag.putBoolean("ecoPeacefulMobs", ecoPeacefulMobs);
        tag.putBoolean("ecoGuzhenrenMobs", ecoGuzhenrenMobs);
        tag.putInt("dialinhQuestType", dialinhQuestType);
        tag.putDouble("dialinhQuestProgress", dialinhQuestProgress);
        tag.putDouble("dialinhQuestGoal", dialinhQuestGoal);
        tag.putInt("dialinhQuestRewardTN", dialinhQuestRewardTN);
        tag.putInt("dialinhBondQuestCount", dialinhBondQuestCount);
        tag.putInt("dialinhBondQuestsDone", dialinhBondQuestsDone);
        tag.putBoolean("dialinhBondComplete", dialinhBondComplete);
        tag.putString("dialinhCustomName", dialinhCustomName);

        CompoundTag wlTag = new CompoundTag();
        for (int i = 0; i < whitelist.size(); i++) {
            wlTag.putString("wl" + i, whitelist.get(i));
        }
        wlTag.putInt("size", whitelist.size());
        tag.put("whitelist", wlTag);

        CompoundTag permTag = new CompoundTag();
        permissions.forEach(permTag::putInt);
        tag.put("permissions", permTag);

        CompoundTag nameTag = new CompoundTag();
        memberNames.forEach(nameTag::putString);
        tag.put("memberNames", nameTag);

        CompoundTag locTag = new CompoundTag();
        locTag.putInt("size", savedLocations.size());
        for (int i = 0; i < savedLocations.size(); i++) {
            SavedLocation loc = savedLocations.get(i);
            CompoundTag lt = new CompoundTag();
            lt.putString("name", loc.name);
            lt.putDouble("x", loc.x);
            lt.putDouble("y", loc.y);
            lt.putDouble("z", loc.z);
            lt.putString("dim", loc.dimensionId != null ? loc.dimensionId : "minecraft:overworld");
            locTag.put("loc" + i, lt);
        }
        tag.put("savedLocations", locTag);

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
        data.phucDiaLevel    = tag.contains("phucDiaLevel")    ? tag.getInt("phucDiaLevel")    : 0;
        data.phucDiaXP       = tag.contains("phucDiaXP")       ? tag.getDouble("phucDiaXP")    : 0;
        data.productionLevel = tag.contains("productionLevel") ? tag.getInt("productionLevel") : 0;
        data.timeLevel       = tag.contains("timeLevel")       ? tag.getInt("timeLevel")       : 0;
        data.defenseLevel    = tag.contains("defenseLevel")    ? tag.getInt("defenseLevel")    : 0;
        data.lingmaiLevel    = tag.contains("lingmaiLevel")    ? tag.getInt("lingmaiLevel")    : 0;
        data.thachnhanSlots       = tag.contains("thachnhanSlots")       ? tag.getInt("thachnhanSlots")       : 1;
        data.dialinhStorageLevel  = tag.contains("dialinhStorageLevel")  ? tag.getInt("dialinhStorageLevel")  : 0;
        data.dialinhSkillDamage   = tag.contains("dialinhSkillDamage")   ? tag.getInt("dialinhSkillDamage")   : 0;
        data.dialinhSkillHp       = tag.contains("dialinhSkillHp")       ? tag.getInt("dialinhSkillHp")       : 0;
        data.hasDialinh      = tag.contains("hasDialinh") && tag.getBoolean("hasDialinh");
        data.phucDiaDamagePenalty  = tag.contains("phucDiaDamagePenalty") ? tag.getDouble("phucDiaDamagePenalty") : 0.0;
        data.tonHieuName    = tag.contains("tonHieuName")    ? tag.getString("tonHieuName")   : "";
        data.tonHieuColor   = tag.contains("tonHieuColor")   ? tag.getInt("tonHieuColor")     : 0xFFD700;
        data.tonHieuEnabled = tag.contains("tonHieuEnabled") && tag.getBoolean("tonHieuEnabled");
        data.ecoFixedDay      = !tag.contains("ecoFixedDay")      || tag.getBoolean("ecoFixedDay");
        data.ecoAllowRain     = tag.contains("ecoAllowRain")      && tag.getBoolean("ecoAllowRain");
        data.ecoPeacefulMobs  = tag.contains("ecoPeacefulMobs")   && tag.getBoolean("ecoPeacefulMobs");
        data.ecoGuzhenrenMobs = tag.contains("ecoGuzhenrenMobs")  && tag.getBoolean("ecoGuzhenrenMobs");
        data.dialinhQuestType      = tag.contains("dialinhQuestType")     ? tag.getInt("dialinhQuestType")       : QUEST_NONE;
        data.dialinhQuestProgress  = tag.contains("dialinhQuestProgress") ? tag.getDouble("dialinhQuestProgress"): 0;
        data.dialinhQuestGoal      = tag.contains("dialinhQuestGoal")     ? tag.getDouble("dialinhQuestGoal")    : 0;
        data.dialinhQuestRewardTN  = tag.contains("dialinhQuestRewardTN") ? tag.getInt("dialinhQuestRewardTN")   : 0;
        data.dialinhBondQuestCount = tag.contains("dialinhBondQuestCount") ? tag.getInt("dialinhBondQuestCount") : 0;
        data.dialinhBondQuestsDone = tag.contains("dialinhBondQuestsDone") ? tag.getInt("dialinhBondQuestsDone") : 0;
        data.dialinhBondComplete   = tag.contains("dialinhBondComplete") && tag.getBoolean("dialinhBondComplete");
        data.dialinhCustomName     = tag.contains("dialinhCustomName") ? tag.getString("dialinhCustomName") : "";
        data.napKhiTick = tag.contains("napKhiTick") ? tag.getInt("napKhiTick") : 0;
        data.zhuanshu = tag.contains("zhuanshu") ? tag.getDouble("zhuanshu") : 0;
        data.jieduan  = tag.contains("jieduan")  ? tag.getDouble("jieduan")  : 0;
        data.daode    = tag.contains("daode")    ? tag.getDouble("daode")    : 0;

        if (tag.contains("savedLocations")) {
            CompoundTag locTag = tag.getCompound("savedLocations");
            int locSize = locTag.getInt("size");
            for (int i = 0; i < locSize; i++) {
                CompoundTag lt = locTag.getCompound("loc" + i);
                data.savedLocations.add(new SavedLocation(
                        lt.getString("name"),
                        lt.getDouble("x"), lt.getDouble("y"), lt.getDouble("z"),
                        lt.contains("dim") ? lt.getString("dim") : "minecraft:overworld"));
            }
        }

        CompoundTag wlTag = tag.getCompound("whitelist");
        int wlSize = wlTag.getInt("size");
        for (int i = 0; i < wlSize; i++) {
            data.whitelist.add(wlTag.getString("wl" + i));
        }

        CompoundTag permTag = tag.getCompound("permissions");
        for (String key : permTag.getAllKeys()) {
            data.permissions.put(key, permTag.getInt(key));
        }

        CompoundTag nameTag = tag.getCompound("memberNames");
        for (String key : nameTag.getAllKeys()) {
            data.memberNames.put(key, nameTag.getString(key));
        }

        return data;
    }
}
