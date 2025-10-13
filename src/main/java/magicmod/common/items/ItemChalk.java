package magicmod.common.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import magicmod.MagicMod;
import magicmod.common.util.DataUtils;
import magicmod.common.util.MCUtils;

public class ItemChalk extends Item {

    public static final ItemChalk INSTANCE = new ItemChalk();

    public ItemChalk() {
        setUnlocalizedName("chalk");
        setTextureName(MagicMod.MODID + ":tools/chalk");

        setMaxDamage(256);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World worldIn, EntityPlayer player) {
        if (player.isSneaking()) {
            stack = stack.copy();

            NBTTagCompound tag = MCUtils.getOrCreateTag(stack);

            tag.setInteger("mode", (tag.getInteger("mode") + 1) % ChalkMode.values().length);
        }

        return stack;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return MCUtils.translate("item.chalk.name", MCUtils.translate("magicmod.chalk-mode." + getMode(stack).name()));
    }

    public static ChalkMode getMode(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();

        ChalkMode mode = DataUtils.getIndexSafe(ChalkMode.values(), tag == null ? 0 : tag.getInteger("mode"));

        return mode == null ? ChalkMode.PlaceRune : mode;
    }

    public enum ChalkMode {
        PlaceRune,
        ToggleStrong,
        ToggleWeak,
        Debug,
    }
}
