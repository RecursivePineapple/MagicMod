package magicmod.common.runes.impl;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import magicmod.common.factory.ArrayFactoryElement;
import magicmod.common.factory.ArrayFactoryGrid;
import magicmod.common.interfaces.AttunedRune;
import magicmod.common.mechanics.IConcept;
import magicmod.common.runes.words.TransferWordLogic;
import magicmod.common.tiles.TileEntityRune;

public class LahRune extends TileEntityRune implements AttunedRune {

    /** The amount of unwanted aura that leaks through this transfer. Lower is better. */
    public double getPrecision() {
        return 0.2;
    }

    @Nullable
    @Override
    public IConcept getConcept(Collection<AttunedRune> stack) {
        TransferWordLogic transferWordLogic = arrayNetwork.getRuneWordLogic(this, TransferWordLogic.class);

        if (transferWordLogic != null) {
            boolean added = stack.add(this);

            try {
                for (ArrayFactoryElement adj : ArrayFactoryGrid.INSTANCE.edges.get(this)) {
                    if (adj instanceof AttunedRune attunedRune) {
                        return attunedRune.getConcept(stack);
                    }
                }
            } finally {
                if (added) {
                    stack.remove(this);
                }
            }
        }

        return null;
    }
}
