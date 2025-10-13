package magicmod;

import net.minecraftforge.client.MinecraftForgeClient;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import magicmod.client.rendering.ItemHolderISBRH;
import magicmod.client.rendering.RuneISBRH;
import magicmod.client.rendering.TexturedItemRenderer;
import magicmod.common.items.ItemAuraStone;
import magicmod.common.items.ItemRune;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        RenderingRegistry.registerBlockHandler(RuneISBRH.INSTANCE);
        RenderingRegistry.registerBlockHandler(ItemHolderISBRH.INSTANCE);

        MinecraftForgeClient.registerItemRenderer(ItemAuraStone.INSTANCE, TexturedItemRenderer.INSTANCE);
        MinecraftForgeClient.registerItemRenderer(ItemRune.INSTANCE, TexturedItemRenderer.INSTANCE);
    }
}
