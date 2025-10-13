package magicmod.common.mechanics.aura;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

@EventBusSubscriber
public class AuraFieldRegistry {

    private static final List<IAuraFieldFactory<?>> FACTORIES = new ArrayList<>();
    private static final Int2ObjectMap<List<ObjectObjectImmutablePair<IAuraFieldFactory<?>, IAuraField>>> AURA_FIELDS = new Int2ObjectOpenHashMap<>();

    public static void registerFactory(IAuraFieldFactory<?> factory) {
        FACTORIES.add(factory);
    }

    public static void registerField(World world, IAuraField field) {
        registerField(world.provider.dimensionId, field);
    }

    public static void registerField(int dimensionId, IAuraField field) {
        getDimList(dimensionId).add(ObjectObjectImmutablePair.of(null, field));
    }

    private static @NotNull List<ObjectObjectImmutablePair<IAuraFieldFactory<?>, IAuraField>> getDimList(int dimensionId) {
        return AURA_FIELDS.computeIfAbsent(dimensionId, x -> new ArrayList<>());
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        if (event.world.isRemote) return;

        var forWorld = getDimList(event.world.provider.dimensionId);

        for (IAuraFieldFactory<?> factory : FACTORIES) {
            IAuraField field = factory.createField(event.world);

            if (field != null) forWorld.add(ObjectObjectImmutablePair.of(factory, field));
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (event.world.isRemote) return;

        var forWorld = getDimList(event.world.provider.dimensionId);

        for (Iterator<ObjectObjectImmutablePair<IAuraFieldFactory<?>, IAuraField>> iterator = forWorld.iterator(); iterator.hasNext(); ) {
            var p = iterator.next();
            if (p.left() != null) {
                @SuppressWarnings("unchecked")
                IAuraFieldFactory<IAuraField> factory = (IAuraFieldFactory<IAuraField>) p.left();

                factory.onFieldUnloaded(p.right(), event.world);
                iterator.remove();
            }
        }
    }

    public static List<IAuraField> getFields(World world) {
        var list = AURA_FIELDS.getOrDefault(world.provider.dimensionId, Collections.emptyList());

        return new AbstractList<>() {

            @Override
            public IAuraField get(int index) {
                return list.get(index).right();
            }

            @Override
            public int size() {
                return list.size();
            }
        };
    }
}
