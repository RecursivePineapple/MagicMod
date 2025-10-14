package magicmod.client.rendering;

import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;

import magicmod.common.interfaces.IItemTexture;
import materiallib.api.util.ImmutableColor;

public class BlockItemTexture implements IItemTexture {

    public final Function<ItemStack, IIcon> icon;
    public final Function<ItemStack, ImmutableColor> colour;

    public BlockItemTexture(Function<ItemStack, IIcon> icon, Function<ItemStack, ImmutableColor> colour) {
        this.icon = icon;
        this.colour = colour;
    }

    @Override
    public void render(IItemRenderer.ItemRenderType type, ItemStack stack) {
        ImmutableColor colour = this.colour.apply(stack);
        IIcon icon = this.icon.apply(stack);

        if (colour == null || icon == null) return;

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

        colour.makeActive();
        RenderUtils.renderItem(type, icon);

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationItemsTexture);
    }
}
