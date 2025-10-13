package magicmod.common.mechanics.chalk;

import java.util.EnumSet;

import magicmod.common.mechanics.AuraDistribution;
import magicmod.common.mechanics.IConcept;

public class ChalkBehaviour {

    public final AuraDistribution runeDistribution = new AuraDistribution();
    public final EnumSet<ChalkTrait> traits = EnumSet.noneOf(ChalkTrait.class);

}
