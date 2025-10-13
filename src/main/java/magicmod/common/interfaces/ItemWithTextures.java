package magicmod.common.interfaces;

import net.minecraft.item.ItemStack;

public interface ItemWithTextures {

    /** texture[layer] */
    IItemTexture[] getTextures(ItemStack stack);

}
