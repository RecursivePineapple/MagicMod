package magicmod.common.runes.impl;

import java.util.Arrays;
import java.util.List;

import it.unimi.dsi.fastutil.Pair;
import magicmod.common.tiles.TileEntityRune;

public class AuraFieldRune extends TileEntityRune {

    @Override
    public List<Pair<Class<?>, Object>> getComponents() {
        return Arrays.asList(Pair.of(ExtractRune.ExtractionTarget.class, ExtractRune.ExtractionTarget.AuraField));
    }
}
