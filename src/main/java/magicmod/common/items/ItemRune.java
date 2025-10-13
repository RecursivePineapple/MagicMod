package magicmod.common.items;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import magicmod.client.rendering.BlockItemTexture;
import magicmod.common.interfaces.IItemTexture;
import magicmod.common.interfaces.IRuneItem;
import magicmod.common.interfaces.ItemWithTextures;
import magicmod.common.runes.Rune;
import magicmod.common.util.Color;
import magicmod.common.util.MCUtils;

public class ItemRune extends ItemBlock implements ItemWithTextures, IRuneItem {

    public static ItemRune INSTANCE;

    public ItemRune(Block block) {
        super(block);

        setUnlocalizedName("rune");
        setHasSubtypes(true);

        INSTANCE = this;
    }

    @Override
    public Rune getRune(ItemStack stack) {
        return Rune.RUNES_BY_ID.get(stack.getItemDamage());
    }

    @Override
    public int getMetadata(int meta) {
        return meta;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        Rune rune = getRune(stack);

        return MCUtils.translate("magicmod.misc.rune_colon", MCUtils.translate("magicmod.rune." + rune.name()));
    }

    @Override
    public void getSubItems(Item self, CreativeTabs tag, List<ItemStack> stacks) {
        for (Rune rune : Rune.values()) {
            stacks.add(new ItemStack(this, 1, rune.id));
        }
    }

    @Override
    public IItemTexture[] getTextures(ItemStack stack) {
        Rune rune = getRune(stack);

        return new IItemTexture[] {
            new BlockItemTexture(s -> rune.icon, s -> Color.WHITE),
        };
    }
}
