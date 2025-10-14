package magicmod.common.items;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import magicmod.MagicMod;
import magicmod.common.mechanics.chalk.ChalkBehaviour;
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

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advancedTooltips) {
        super.addInformation(stack, player, tooltip, advancedTooltips);

        ItemStack base = getChalkBase(stack);
        tooltip.add(MCUtils.translate("magicmod.tooltips.chalk.base", base == null ? null : base.getDisplayName()));

        List<ItemStack> dopants = getChalkDopants(stack);

        if (!dopants.isEmpty()) {
            tooltip.add(MCUtils.translate("magicmod.tooltips.chalk.dopant.header"));

            for (ItemStack dopant : dopants) {
                tooltip.add(MCUtils.translate("magicmod.tooltips.chalk.dopant.entry", dopant.getDisplayName()));
            }
        }
    }

    public static ChalkMode getMode(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();

        ChalkMode mode = DataUtils.getIndexSafe(ChalkMode.values(), tag == null ? 0 : tag.getInteger("mode"));

        return mode == null ? ChalkMode.PlaceRune : mode;
    }

    public static ItemStack getChalkBase(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();

        return ItemStack.loadItemStackFromNBT(tag.getCompoundTag("base"));
    }

    public static List<ItemStack> getChalkDopants(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();

        List<ItemStack> dopants = new ArrayList<>();

        for (NBTTagCompound dopant : MCUtils.getTagList(tag, "dopants")) {
            ItemStack loaded = ItemStack.loadItemStackFromNBT(dopant);

            if (loaded != null) dopants.add(loaded);
        }

        return dopants;
    }

    public static ChalkBehaviour getChalkBehaviour(ItemStack stack) {
        ItemStack base = getChalkBase(stack);
        List<ItemStack> dopants = getChalkDopants(stack);

        ChalkBehaviour behaviour = new ChalkBehaviour();

        behaviour.tryModify(base);
        dopants.forEach(behaviour::tryModify);

        return behaviour;
    }

    public static ItemStack createChalkStack(ItemStack base, List<ItemStack> dopants) {
        ItemStack chalk = new ItemStack(INSTANCE, 1);

        NBTTagCompound tag = new NBTTagCompound();

        tag.setTag("base", base.writeToNBT(new NBTTagCompound()));
        tag.setTag("dopants", dopants.stream().map(stack -> stack.writeToNBT(new NBTTagCompound())).collect(MCUtils.toNBTTagList()));

        chalk.setTagCompound(tag);

        return chalk;
    }

    public enum ChalkMode {
        PlaceRune,
        ToggleStrong,
        ToggleWeak,
        Debug,
    }
}
