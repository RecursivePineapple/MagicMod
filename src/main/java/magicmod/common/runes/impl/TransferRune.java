package magicmod.common.runes.impl;

import magicmod.common.factory.ArrayConnectionType;
import magicmod.common.mechanics.BaseConcept;
import magicmod.common.mechanics.IAuraBuffer;
import magicmod.common.tiles.TileEntityRune;
import magicmod.common.util.DataUtils;

public class TransferRune extends TileEntityRune {

    @Override
    protected void doRuneTick() {
        var input = DataUtils.choose(arrayNetwork.getAdjacentArrays(ArrayConnectionType.INPUT), worldObj.rand);
        var output = DataUtils.choose(arrayNetwork.getAdjacentArrays(ArrayConnectionType.OUTPUT), worldObj.rand);

        if (input == null || output == null) return;

        IAuraBuffer auraBuffer = getEffectiveAuraBuffer();

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

        inputBuffer.transferAmount(
            outputBuffer,
            concept -> input.data().getResonance(concept) * output.data().getResonance(concept),
            transferable,
            transferEfficiency);
    }
}
