package magicmod.common.factory.framework;

import java.util.Collection;

/**
 * A factory network is a logical group of factory elements.
 * You usually want to extend {@link StandardFactoryNetwork}, not this.
 */
public interface IFactoryNetwork<TSelf extends IFactoryNetwork<TSelf, TElement, TGrid>, TElement extends IFactoryElement<TElement, TSelf, TGrid>, TGrid extends IFactoryGrid<TGrid, TElement, TSelf>> {

    void addElement(TElement element);

    void removeElement(TElement element);

    void onElementUpdated(TElement element, boolean topologyChanged);

    default void onNetworkRemoved() {

    }

    default void onNetworkSubsumedPre(TSelf subsumer) {

    }

    default void onNetworkSubsumedPost(TSelf subsumer) {

    }

    Collection<TElement> getElements();
}
