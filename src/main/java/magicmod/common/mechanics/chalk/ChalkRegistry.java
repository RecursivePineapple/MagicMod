package magicmod.common.mechanics.chalk;

import java.util.Objects;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.gtnewhorizon.gtnhlib.hash.Fnv1a32;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import magicmod.common.util.MCUtils;

public class ChalkRegistry {

    public static final Hash.Strategy<ItemStack> ITEMSTACK_HASH_STRATEGY_NBT_SENSITIVE = new Hash.Strategy<>() {

        @Override
        public int hashCode(ItemStack o) {
            int hash = Fnv1a32.initialState();

            if (o != null) {
                hash = Fnv1a32.hashStep(hash, Objects.hashCode(o.getItem()));
                hash = Fnv1a32.hashStep(hash, Items.feather.getDamage(o));
                hash = Fnv1a32.hashStep(hash, Objects.hashCode(o.getTagCompound()));
            }

            return hash;
        }

        @Override
        public boolean equals(ItemStack a, ItemStack b) {
            return MCUtils.areStacksBasicallyEqual(a, b);
        }
    };

    private static final ObjectOpenCustomHashSet<ItemStack> CHALK_BASES = new ObjectOpenCustomHashSet<>(ITEMSTACK_HASH_STRATEGY_NBT_SENSITIVE);

    private static final Object2ObjectOpenCustomHashMap<ItemStack, IChalkModifier> CHALK_DOPANTS = new Object2ObjectOpenCustomHashMap<>(ITEMSTACK_HASH_STRATEGY_NBT_SENSITIVE);

    public static boolean isChalkBase(ItemStack stack) {
        return CHALK_BASES.contains(stack);
    }

    public static boolean isChalkDopant(ItemStack stack) {
        return CHALK_DOPANTS.containsKey(stack);
    }

    public static void registerChalkBase(ItemStack stack) {
        CHALK_BASES.add(stack.copy());
    }

    public static void registerChalkDopant(ItemStack stack, IChalkModifier chalkModifier) {
        CHALK_DOPANTS.put(stack.copy(), chalkModifier);
    }
}
