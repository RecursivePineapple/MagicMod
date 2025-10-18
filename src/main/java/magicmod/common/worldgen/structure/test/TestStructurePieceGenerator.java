package magicmod.common.worldgen.structure.test;

import java.io.IOException;

import net.minecraft.world.biome.BiomeGenBase;

import cpw.mods.fml.common.registry.GameRegistry;
import magicmod.common.worldgen.structure.core.StructurePiece;
import magicmod.common.worldgen.structure.core.StructurePieceGenerator;

public class TestStructurePieceGenerator extends StructurePieceGenerator {

    public TestStructurePieceGenerator() throws IOException {
        super("Test");

        registerStart("tower", StructurePiece.load("assets/magicmod/structures/dungeon/stone-brick-tower.json"));
        registerPiece("stairs", StructurePiece.load("assets/magicmod/structures/dungeon/stone-brick-stairs.json"));

        this.setRarity(10);
        GameRegistry.registerWorldGenerator(this, 50);
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        if (!super.canSpawnStructureAtCoords(chunkX, chunkZ)) return false;

        BiomeGenBase[] biomes = this.worldObj.getWorldChunkManager().getBiomeGenAt(null, (chunkX << 4) + 8, (chunkZ << 4) + 8, 1, 1, true);

        return biomes[0].getTempCategory() == BiomeGenBase.TempCategory.MEDIUM;
    }
}
