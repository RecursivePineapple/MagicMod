package magicmod.common.runes;

import com.google.common.collect.ImmutableList;
import magicmod.common.factory.ArrayFactoryNetwork;
import magicmod.common.tiles.TileEntityRune;

public abstract class RuneWordLogic {

    public final RuneWord word;
    public final ImmutableList<TileEntityRune> runes;
    public final ArrayFactoryNetwork network;

    public interface RuneWordLogicCtor {
        RuneWordLogic createWordLogic(RuneWord word, ImmutableList<TileEntityRune> runes, ArrayFactoryNetwork network);
    }

    public RuneWordLogic(RuneWord word, ImmutableList<TileEntityRune> runes, ArrayFactoryNetwork network) {
        this.word = word;
        this.runes = runes;
        this.network = network;
    }

    @SuppressWarnings("unchecked")
    public <T extends TileEntityRune> T getRune(Rune rune) {
        return (T) runes.get(word.getRuneIndex(rune));
    }

    public abstract void update();
}
