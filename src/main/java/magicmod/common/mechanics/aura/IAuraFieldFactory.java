package magicmod.common.mechanics.aura;

import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

public interface IAuraFieldFactory<F extends IAuraField> {

    @Nullable F createField(World world);

    void onFieldUnloaded(F field, World world);
}
