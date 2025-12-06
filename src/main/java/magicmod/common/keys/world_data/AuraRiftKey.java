package magicmod.common.keys.world_data;

import net.minecraft.world.World;

import magicmod.common.data.RadiusMap;
import magicmod.common.interfaces.IWorldDataKey;
import magicmod.common.mechanics.rifts.IAuraRift;

public class AuraRiftKey implements IWorldDataKey<RadiusMap<IAuraRift>> {

    @Override
    public RadiusMap<IAuraRift> getInitialValue(World world) {
        return new RadiusMap<>();
    }
}
