package magicmod.common.interfaces;

import net.minecraft.item.ItemStack;

import magicmod.common.mechanics.AuraBuffer;

public interface IAuraStorageItem {

    AuraBuffer getBuffer(ItemStack stack);

    void setBuffer(ItemStack stack, AuraBuffer buffer);

}
