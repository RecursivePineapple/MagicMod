package magicmod.common.mechanics.aura;

import magicmod.common.mechanics.AuraBuffer;
import magicmod.common.mechanics.IAuraBuffer;
import magicmod.common.mechanics.IConcept;

public interface IAuraField {

    boolean contains(IConcept concept);

    IAuraBuffer getBuffer(int x, int y, int z, double radius);
}
