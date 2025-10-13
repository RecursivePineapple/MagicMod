package magicmod.common.factory;

import magicmod.common.factory.framework.IFactoryElement;
import magicmod.common.mechanics.IAuraBuffer;
import magicmod.common.runes.Rune;

public interface ArrayFactoryElement extends IFactoryElement<ArrayFactoryElement, ArrayFactoryNetwork, ArrayFactoryGrid> {

    IAuraBuffer getInternalAuraBuffer();

    void getFormationConnections(FormationNeighbourAdder adder);

    Rune getRune();
}
