package magicmod.common.util;

import java.util.BitSet;

import com.cleanroommc.modularui.utils.MathUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import magicmod.common.data.CapacityLRUCache;

public class FeatureLookup<T> {

    private static final int REGION_WIDTH = 32;

    public interface FeatureCallback<T> {
        void accept(int featureX, int featureZ, T feature);
    }

    public static class RegionInfo<T> {

        public final int chunkX, chunkZ;
        public final BitSet presence = new BitSet();
        public final Int2ObjectOpenHashMap<T> features = new Int2ObjectOpenHashMap<>();

        public RegionInfo(int chunkX, int chunkZ) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        public final int scan(int minX, int maxX, int minZ, int maxZ) {
            if (presence.isEmpty()) return -1;

            if (minX < chunkX && minZ < chunkZ && maxX >= chunkX + REGION_WIDTH && maxZ >= chunkZ + REGION_WIDTH) {
                return presence.nextSetBit(0);
            }

            minX = MathUtils.clamp(minX - chunkX, 0, REGION_WIDTH - 1);
            maxX = MathUtils.clamp(maxX - chunkX, 0, REGION_WIDTH - 1);
            minZ = MathUtils.clamp(minZ - chunkZ, 0, REGION_WIDTH - 1);
            maxZ = MathUtils.clamp(maxZ - chunkZ, 0, REGION_WIDTH - 1);

            for (int z = minZ; z <= maxZ; z++) {
                int start = getIndex(minX, z);
                int end = getIndex(maxX, z);

                // If there is a feature in this row, this will return its index
                int next = presence.nextSetBit(start);

                // If next == -1, then there are no features past 'start' and there's no point in checking further
                if (next == -1) return -1;

                // If the next closest feature in the bit set is <= the end, then it's contained within this row
                if (next <= end) return next;
            }

            return -1;
        }

        public final void scanAll(int minX, int maxX, int minZ, int maxZ, FeatureCallback<T> callback) {
            if (presence.isEmpty()) return;

            minX = MathUtils.clamp(minX - chunkX, 0, REGION_WIDTH - 1);
            maxX = MathUtils.clamp(maxX - chunkX, 0, REGION_WIDTH - 1);
            minZ = MathUtils.clamp(minZ - chunkZ, 0, REGION_WIDTH - 1);
            maxZ = MathUtils.clamp(maxZ - chunkZ, 0, REGION_WIDTH - 1);

            for (int z = minZ; z <= maxZ; z++) {
                int start = getIndex(minX, z);
                int end = getIndex(maxX, z);

                // If there is a feature in this row, this will return its index
                int next = presence.nextSetBit(start);

                while (next != -1 && next <= end) {
                    callback.accept(next % REGION_WIDTH + chunkX, z + chunkZ, features.get(next));
                    next = presence.nextSetBit(next + 1);
                }

                // If next == -1, then there are no features past 'start' and there's no point in checking further
                if (next == -1) return;
            }
        }

        public final boolean check(int minX, int maxX, int minZ, int maxZ) {
            return scan(minX, maxX, minZ, maxZ) != -1;
        }

        public final void put(int relChunkX, int relChunkZ, T value) {
            int index = getIndex(relChunkX, relChunkZ);

            presence.set(index);

            if (value != null) {
                features.put(index, value);
            }
        }

        public final T remove(int relChunkX, int relChunkZ) {
            int index = getIndex(relChunkX, relChunkZ);

            presence.clear(index);

            return features.remove(index);
        }

        private static int getIndex(int relChunkX, int relChunkZ) {
            return relChunkZ * REGION_WIDTH + relChunkX;
        }
    }

    private final FeatureScanner<T, ?> scanner;
    private final CapacityLRUCache<IntIntPair, RegionInfo<T>> perRegionCache;

    private final IntIntMutablePair pooledCoord = IntIntMutablePair.of(0, 0);

    public static class FeatureInfo<T> {
        public boolean found;
        public T feature;

        public final void set(T feature) {
            found = true;
            this.feature = feature;
        }

        public final void reset() {
            found = false;
            feature = null;
        }
    }

    public interface FeatureScanner<T, R> {
        default R init() {
            return null;
        }

        void scan(R state, int chunkX, int chunkZ, FeatureInfo<T> info);

        default void scan0(Object state, int chunkX, int chunkZ, FeatureInfo<T> info) {
            //noinspection unchecked
            this.scan((R) state, chunkX, chunkZ, info);
        }
    }

    public <R> FeatureLookup(FeatureScanner<T, R> scanner) {
        this(64, scanner);
    }

    public <R> FeatureLookup(int capacity, FeatureScanner<T, R> scanner) {
        this.scanner = scanner;
        perRegionCache = new CapacityLRUCache<>(capacity, this::scanForFeatures);
    }

    private RegionInfo<T> scanForFeatures(IntIntPair regionCoord) {
        RegionInfo<T> info = new RegionInfo<>(regionCoord.leftInt() << 5, regionCoord.rightInt() << 5);

        Object state = scanner.init();

        FeatureInfo<T> featureInfo = new FeatureInfo<>();

        // Scan each chunk in this region to see if it will generate an island
        for (int z = 0; z < 32; z++) {
            for (int x = 0; x < 32; x++) {
                featureInfo.reset();

                scanner.scan0(state, x + info.chunkX, z + info.chunkZ, featureInfo);

                if (featureInfo.found) {
                    info.put(x, z, featureInfo.feature);
                }
            }
        }

        return info;
    }

    public boolean find(int chunkX, int chunkZ, int radius, FeatureInfo<T> featureInfo) {
        int chunkMinX = chunkX - radius;
        int chunkMaxX = chunkX + radius;
        int chunkMinZ = chunkZ - radius;
        int chunkMaxZ = chunkZ + radius;

        int regionMinX = chunkMinX >> 5;
        int regionMaxX = chunkMaxX >> 5;
        int regionMinZ = chunkMinZ >> 5;
        int regionMaxZ = chunkMaxZ >> 5;

        if (featureInfo != null) featureInfo.reset();

        for (int z = regionMinZ; z <= regionMaxZ; z++) {
            for (int x = regionMinX; x <= regionMaxX; x++) {
                pooledCoord.left(x)
                    .right(z);

                RegionInfo<T> region = perRegionCache.get(pooledCoord);

                int index = region.scan(chunkMinX, chunkMaxX, chunkMinZ, chunkMaxZ);

                if (index == -1) continue;

                if (featureInfo != null) featureInfo.set(region.features.get(index));

                return true;
            }
        }

        return false;
    }

    public void findAll(int chunkX, int chunkZ, int radius, FeatureCallback<T> callback) {
        int chunkMinX = chunkX - radius;
        int chunkMaxX = chunkX + radius;
        int chunkMinZ = chunkZ - radius;
        int chunkMaxZ = chunkZ + radius;

        int regionMinX = chunkMinX >> 5;
        int regionMaxX = chunkMaxX >> 5;
        int regionMinZ = chunkMinZ >> 5;
        int regionMaxZ = chunkMaxZ >> 5;

        for (int z = regionMinZ; z <= regionMaxZ; z++) {
            for (int x = regionMinX; x <= regionMaxX; x++) {
                pooledCoord.left(x)
                    .right(z);

                RegionInfo<T> region = perRegionCache.get(pooledCoord);

                region.scanAll(chunkMinX, chunkMaxX, chunkMinZ, chunkMaxZ, callback);
            }
        }
    }
}
