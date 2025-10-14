package magicmod.common.mechanics.chalk;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;

import com.gtnewhorizon.gtnhlib.capability.Capabilities;
import magicmod.common.mechanics.AuraDistribution;

public class ChalkBehaviour {

    public final AuraDistribution runeDistribution = new AuraDistribution();
    public final EnumSet<ChalkTrait> traits = EnumSet.noneOf(ChalkTrait.class);

    public void tryModify(ItemStack modifierStack) {
        IChalkModifier modifier = Capabilities.getCapability(modifierStack, IChalkModifier.class);

        if (modifier != null) {
            modifier.modify(this);
        }
    }
}
