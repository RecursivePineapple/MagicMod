package magicmod.common.mechanics.rifts;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

import cpw.mods.fml.common.IWorldGenerator;
import magicmod.common.mechanics.AuraBuffer;
import magicmod.common.mechanics.BaseConcept;
import magicmod.common.mechanics.IConcept;

public class AuraRiftSpawner implements IWorldGenerator {

    public static AuraRiftSpawner INSTANCE = new AuraRiftSpawner();

    public static void spawnRift(World world, int x, int y, int z, Random rng, IConcept concept) {
        AuraBuffer generation = new AuraBuffer();

        generation.add(concept, rng.nextDouble() * 20);

        EntityAuraRift rift = new EntityAuraRift(world, generation, rng.nextDouble() * 200 + 200);

        rift.posX = x + 0.5;
        rift.posY = y + 0.5;
        rift.posZ = z + 0.5;

        world.spawnEntityInWorld(rift);
    }

    @Override
    public void generate(Random rng, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        int blockX = chunkX << 4;
        int blockZ = chunkZ << 4;

        if (rng.nextInt(10) == 0) {
            for (int i = 0; i < 20; i++) {
                int x = blockX + rng.nextInt(16);
                int z = blockZ + rng.nextInt(16);

                int height = world.getHeightValue(x, z);
                if (height <= 0) continue;

                int y = rng.nextInt(height);

                if (world.getBlock(x, y, z)
                    .getMaterial() == Material.lava) {
                    int k2 = 0;

                    while (k2 < 10 && !world.isAirBlock(x, y + k2, z)) k2++;

                    if (world.isAirBlock(x, y + k2, z)) {
                        spawnRift(world, x, y + k2, z, rng, BaseConcept.Fire);
                        break;
                    }
                }
            }
        }

        if (rng.nextInt(50) == 0) {
            for (int i = 0; i < 20; i++) {
                int x = blockX + rng.nextInt(16);
                int z = blockZ + rng.nextInt(16);

                int height = world.getHeightValue(x, z);
                if (height <= 0) continue;

                int y = rng.nextInt(height);

                if (world.getBlock(x, y, z)
                    .getMaterial() == Material.water) {
                    int k2 = 0;

                    while (k2 < 10 && !world.isAirBlock(x, y + k2, z)) k2++;

                    if (world.isAirBlock(x, y + k2, z)) {
                        spawnRift(world, x, y + k2, z, rng, BaseConcept.Water);
                        break;
                    }
                }
            }
        }

        if (rng.nextInt(50) == 0) {
            for (int i = 0; i < 20; i++) {
                int x = blockX + rng.nextInt(16);
                int z = blockZ + rng.nextInt(16);

                int height = world.getHeightValue(x, z);

                if (height >= 15) {
                    int y = rng.nextInt(height - 10) + 2;

                    if (world.isAirBlock(x, y, z)) {
                        spawnRift(world, x, y, z, rng, BaseConcept.Earth);
                        break;
                    }
                }
            }
        }
    }
}
