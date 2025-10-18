package magicmod.shaded.dynamicaabbtree;

import java.util.function.Predicate;

@FunctionalInterface
public interface AABBOverlapFilter<T extends Boundable & Identifiable> extends Predicate<T>
{

}
