package magicmod.common.factory;

import java.util.concurrent.atomic.AtomicInteger;

import magicmod.common.tiles.TileEntityRune;

public class ArrayConnectionType<T> {

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    public static final ArrayConnectionType<TileEntityRune> INPUT = new ArrayConnectionType<>("input", TileEntityRune.class);
    public static final ArrayConnectionType<TileEntityRune> OUTPUT = new ArrayConnectionType<>("output", TileEntityRune.class);

    public final Class<T> type;
    public final String name;
    public final int id = COUNTER.getAndIncrement();

    private ArrayConnectionType(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
