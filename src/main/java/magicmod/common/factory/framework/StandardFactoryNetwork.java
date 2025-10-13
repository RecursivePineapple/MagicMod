package magicmod.common.factory.framework;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

/**
 * A pretty basic factory network.
 * This doesn't do much beyond tracking elements and components.
 */
public class StandardFactoryNetwork<TSelf extends IFactoryNetwork<TSelf, TElement, TGrid>, TElement extends IFactoryElement<TElement, TSelf, TGrid>, TGrid extends IFactoryGrid<TGrid, TElement, TSelf>>
    implements IFactoryNetwork<TSelf, TElement, TGrid> {

    public final ObjectLinkedOpenHashSet<TElement> elements = new ObjectLinkedOpenHashSet<>();
    public final HashMap<Class<?>, Collection<Object>> components = new HashMap<>();

    @Override
    public void addElement(TElement element) {
        elements.add(element);

        for (var component : element.getComponents()) {
            addComponentImpl(component.left(), component.right());
        }
    }

    @Override
    public void removeElement(TElement element) {
        elements.remove(element);

        if (element != null && element.getNetwork() == this) {
            for (var component : element.getComponents()) {
                removeComponentImpl(component.left(), component.right());
            }
        }
    }

    @Override
    public void onElementUpdated(TElement element, boolean topologyChanged) {

    }

    private void addComponentImpl(Class<?> iface, Object impl) {
        components.computeIfAbsent(iface, x -> new HashSet<>())
            .add(impl);
    }

    public <TIface, TImpl extends TIface> void addComponent(Class<TIface> iface, TImpl impl) {
        addComponentImpl(iface, impl);
    }

    private void removeComponentImpl(Class<?> iface, Object impl) {
        Collection<Object> s = components.get(iface);

        if (s != null) {
            s.remove(impl);

            if (s.isEmpty()) {
                components.remove(iface);
            }
        }
    }

    public <TIface, TImpl extends TIface> void removeComponent(Class<TIface> iface, TImpl impl) {
        removeComponentImpl(iface, impl);
    }

    @SuppressWarnings("unchecked")
    public <TIface> Collection<TIface> getComponents(Class<TIface> iface) {
        return (Collection<TIface>) components.getOrDefault(iface, Collections.emptyList());
    }

    @Override
    public Collection<TElement> getElements() {
        return elements;
    }
}
