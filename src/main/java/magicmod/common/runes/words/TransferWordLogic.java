package magicmod.common.runes.words;

import java.util.Objects;

import com.google.common.collect.ImmutableList;
import magicmod.common.factory.ArrayConnectionType;
import magicmod.common.factory.ArrayFactoryNetwork;
import magicmod.common.interfaces.VerbRuneWordLogic;
import magicmod.common.mechanics.BaseConcept;
import magicmod.common.mechanics.IAuraBuffer;
import magicmod.common.mechanics.IConcept;
import magicmod.common.runes.Rune;
import magicmod.common.runes.RuneWord;
import magicmod.common.runes.RuneWordLogic;
import magicmod.common.runes.impl.LahRune;
import magicmod.common.tiles.TileEntityRune;
import magicmod.common.util.DataUtils;

public class TransferWordLogic extends RuneWordLogic implements VerbRuneWordLogic {

    public TransferWordLogic(RuneWord word, ImmutableList<TileEntityRune> runes, ArrayFactoryNetwork network) {
        super(word, runes, network);
    }

    @Override
    public void update() {
        if (DataUtils.count(network.words, word -> word instanceof VerbRuneWordLogic) > 1) return;

        var input = network.getAdjacentArray(ArrayConnectionType.INPUT);
        var output = network.getAdjacentArray(ArrayConnectionType.OUTPUT);

        if (input == null || output == null) return;

        IAuraBuffer auraBuffer = network.auraBuffer;

        double motion = auraBuffer.getAmount(BaseConcept.Motion);
        double order = auraBuffer.getAmount(BaseConcept.Order);

        double transferPressure = motion * 4 + order * 0.8;
        double transferAmount = motion * 2.5 + order * 0.5;
        double transferEfficiency = 0.95;

        IAuraBuffer inputBuffer = input.rune().getEffectiveAuraBuffer();
        IAuraBuffer outputBuffer = output.rune().getEffectiveAuraBuffer();

        double inputAmount = inputBuffer.getTotalAmount();
        double outputAmount = outputBuffer.getTotalAmount();

        double transferable = DataUtils.clamp(inputAmount - outputAmount, 0, transferAmount);

        if (transferable + outputAmount > transferPressure) {
            transferable = Math.max(0, transferPressure - outputAmount);
        }

        if (transferable <= 0) return;

        double debt = transferable;

        if (motion > 0) {
            debt -= auraBuffer.extract(BaseConcept.Motion, debt * 0.01, false) * 100;
        }

        if (order > 0) {
            debt -= auraBuffer.extract(BaseConcept.Order, debt * 0.1, false) * 10;
        }

        transferable -= debt;

        LahRune lahRune = getRune(Rune.Lah);
        IConcept filter = lahRune.getConcept();

        inputBuffer.transferAmount(
            outputBuffer,
            concept -> {
                double resonance = input.data().getResonance(concept) * output.data().getResonance(concept);

                if (filter != null) {
                    resonance *= Objects.equals(concept, filter) ? 1 : lahRune.getPrecision();
                }

                return resonance;
            },
            transferable,
            transferEfficiency);
    }
}
