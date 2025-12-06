package magicmod;

import net.minecraftforge.client.MinecraftForgeClient;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import magicmod.client.rendering.RuneISBRH;
import magicmod.client.rendering.TexturedItemRenderer;
import magicmod.client.rendering.entity.EntityRendererAuraRift;
import magicmod.common.items.ItemAuraStone;
import magicmod.common.items.ItemRune;
import magicmod.common.mechanics.rifts.EntityAuraRift;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        RenderingRegistry.registerBlockHandler(RuneISBRH.INSTANCE);

        MinecraftForgeClient.registerItemRenderer(ItemAuraStone.INSTANCE, TexturedItemRenderer.INSTANCE);
        MinecraftForgeClient.registerItemRenderer(ItemRune.INSTANCE, TexturedItemRenderer.INSTANCE);

        RenderingRegistry.registerEntityRenderingHandler(EntityAuraRift.class, EntityRendererAuraRift.INSTANCE);
    }
}
