package magicmod.common.runes.impl;

import java.util.function.Supplier;

import magicmod.common.tiles.TileEntityRune;

public class SimpleRune extends TileEntityRune {

    public static final Supplier<TileEntityRune> SIMPLE_RUNE = SimpleRune::new;
}
