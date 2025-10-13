package magicmod.common.interfaces;

import java.util.Collection;
import java.util.HashSet;

import org.jetbrains.annotations.Nullable;

import magicmod.common.mechanics.IConcept;

public interface AttunedRune {

    @Nullable
    IConcept getConcept(Collection<AttunedRune> stack);

    default IConcept getConcept() {
        return getConcept(new HashSet<>());
    }
}
