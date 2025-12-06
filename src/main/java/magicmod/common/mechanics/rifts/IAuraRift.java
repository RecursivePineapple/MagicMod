package magicmod.common.mechanics.rifts;

import magicmod.common.interfaces.ObjectWithPosition;
import magicmod.common.interfaces.ObjectWithRange;
import magicmod.common.mechanics.IAuraBuffer;

public interface IAuraRift extends ObjectWithRange, ObjectWithPosition {

    IAuraBuffer getBuffer();

}
