package magicmod.mixin.early.minecraft;

import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import magicmod.common.interfaces.IWorldDataKey;
import magicmod.common.interfaces.IWorldExt;

@Mixin(World.class)
public class MixinWorld implements IWorldExt {

    @Unique
    private final Reference2ObjectOpenHashMap<IWorldDataKey<?>, Object> magicmod$worldData = new Reference2ObjectOpenHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T> T magicmod$getWorldData(IWorldDataKey<T> key) {
        T value = (T) magicmod$worldData.get(key);

        if (value == null) {
            value = key.getInitialValue((World) (Object) this);

            if (value != null) {
                magicmod$worldData.put(key, value);
            }
        }

        return value;
    }
}
