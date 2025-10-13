package magicmod.common.runes.impl;

import magicmod.common.factory.ArrayConnectionType;
import magicmod.common.factory.FormationNeighbourAdder;
import magicmod.common.tiles.TileEntityRune;

public class OutputRune extends TileEntityRune {

    @Override
    protected void addFormationNeighbour(TileEntityRune neighbour, FormationNeighbourAdder adder) {
        adder.addNeighbour(neighbour, ArrayConnectionType.OUTPUT, this);
    }
}
