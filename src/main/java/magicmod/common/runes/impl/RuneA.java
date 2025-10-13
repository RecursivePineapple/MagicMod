package magicmod.common.runes.impl;

import magicmod.common.mechanics.BaseConcept;
import magicmod.common.mechanics.IAuraBuffer;
import magicmod.common.tiles.TileEntityRune;

public class RuneA extends TileEntityRune {

    @Override
    protected void doRuneTick() {
        super.doRuneTick();

        IAuraBuffer auraBuffer = getEffectiveAuraBuffer();

        double present = auraBuffer.getAmount(BaseConcept.Air);

        double remaining = 100 - present;

        auraBuffer.add(BaseConcept.Air, remaining * 0.2);
    }
}
