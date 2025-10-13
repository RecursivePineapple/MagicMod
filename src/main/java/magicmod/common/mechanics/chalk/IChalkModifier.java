package magicmod.common.mechanics.chalk;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import magicmod.common.mechanics.AuraBuffer;
import magicmod.common.mechanics.AuraDistribution;

public interface IChalkModifier {

    default void modify(ChalkBehaviour chalkBehaviour) {
        AuraDistribution dist = this.getAuraDistribution();

        if (dist != null) chalkBehaviour.runeDistribution.union(dist);

        chalkBehaviour.traits.addAll(this.getTraits());
    }

    /** The aura distribution this dopant gives to chalk (which is then given to runes). */
    @Nullable
    AuraDistribution getAuraDistribution();

    /** Any traits this dopant gives to chalk. */
    @NotNull
    List<ChalkTrait> getTraits();

    /** The total amount of aura required to mix chalk with this dopant. */
    AuraBuffer getRequiredMixingAura();
}
