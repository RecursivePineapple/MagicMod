package magicmod.common.runes.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.Pair;
import magicmod.common.interfaces.AttunedRune;
import magicmod.common.mechanics.BaseConcept;
import magicmod.common.mechanics.IConcept;
import magicmod.common.tiles.TileEntityRune;

public class OrderRune extends TileEntityRune implements AttunedRune {

    @Override
    public List<Pair<Class<?>, Object>> getComponents() {
        return Arrays.asList(Pair.of(IConcept.class, BaseConcept.Order));
    }

    @Override
    public @Nullable IConcept getConcept(Collection<AttunedRune> stack) {
        return BaseConcept.Order;
    }
}
