package magicmod.common.runes.impl;

import java.util.HashSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

import org.apache.commons.lang3.mutable.MutableObject;

import magicmod.common.tiles.TileEntityRune;
import magicmod.common.data.ChunkMap;

public class OriginRune extends TileEntityRune {

    private final MutableObject<OriginRune> self = new MutableObject<>(this);

    @Override
    protected void onFirstTick() {
        super.onFirstTick();

        FakeSaveData saveData = getSaveData(worldObj);

        int chunkX = xCoord >> 4;
        int chunkZ = zCoord >> 4;
        int radius = (getBlockRadius() >> 4) + 1;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                saveData.originRunes.computeIfAbsent(chunkX + x, chunkZ + z, (_x, _z) -> new HashSet<>()).add(self);
            }
        }
    }

    @Override
    protected void onRemoved() {
        FakeSaveData saveData = getSaveData(worldObj);

        int chunkX = xCoord >> 4;
        int chunkZ = zCoord >> 4;
        int radius = (getBlockRadius() >> 4) + 1;

        self.setValue(null);

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                HashSet<MutableObject<OriginRune>> runes = saveData.originRunes.get(chunkX + x, chunkZ + z);

                if (runes != null) {
                    runes.remove(self);

                    if (runes.isEmpty()) {
                        saveData.originRunes.remove(chunkX + x, chunkZ + z);
                    }
                }
            }
        }
    }

    public int getBlockRadius() {
        return 16;
    }

    public float getRitualQuality() {
        return 1;
    }

    private static final String DATA_NAME = "magicmod.origin_runes";

    public static FakeSaveData getSaveData(World world) {
        FakeSaveData saveData = (FakeSaveData) world.mapStorage.loadData(FakeSaveData.class, DATA_NAME);

        if (saveData == null) {
            saveData = new FakeSaveData(DATA_NAME);

            world.mapStorage.setData(DATA_NAME, saveData);
        }

        return saveData;
    }

    public static class FakeSaveData extends WorldSavedData {

        public final ChunkMap<HashSet<MutableObject<OriginRune>>> originRunes = new ChunkMap<>();

        public FakeSaveData(String name) {
            super(name);
        }

        @Override
        public void readFromNBT(NBTTagCompound p_76184_1_) {

        }

        @Override
        public void writeToNBT(NBTTagCompound p_76187_1_) {

        }

        @Override
        public boolean isDirty() {
            // Return false so that it never gets saved
            return false;
        }
    }

}
