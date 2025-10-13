package magicmod.common.runes;

import static magicmod.common.runes.Rune.Doh;
import static magicmod.common.runes.Rune.Lah;
import static magicmod.common.runes.Rune.Toh;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import magicmod.common.factory.ArrayFactoryNetwork;
import magicmod.common.runes.words.TransferWordLogic;
import magicmod.common.tiles.TileEntityRune;

public enum RuneWord {
    Transfer(TransferWordLogic::new, Toh, Lah, Doh),
    //
    ;

    private final RuneWordLogic.RuneWordLogicCtor ctor;
    public final ImmutableList<Rune> runes;
    private final Reference2IntOpenHashMap<Rune> runeIndices = new Reference2IntOpenHashMap<>();

    RuneWord(RuneWordLogic.RuneWordLogicCtor ctor, Rune... runes) {
        this.ctor = ctor;
        HashSet<Rune> runeSet = new HashSet<>();

        runeIndices.defaultReturnValue(-1);

        for (int i = 0; i < runes.length; i++) {
            Rune rune = runes[i];

            if (!runeSet.add(rune)) {
                throw new IllegalArgumentException("Rune word " + name() + " has duplicate rune: " + rune);
            }

            runeIndices.put(rune, i);
        }

        this.runes = ImmutableList.copyOf(runes);
    }

    public int getRuneIndex(Rune rune) {
        int index = runeIndices.getInt(rune);
        if (index == -1) throw new IllegalArgumentException("Rune word " + name() + " does not contain rune " + rune);
        return index;
    }

    public RuneWordLogic create(List<TileEntityRune> runes, ArrayFactoryNetwork network) {
        return ctor.createWordLogic(this, ImmutableList.copyOf(runes), network);
    }

    public static final ImmutableMap<Rune, Set<RuneWord>> WORDS_BY_RUNE;

    static {
        SetMultimap<Rune, RuneWord> map = MultimapBuilder.hashKeys().hashSetValues().build();

        for (RuneWord word : values()) {
            for (Rune rune : word.runes) {
                map.put(rune, word);
            }
        }

        WORDS_BY_RUNE = ImmutableMap.copyOf(Multimaps.asMap(map));
    }
}
