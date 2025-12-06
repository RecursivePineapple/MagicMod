package magicmod.common.interfaces;

import net.minecraft.world.World;

public interface IWorldDataKey<T> {

    T getInitialValue(World world);

}
