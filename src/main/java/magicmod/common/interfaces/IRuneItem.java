package magicmod.common.interfaces;

import net.minecraft.item.ItemStack;

import magicmod.common.runes.Rune;

public interface IRuneItem {

    Rune getRune(ItemStack stack);
    
}
