package magicmod.common.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
    public static final Collector<NBTBase, NBTTagList, NBTTagList> NBT_TAG_LIST_COLLECTOR = new Collector<>() {

        @Override
        public Supplier<NBTTagList> supplier() {
            return NBTTagList::new;
        }

        @Override
        public BiConsumer<NBTTagList, NBTBase> accumulator() {
            return NBTTagList::appendTag;
        }

        @Override
        public BinaryOperator<NBTTagList> combiner() {
            return (from, to) -> {
                //noinspection unchecked
                to.tagList.addAll(from.tagList);

                return to;
            };
        }

        @Override
        public Function<NBTTagList, NBTTagList> finisher() {
            return Function.identity();
        }

        @Override
        public Set<Characteristics> characteristics() {
            return new HashSet<>(Arrays.asList(Characteristics.IDENTITY_FINISH));
        }
    };

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

    @SuppressWarnings("unchecked")
    public static <NBT extends NBTBase> Collector<NBT, ?, NBTTagList> toNBTTagList() {
        return (Collector<NBT, ?, NBTTagList>) NBT_TAG_LIST_COLLECTOR;
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

    /**
     * Converts an nbt tag to json.
     * Does not preserve the specific types of the tags, but the returned data will be sane and generally correct.
     * Compatible with Gson.
     */
    @SuppressWarnings("unchecked")
    public static <T extends JsonElement> T toJsonObject(NBTBase nbt) {
        if (nbt == null) return null;

        if (nbt instanceof NBTTagCompound nbtTagCompound) {
            final Map<String, NBTBase> tagMap = (Map<String, NBTBase>) nbtTagCompound.tagMap;

            JsonObject root = new JsonObject();

            for (Map.Entry<String, NBTBase> nbtEntry : tagMap.entrySet()) {
                root.add(nbtEntry.getKey(), toJsonObject(nbtEntry.getValue()));
            }

            return (T) root;
        } else if (nbt instanceof NBTTagByte) {
            // Number (byte)
            return (T) new JsonPrimitive(((NBTTagByte) nbt).func_150290_f());
        } else if (nbt instanceof NBTTagShort) {
            // Number (short)
            return (T) new JsonPrimitive(((NBTTagShort) nbt).func_150289_e());
        } else if (nbt instanceof NBTTagInt) {
            // Number (int)
            return (T) new JsonPrimitive(((NBTTagInt) nbt).func_150287_d());
        } else if (nbt instanceof NBTTagLong) {
            // Number (long)
            return (T) new JsonPrimitive(((NBTTagLong) nbt).func_150291_c());
        } else if (nbt instanceof NBTTagFloat) {
            // Number (float)
            return (T) new JsonPrimitive(((NBTTagFloat) nbt).func_150288_h());
        } else if (nbt instanceof NBTTagDouble) {
            // Number (double)
            return (T) new JsonPrimitive(((NBTTagDouble) nbt).func_150286_g());
        } else if (nbt instanceof NBTBase.NBTPrimitive) {
            // Number
            return (T) new JsonPrimitive(((NBTBase.NBTPrimitive) nbt).func_150286_g());
        } else if (nbt instanceof NBTTagString) {
            // String
            return (T) new JsonPrimitive(((NBTTagString) nbt).func_150285_a_());
        } else if (nbt instanceof NBTTagList list) {
            JsonArray arr = new JsonArray();
            list.tagList.forEach(c -> arr.add(toJsonObject((NBTBase) c)));
            return (T) arr;
        } else if (nbt instanceof NBTTagIntArray list) {
            JsonArray arr = new JsonArray();

            for (int i : list.func_150302_c()) {
                arr.add(new JsonPrimitive(i));
            }

            return (T) arr;
        } else if (nbt instanceof NBTTagByteArray list) {
            JsonArray arr = new JsonArray();

            for (byte i : list.func_150292_c()) {
                arr.add(new JsonPrimitive(i));
            }

            return (T) arr;
        } else {
            throw new IllegalArgumentException("Unsupported NBT Tag: " + NBTBase.NBTTypes[nbt.getId()] + " - " + nbt);
        }
    }

    /**
     * The opposite of {@link #toJsonObject(NBTBase)}
     */
    @SuppressWarnings("unchecked")
    public static <T extends NBTBase> T toNbt(JsonElement jsonElement) {
        if (jsonElement == null || jsonElement == JsonNull.INSTANCE) return null;

        if (jsonElement instanceof JsonPrimitive jsonPrimitive) {
            if (jsonPrimitive.isNumber()) {
                if (jsonPrimitive.getAsBigDecimal().remainder(BigDecimal.ONE).equals(BigDecimal.ZERO)) {
                    long lval = jsonPrimitive.getAsLong();

                    if (lval >= Byte.MIN_VALUE && lval <= Byte.MAX_VALUE) return (T) new NBTTagByte((byte) lval);

                    if (lval >= Short.MIN_VALUE && lval <= Short.MAX_VALUE) return (T) new NBTTagShort((short) lval);

                    if (lval >= Integer.MIN_VALUE && lval <= Integer.MAX_VALUE) return (T) new NBTTagInt((int) lval);

                    return (T) new NBTTagLong(lval);
                } else {
                    double dval = jsonPrimitive.getAsDouble();
                    float fval = (float) dval;

                    if (Math.abs(dval - fval) < 0.0001) return (T) new NBTTagFloat(fval);

                    return (T) new NBTTagDouble(dval);
                }
            } else {
                return (T) new NBTTagString(jsonPrimitive.getAsString());
            }
        } else if (jsonElement instanceof JsonArray jsonArray) {
            final List<NBTBase> tags = new ArrayList<>();

            int type = -1;

            for (JsonElement element : jsonArray) {
                if (element == null || element == JsonNull.INSTANCE) continue;

                NBTBase tag = toNbt(element);

                if (tag == null) continue;

                if (type == -1) type = tag.getId();
                if (type != tag.getId()) throw new IllegalArgumentException("NBT lists cannot contain tags of varying types");

                tags.add(tag);
            }

            // spotless:off
            if (type == Constants.NBT.TAG_INT) {
                return (T) new NBTTagIntArray(tags.stream().mapToInt(i -> ((NBTTagInt) i).func_150287_d()).toArray());
            } else if (type == Constants.NBT.TAG_BYTE) {
                final byte[] array = new byte[tags.size()];

                for (int i = 0; i < tags.size(); i++) {
                    array[i] = ((NBTTagByte) tags.get(i)).func_150290_f();
                }

                return (T) new NBTTagByteArray(array);
            } else {
                NBTTagList list = new NBTTagList();
                tags.forEach(list::appendTag);

                return (T) list;
            }
            // spotless:on
        } else if (jsonElement instanceof JsonObject jsonObject) {
            NBTTagCompound compound = new NBTTagCompound();

            for (Map.Entry<String, JsonElement> jsonEntry : jsonObject.entrySet()) {
                if (jsonEntry.getValue() == JsonNull.INSTANCE) continue;

                compound.setTag(jsonEntry.getKey(), toNbt(jsonEntry.getValue()));
            }

            return (T) compound;
        }

        throw new IllegalArgumentException("Unhandled element " + jsonElement);
    }
}
