package magicmod.common.runes.impl;

import magicmod.common.factory.ArrayConnectionType;
import magicmod.common.factory.FormationNeighbourAdder;
import magicmod.common.runes.words.TransferWordLogic;
import magicmod.common.tiles.TileEntityRune;

public class TohRune extends TileEntityRune {

    @Override
    protected void addFormationNeighbour(TileEntityRune neighbour, FormationNeighbourAdder adder) {
        TransferWordLogic transferWordLogic = arrayNetwork.getRuneWordLogic(this, TransferWordLogic.class);

        if (transferWordLogic != null) {
            adder.addNeighbour(neighbour, ArrayConnectionType.INPUT, this);
        } else {
            super.addFormationNeighbour(neighbour, adder);
        }
    }
}
