package magicmod.common.factory;

import com.github.bsideup.jabel.Desugar;
import magicmod.common.tiles.TileEntityRune;

@Desugar
public record FormationAdjacency<T>(TileEntityRune rune, ArrayConnectionType<T> connectionType, T data) {

    @SuppressWarnings("unchecked")
    public <T2> T2 getData(ArrayConnectionType<T2> connectionType) {
        return connectionType == this.connectionType ? (T2) data : null;
    }

    @SuppressWarnings("unchecked")
    public <T2> FormationAdjacency<T2> cast(ArrayConnectionType<T2> connectionType) {
        return connectionType == this.connectionType ? (FormationAdjacency<T2>) this : null;
    }
}
