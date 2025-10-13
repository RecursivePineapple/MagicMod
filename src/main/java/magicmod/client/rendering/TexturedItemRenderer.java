package magicmod.client.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import magicmod.common.interfaces.IItemTexture;
import magicmod.common.interfaces.ItemWithTextures;

public class TexturedItemRenderer implements IItemRenderer {

    public static final TexturedItemRenderer INSTANCE = new TexturedItemRenderer();

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return type != ItemRenderType.FIRST_PERSON_MAP;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return type == ItemRenderType.ENTITY && helper == ItemRendererHelper.ENTITY_BOBBING
            || (helper == ItemRendererHelper.ENTITY_ROTATION && Minecraft.getMinecraft().gameSettings.fancyGraphics);
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
        if (!(stack.getItem() instanceof ItemWithTextures texturedItem)) return;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        RenderUtils.applyStandardItemTransform(type);

        IItemTexture[] textures = texturedItem.getTextures(stack);

        if (textures != null) {
            for (IItemTexture texture : textures) {
                texture.render(type, stack);
            }
        }

        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }
}
