package magicmod.mixin.early.minecraft;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenLakes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import magicmod.common.mechanics.BaseConcept;
import magicmod.common.mechanics.rifts.AuraRiftSpawner;

@Mixin(WorldGenLakes.class)
public class MixinWorldGenLakes {

    @Inject(method = "generate", at = @At("RETURN"))
    public void spawnAuraRifts(World world, Random rng, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        for (int i = 0; i < 20; i++) {
            int dx = rng.nextInt(16);
            int dy = rng.nextInt(8);
            int dz = rng.nextInt(16);

            if (world.getBlock(x + dx, y - dy, z + dz).getMaterial() == Material.lava) {
                AuraRiftSpawner.spawnRift(world, x, y - dy, z, rng, BaseConcept.Fire);
                break;
            }
        }
    }
}
