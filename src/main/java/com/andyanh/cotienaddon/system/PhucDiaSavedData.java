package com.andyanh.cotienaddon.system;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Lưu trữ mapping UUID → slot index của Phúc Địa xuyên suốt các session server.
 * Dùng SavedData để persist vào level overworld storage.
 */
public class PhucDiaSavedData extends SavedData {

    private static final String NAME = "cotienaddon_phuc_dia";
    private static final Factory<PhucDiaSavedData> FACTORY =
            new Factory<>(PhucDiaSavedData::new, PhucDiaSavedData::load);

    private int nextSlot = 0;
    private final Map<UUID, Integer> ownerToSlot = new HashMap<>();
    private final Map<Integer, UUID> slotToOwner = new HashMap<>();

    private PhucDiaSavedData() {}

    public static PhucDiaSavedData get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(FACTORY, NAME);
    }

    public int allocateSlot(UUID owner) {
        if (ownerToSlot.containsKey(owner)) {
            return ownerToSlot.get(owner);
        }
        int slot = nextSlot++;
        ownerToSlot.put(owner, slot);
        slotToOwner.put(slot, owner);
        setDirty();
        return slot;
    }

    public int getSlot(UUID owner) {
        return ownerToSlot.getOrDefault(owner, -1);
    }

    public UUID getOwner(int slot) {
        return slotToOwner.get(slot);
    }

    public static PhucDiaSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        PhucDiaSavedData data = new PhucDiaSavedData();
        data.nextSlot = tag.getInt("nextSlot");
        CompoundTag map = tag.getCompound("slotMap");
        for (String key : map.getAllKeys()) {
            UUID owner = UUID.fromString(key);
            int slot = map.getInt(key);
            data.ownerToSlot.put(owner, slot);
            data.slotToOwner.put(slot, owner);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt("nextSlot", nextSlot);
        CompoundTag map = new CompoundTag();
        ownerToSlot.forEach((uuid, slot) -> map.putInt(uuid.toString(), slot));
        tag.put("slotMap", map);
        return tag;
    }
}
