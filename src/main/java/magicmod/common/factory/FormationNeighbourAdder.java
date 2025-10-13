package magicmod.common.factory;

import magicmod.common.tiles.TileEntityRune;

public interface FormationNeighbourAdder {

    <T> void addNeighbour(TileEntityRune neighbour, ArrayConnectionType<T> connectionType, T value);

    default void addNeighbour(TileEntityRune neighbour, ArrayConnectionType<Void> connectionType) {
        addNeighbour(neighbour, connectionType, null);
    }

    default void addNeighbour(TileEntityRune neighbour) {
        addNeighbour(neighbour, null, null);
    }
}
