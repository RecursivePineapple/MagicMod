package magicmod.common.factory;

import java.util.UUID;

import magicmod.common.factory.framework.StandardFactoryNetwork;

public class FormationFactoryNetwork extends StandardFactoryNetwork<FormationFactoryNetwork, FormationFactoryElement, FormationFactoryGrid> {

    private final UUID id = UUID.randomUUID();

    @Override
    public String toString() {
        return String.format("FormationFactoryNetwork{id=%s}", id);
    }
}
