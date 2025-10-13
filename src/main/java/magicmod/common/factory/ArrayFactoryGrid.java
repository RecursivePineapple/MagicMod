package magicmod.common.factory;

import net.minecraft.server.MinecraftServer;

import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import magicmod.common.factory.framework.StandardFactoryGrid;

@EventBusSubscriber
public class ArrayFactoryGrid extends StandardFactoryGrid<ArrayFactoryGrid, ArrayFactoryElement, ArrayFactoryNetwork> {

    public static final ArrayFactoryGrid INSTANCE = new ArrayFactoryGrid();

    @Override
    protected ArrayFactoryNetwork createNetwork() {
        return new ArrayFactoryNetwork();
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if (event.side != Side.SERVER) return;
        if (event.phase != TickEvent.Phase.END) return;
        if (event.type != TickEvent.Type.SERVER) return;

        if (MinecraftServer.getServer().getTickCounter() % 20 == 0) {
            for (ArrayFactoryNetwork network : INSTANCE.networks) {
                network.tick();
            }
        }
    }
}
