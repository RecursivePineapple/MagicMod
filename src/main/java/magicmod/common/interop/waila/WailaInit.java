package magicmod.common.interop.waila;

import cpw.mods.fml.common.event.FMLInterModComms;
import magicmod.common.tiles.TileEntityRune;
import magicmod.common.util.Mods;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;

public class WailaInit {

    public static void register(IWailaRegistrar register) {
        final IWailaDataProvider wailaDataProvider = new RuneWailaDataProvider();

        register.registerBodyProvider(wailaDataProvider, TileEntityRune.class);
        register.registerNBTProvider(wailaDataProvider, TileEntityRune.class);
        register.registerTailProvider(wailaDataProvider, TileEntityRune.class);
    }

    public static void init() {
        FMLInterModComms.sendMessage(Mods.Waila.ID, "register", WailaInit.class.getName() + ".register");
    }
}
