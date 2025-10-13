package magicmod.common.runes.impl;

import magicmod.common.factory.ArrayConnectionType;
import magicmod.common.factory.FormationNeighbourAdder;
import magicmod.common.tiles.TileEntityRune;

public class InputRune extends TileEntityRune {

    @Override
    protected void addFormationNeighbour(TileEntityRune neighbour, FormationNeighbourAdder adder) {
        adder.addNeighbour(neighbour, ArrayConnectionType.INPUT, this);
    }
}
