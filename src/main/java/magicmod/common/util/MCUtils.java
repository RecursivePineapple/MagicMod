package magicmod.common.util;

import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

@SuppressWarnings("unused")
public class MCUtils {

    /**
     * Formats a number with group separator and at most 2 fraction digits.
     */
    private static final Map<Locale, DecimalFormat> DECIMAL_FORMATTERS = new HashMap<>();

    public static final String BLACK = EnumChatFormatting.BLACK.toString();
    public static final String DARK_BLUE = EnumChatFormatting.DARK_BLUE.toString();
    public static final String DARK_GREEN = EnumChatFormatting.DARK_GREEN.toString();
    public static final String DARK_AQUA = EnumChatFormatting.DARK_AQUA.toString();
    public static final String DARK_RED = EnumChatFormatting.DARK_RED.toString();
    public static final String DARK_PURPLE = EnumChatFormatting.DARK_PURPLE.toString();
    public static final String GOLD = EnumChatFormatting.GOLD.toString();
    public static final String GRAY = EnumChatFormatting.GRAY.toString();
    public static final String DARK_GRAY = EnumChatFormatting.DARK_GRAY.toString();
    public static final String BLUE = EnumChatFormatting.BLUE.toString();
    public static final String GREEN = EnumChatFormatting.GREEN.toString();
    public static final String AQUA = EnumChatFormatting.AQUA.toString();
    public static final String RED = EnumChatFormatting.RED.toString();
    public static final String LIGHT_PURPLE = EnumChatFormatting.LIGHT_PURPLE.toString();
    public static final String YELLOW = EnumChatFormatting.YELLOW.toString();
    public static final String WHITE = EnumChatFormatting.WHITE.toString();
    public static final String OBFUSCATED = EnumChatFormatting.OBFUSCATED.toString();
    public static final String BOLD = EnumChatFormatting.BOLD.toString();
    public static final String STRIKETHROUGH = EnumChatFormatting.STRIKETHROUGH.toString();
    public static final String UNDERLINE = EnumChatFormatting.UNDERLINE.toString();
    public static final String ITALIC = EnumChatFormatting.ITALIC.toString();
    public static final String RESET = EnumChatFormatting.RESET.toString();

    public static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    private MCUtils() {}

    public static NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null) stack.setTagCompound(tag = new NBTTagCompound());

        return tag;
    }

    public static Iterable<NBTTagCompound> getTagList(NBTTagCompound tag, String key) {
        //noinspection unchecked
        return tag.getTagList(key, Constants.NBT.TAG_COMPOUND).tagList;
    }

    public static void sendErrorMessage(ICommandSender player, String message) {
        player.addChatMessage(new ChatComponentText(RED + message));
    }

    public static void sendWarningMessage(ICommandSender player, String message) {
        player.addChatMessage(new ChatComponentText(GOLD + message));
    }

    public static void sendInfoMessage(ICommandSender player, String message) {
        player.addChatMessage(new ChatComponentText(GRAY + message));
    }

    public static void sendChatMessage(ICommandSender player, String message) {
        player.addChatMessage(new ChatComponentText(message));
    }

    public static String stripFormat(String text) {
        return FORMATTING_CODE_PATTERN.matcher(text).replaceAll("");
    }

    private static DecimalFormat getDecimalFormat() {
        return DECIMAL_FORMATTERS.computeIfAbsent(Locale.getDefault(Locale.Category.FORMAT), locale -> {
            DecimalFormat numberFormat = new DecimalFormat(); // uses the necessary locale inside anyway
            numberFormat.setGroupingUsed(true);
            numberFormat.setMaximumFractionDigits(2);
            numberFormat.setRoundingMode(RoundingMode.HALF_UP);
            DecimalFormatSymbols decimalFormatSymbols = numberFormat.getDecimalFormatSymbols();
            decimalFormatSymbols.setGroupingSeparator(','); // Use sensible separator for best clarity.
            numberFormat.setDecimalFormatSymbols(decimalFormatSymbols);
            return numberFormat;
        });
    }

    public static String formatNumbers(BigInteger aNumber) {
        return getDecimalFormat().format(aNumber);
    }

    public static String formatNumbers(long aNumber) {
        return getDecimalFormat().format(aNumber);
    }

    public static String formatNumbers(double aNumber) {
        return getDecimalFormat().format(aNumber);
    }

    public static EntityPlayer getPlayerById(UUID playerId) {
        for (EntityPlayer player : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            if (player.getGameProfile().getId().equals(playerId)) { return player; }
        }

        return null;
    }

    public static Stream<ItemStack> streamInventory(IInventory inv) {
        return IntStream.range(0, inv.getSizeInventory())
            .mapToObj(inv::getStackInSlot);
    }

    public static ItemStack[] inventoryToArray(IInventory inv) {
        return inventoryToArray(inv, true);
    }

    public static ItemStack[] inventoryToArray(IInventory inv, boolean copyStacks) {
        ItemStack[] array = new ItemStack[inv.getSizeInventory()];

        for (int i = 0; i < array.length; i++) {
            array[i] = copyStacks ? ItemStack.copyItemStack(inv.getStackInSlot(i)) : inv.getStackInSlot(i);
        }

        return array;
    }

    public static ForgeDirection nullIfUnknown(ForgeDirection dir) {
        return dir == ForgeDirection.UNKNOWN ? null : dir;
    }

    public static NBTTagCompound copy(NBTTagCompound tag) {
        return tag == null ? null : (NBTTagCompound) tag.copy();
    }

    public static ItemStack copyWithAmount(ItemStack stack, int amount) {
        if (stack == null) return null;
        stack = stack.copy();
        stack.stackSize = amount;
        return stack;
    }

    public static boolean areStacksBasicallyEqual(ItemStack a, ItemStack b) {
        if (a == null || b == null) { return a == null && b == null; }

        return a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage() && ItemStack.areItemStackTagsEqual(a, b);
    }

    public static String getDirectionDisplayName(ForgeDirection dir) {
        return getDirectionDisplayName(dir, false);
    }

    public static String getDirectionDisplayName(ForgeDirection dir, boolean unknownIsCentre) {
        return switch (dir) {
            case DOWN -> "Down";
            case EAST -> "East";
            case NORTH -> "North";
            case SOUTH -> "South";
            case UNKNOWN -> unknownIsCentre ? "Center" : "Unknown";
            case UP -> "Up";
            case WEST -> "West";
        };
    }

    public static String translate(String key) {
        return StatCollector.translateToLocal(key);
    }

    public static String translate(String key, Object... params) {
        return StatCollector.translateToLocalFormatted(key, params);
    }
}
