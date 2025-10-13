package magicmod;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import magicmod.common.mechanics.BaseConcept;
import magicmod.common.mechanics.aura.AuraFieldRegistry;
import magicmod.common.mechanics.aura.NodeAuraField;
import magicmod.common.util.Curve;

public class ServerProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    @Override
    public void serverStarting(FMLServerStartingEvent event) {
        super.serverStarting(event);
    }
}
