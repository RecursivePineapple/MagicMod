package magicmod.common.factory;

import magicmod.common.factory.framework.StandardFactoryGrid;

public class FormationFactoryGrid extends StandardFactoryGrid<FormationFactoryGrid, FormationFactoryElement, FormationFactoryNetwork> {

    public static final FormationFactoryGrid INSTANCE = new FormationFactoryGrid();

    @Override
    protected FormationFactoryNetwork createNetwork() {
        return new FormationFactoryNetwork();
    }
}
