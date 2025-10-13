package magicmod.common.factory;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Iterators;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import magicmod.common.factory.framework.StandardFactoryNetwork;
import magicmod.common.mechanics.IAuraBuffer;
import magicmod.common.mechanics.MergedAuraBuffer;
import magicmod.common.runes.Rune;
import magicmod.common.runes.RuneWord;
import magicmod.common.runes.RuneWordLogic;
import magicmod.common.tiles.TileEntityRune;
import magicmod.common.util.DataUtils;

public class ArrayFactoryNetwork extends StandardFactoryNetwork<ArrayFactoryNetwork, ArrayFactoryElement, ArrayFactoryGrid> implements FormationFactoryElement {

    private final UUID id = UUID.randomUUID();

    public final MergedAuraBuffer auraBuffer = new MergedAuraBuffer(new AbstractCollection<>() {

        @Override
        public @NotNull Iterator<IAuraBuffer> iterator() {
            return Iterators.transform(elements.iterator(), ArrayFactoryElement::getInternalAuraBuffer);
        }

        @Override
        public int size() {
            return elements.size();
        }
    });

    public FormationFactoryNetwork formationNetwork;

    public final List<FormationAdjacency<?>> adjacencies = new ArrayList<>();

    public final SetMultimap<ArrayFactoryElement, RuneWordLogic> wordsByRune = MultimapBuilder.hashKeys().hashSetValues().build();
    public final Set<RuneWordLogic> words = new HashSet<>();

    @Override
    public void addElement(ArrayFactoryElement element) {
        boolean wasEmpty = elements.isEmpty();

        super.addElement(element);

        scanForWords(element);
    }

    @Override
    public void removeElement(ArrayFactoryElement element) {
        words.removeAll(wordsByRune.removeAll(element));

        boolean wasEmpty = elements.isEmpty();

        super.removeElement(element);

        if (elements.isEmpty() && !wasEmpty) {
            FormationFactoryGrid.INSTANCE.removeElement(this);
        } else {
            FormationFactoryGrid.INSTANCE.updateElement(this);
        }
    }

    @Override
    public void onElementUpdated(ArrayFactoryElement element, boolean topologyChanged) {
        FormationFactoryGrid.INSTANCE.updateElement(this);
    }

    @Override
    public void getNeighbours(Collection<FormationFactoryElement> neighbours) {
        adjacencies.clear();

        FormationNeighbourAdder adder = new FormationNeighbourAdder() {
            @Override
            public <T> void addNeighbour(TileEntityRune neighbour, ArrayConnectionType<T> connectionType, T value) {
                adjacencies.add(new FormationAdjacency<>(neighbour, connectionType, value));
            }
        };

        this.elements.forEach(e -> e.getFormationConnections(adder));

        for (FormationAdjacency<?> adjacent : adjacencies) {
            ArrayFactoryNetwork network = adjacent.rune().getNetwork();

            if (network != this && network != null) {
                neighbours.add(network);
            }
        }
    }

    @Override
    public FormationFactoryNetwork getNetwork() {
        return formationNetwork;
    }

    @Override
    public void setNetwork(FormationFactoryNetwork network) {
        this.formationNetwork = network;
    }

    @Override
    public String toString() {
        return String.format("ArrayFactoryNetwork{id=%s}", id);
    }

    public <T> FormationAdjacency<T> getAdjacentArray(ArrayConnectionType<T> connectionType) {
        for (FormationAdjacency<?> adjacency : adjacencies) {
            FormationAdjacency<T> casted = adjacency.cast(connectionType);

            if (casted != null) {
                return casted;
            }
        }

        return null;
    }

    public <T> List<FormationAdjacency<T>> getAdjacentArrays(ArrayConnectionType<T> connectionType) {
        List<FormationAdjacency<T>> out = new ArrayList<>();

        for (FormationAdjacency<?> adjacency : adjacencies) {
            FormationAdjacency<T> casted = adjacency.cast(connectionType);

            if (casted != null) out.add(casted);
        }

        return out;
    }

    public void tick() {
        for (FormationAdjacency<?> adjacency : adjacencies) {
            IAuraBuffer outputBuffer = adjacency.rune().getEffectiveAuraBuffer();

            double delta = this.auraBuffer.getTotalAmount() - outputBuffer.getTotalAmount();

            if (delta <= 0) continue;

            this.auraBuffer.transferAmount(outputBuffer, delta * 0.001, 1);
        }

        for (RuneWordLogic wordLogic : words) {
            wordLogic.update();
        }
    }

    private void scanForWords(ArrayFactoryElement element) {
        Rune rune = element.getRune();

        Set<RuneWord> wordCandidates = RuneWord.WORDS_BY_RUNE.get(rune);

        if (wordCandidates == null) return;

        List<ArrayFactoryElement> inWord = new ArrayList<>();

        outer: for (RuneWord candidate : wordCandidates) {
            int index = candidate.getRuneIndex(rune);

            inWord.clear();
            inWord.add(element);

            ArrayFactoryElement curr = element;

            for (int i = index - 1; i >= 0; i--) {
                curr = getAdjacentRune(curr, candidate.runes.get(i));

                if (curr == null) continue outer;

                inWord.add(0, curr);
            }

            curr = element;

            for (int i = index + 1; i < candidate.runes.size(); i++) {
                curr = getAdjacentRune(curr, candidate.runes.get(i));

                if (curr == null) continue outer;

                inWord.add(curr);
            }

            RuneWordLogic wordLogic = candidate.create(DataUtils.downcastUnchecked(inWord), this);

            for (ArrayFactoryElement e : inWord) {
                wordsByRune.put(e, wordLogic);
            }

            words.add(wordLogic);
        }
    }

    private ArrayFactoryElement getAdjacentRune(ArrayFactoryElement start, Rune rune) {
        return DataUtils.find(ArrayFactoryGrid.INSTANCE.edges.get(start), adj -> adj.getRune() == rune);
    }

    public <T extends RuneWordLogic> T getRuneWordLogic(ArrayFactoryElement rune, Class<T> wordLogicClass) {
        return DataUtils.findInstance(wordsByRune.get(rune), wordLogicClass);
    }
}
