package magicmod.common.interop.waila;

import cpw.mods.fml.common.event.FMLInterModComms;
import magicmod.common.util.Mods;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaEntityProvider;
import mcp.mobius.waila.api.IWailaRegistrar;

public class WailaInit {

    public static void register(IWailaRegistrar register) {
        final IWailaDataProvider wailaDataProvider = new RuneWailaDataProvider();

        register.registerBodyProvider(wailaDataProvider, IWailaTile.class);
        register.registerNBTProvider(wailaDataProvider, IWailaTile.class);
        register.registerTailProvider(wailaDataProvider, IWailaTile.class);

        final IWailaEntityProvider entityProvider = new EntityWailaDataProvider();

        register.registerBodyProvider(entityProvider, IWailaEntity.class);
        register.registerNBTProvider(entityProvider, IWailaEntity.class);
        register.registerTailProvider(entityProvider, IWailaEntity.class);
    }

    public static void init() {
        FMLInterModComms.sendMessage(Mods.Waila.ID, "register", WailaInit.class.getName() + ".register");
    }
}
