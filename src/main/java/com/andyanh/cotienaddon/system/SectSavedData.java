package com.andyanh.cotienaddon.system;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.*;

public class SectSavedData extends SavedData {
    private static final String FILE_NAME = "cotien_sects";
    
    private final Map<UUID, Sect> sects = new HashMap<>();
    private final Map<UUID, UUID> playerToSect = new HashMap<>();

    // pending invites: invitee UUID → sect UUID (in-memory only, cleared on restart)
    private static final Map<UUID, UUID> pendingInvites = new HashMap<>();

    public static void addPendingInvite(UUID invitee, UUID sectId) { pendingInvites.put(invitee, sectId); }
    public static UUID getPendingInvite(UUID invitee) { return pendingInvites.remove(invitee); }

    public static class Sect {
        public UUID id;
        public String name;
        public UUID leader;
        public SectType type;
        public Set<UUID> members = new LinkedHashSet<>();
        public BlockPos homePos;
        public String homeDimension;
        // transient — populated by server before sending sync packet, not persisted
        public transient Map<UUID, String> memberNames = new LinkedHashMap<>();

        public Sect(UUID id, String name, UUID leader, SectType type) {
            this.id = id;
            this.name = name;
            this.leader = leader;
            this.type = type;
            this.members.add(leader);
        }

        public CompoundTag save() {
            CompoundTag nbt = new CompoundTag();
            nbt.putUUID("id", id);
            nbt.putString("name", name);
            nbt.putUUID("leader", leader);
            nbt.putString("type", type.name());
            
            ListTag membersList = new ListTag();
            for (UUID m : members) {
                CompoundTag mTag = new CompoundTag();
                mTag.putUUID("uuid", m);
                membersList.add(mTag);
            }
            nbt.put("members", membersList);
            
            if (homePos != null) {
                nbt.putInt("homeX", homePos.getX());
                nbt.putInt("homeY", homePos.getY());
                nbt.putInt("homeZ", homePos.getZ());
                nbt.putString("homeDim", homeDimension);
            }
            return nbt;
        }

        public static Sect load(CompoundTag nbt) {
            Sect sect = new Sect(
                nbt.getUUID("id"),
                nbt.getString("name"),
                nbt.getUUID("leader"),
                SectType.valueOf(nbt.getString("type"))
            );
            
            ListTag membersList = nbt.getList("members", Tag.TAG_COMPOUND);
            for (int i = 0; i < membersList.size(); i++) {
                sect.members.add(membersList.getCompound(i).getUUID("uuid"));
            }
            
            if (nbt.contains("homeX")) {
                sect.homePos = new BlockPos(nbt.getInt("homeX"), nbt.getInt("homeY"), nbt.getInt("homeZ"));
                sect.homeDimension = nbt.getString("homeDim");
            }
            return sect;
        }
    }

    public enum SectType {
        MORTAL("Phàm Nhân", 100000, 2),
        IMMORTAL("Cổ Tiên", 100000, 4);

        public final String displayName;
        public final int cost;
        public final int buffAmount;

        SectType(String displayName, int cost, int buffAmount) {
            this.displayName = displayName;
            this.cost = cost;
            this.buffAmount = buffAmount;
        }
    }

    public static SectSavedData get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            DimensionDataStorage storage = serverLevel.getServer().overworld().getDataStorage();
            return storage.computeIfAbsent(new SavedData.Factory<>(SectSavedData::new, SectSavedData::load, null), FILE_NAME);
        }
        return new SectSavedData();
    }

    public void createSect(String name, UUID leader, SectType type) {
        UUID id = UUID.randomUUID();
        Sect sect = new Sect(id, name, leader, type);
        sects.put(id, sect);
        playerToSect.put(leader, id);
        setDirty();
    }

    public Sect getSectOfPlayer(UUID playerUuid) {
        UUID sectId = playerToSect.get(playerUuid);
        return sectId != null ? sects.get(sectId) : null;
    }
    
    public Sect getSect(UUID sectId) {
        return sects.get(sectId);
    }

    public void addMember(UUID sectId, UUID playerUuid) {
        Sect sect = sects.get(sectId);
        if (sect != null) {
            sect.members.add(playerUuid);
            playerToSect.put(playerUuid, sectId);
            setDirty();
        }
    }

    public void removeMember(UUID playerUuid) {
        UUID sectId = playerToSect.remove(playerUuid);
        if (sectId != null) {
            Sect sect = sects.get(sectId);
            if (sect != null) {
                sect.members.remove(playerUuid);
                if (sect.members.isEmpty()) {
                    sects.remove(sectId);
                } else if (sect.leader.equals(playerUuid)) {
                    sect.leader = sect.members.iterator().next();
                }
                setDirty();
            }
        }
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
        ListTag sectList = new ListTag();
        for (Sect s : sects.values()) {
            sectList.add(s.save());
        }
        nbt.put("sects", sectList);
        return nbt;
    }

    public static SectSavedData load(CompoundTag nbt, HolderLookup.Provider provider) {
        SectSavedData data = new SectSavedData();
        ListTag sectList = nbt.getList("sects", Tag.TAG_COMPOUND);
        for (int i = 0; i < sectList.size(); i++) {
            Sect s = Sect.load(sectList.getCompound(i));
            data.sects.put(s.id, s);
            for (UUID m : s.members) {
                data.playerToSect.put(m, s.id);
            }
        }
        return data;
    }
}
