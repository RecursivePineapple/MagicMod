package magicmod.common.mechanics.aura;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.minecraft.world.World;

import org.joml.Vector2d;

import com.github.bsideup.jabel.Desugar;
import com.gtnewhorizon.gtnhlib.hash.Fnv1a64;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import magicmod.common.mechanics.AuraBuffer;
import magicmod.common.mechanics.AuraStack;
import magicmod.common.mechanics.IAuraBuffer;
import magicmod.common.mechanics.IConcept;
import magicmod.common.util.DataUtils;
import magicmod.common.util.Curve;
import magicmod.common.util.FeatureLookup;
import magicmod.common.util.XSTR;

public class NodeAuraField implements IAuraField, FeatureLookup.FeatureScanner<NodeAuraField.AuraNode, Void> {

    public final String fieldId;

    private final IConcept concept;
    private final float nodeGenerationChance;
    private final Curve nodeCapacities, nodeGenRates;

    private final FeatureLookup<AuraNode> auraNodes = new FeatureLookup<>(this);

    private final XSTR scanRNG = new XSTR();

    private final List<AuraNode> pooledNodeList = new ArrayList<>();

    private World world;

    public NodeAuraField(String fieldId, IConcept concept, float nodeGenerationChance, Curve nodeCapacities, Curve nodeGenRates) {
        this.fieldId = fieldId;
        this.concept = concept;
        this.nodeGenerationChance = nodeGenerationChance;
        this.nodeCapacities = nodeCapacities;
        this.nodeGenRates = nodeGenRates;
    }

    public static IAuraFieldFactory<NodeAuraField> factory(String fieldId, IConcept concept, float nodeGenerationChance, Curve nodeCapacities, Curve nodeGenRates) {
        return new IAuraFieldFactory<>() {

            @Override
            public NodeAuraField createField(World world) {
                NodeAuraField field = new NodeAuraField(fieldId, concept, nodeGenerationChance, nodeCapacities, nodeGenRates);
                field.setWorld(world);

                return field;
            }

            @Override
            public void onFieldUnloaded(NodeAuraField field, World world) {
                field.setWorld(null);
            }
        };
    }

    public void setWorld(World world) {
        this.world = world;
    }

    @Override
    public boolean contains(IConcept concept) {
        return concept == this.concept;
    }

    @Override
    public IAuraBuffer getBuffer(int x, int y, int z, double radius) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        List<AuraNode> nodes = new ArrayList<>();

        auraNodes.findAll(chunkX, chunkZ, DataUtils.d2iCeil(radius / 16d * 4d), (featureX, featureZ, node) -> {
            nodes.add(node);
        });

        return new IAuraBuffer() {

            @Override
            public Set<IConcept> getStoredConcepts() {
                return Collections.singleton(concept);
            }

            @Override
            public double getAmount(IConcept concept) {
                if (concept != NodeAuraField.this.concept) return 0;

                long now = world.getTotalWorldTime();

                double totalAvailable = 0.0;

                for (AuraNode n : nodes) {
                    double storedAmount = n.getStoredAmount(now);
                    totalAvailable += storedAmount;
                }

                return totalAvailable;
            }

            @Override
            public double add(IConcept concept, double amount) {
                return amount;
            }

            @Override
            public List<AuraStack> addAll(Iterable<AuraStack> toInject) {
                return ObjectIterators.pour(toInject.iterator());
            }

            @Override
            public double extract(IConcept concept, double request, boolean simulate) {
                if (concept != NodeAuraField.this.concept) return 0;

                long now = world.getTotalWorldTime();

                double totalAvailable = getAmount(concept);

                double extracted = 0;

                for (AuraNode node : nodes) {
                    double stored = node.getStoredAmount(now);

                    double weight = stored / totalAvailable;

                    if (!simulate) {
                        extracted += node.extract(now, request * weight);
                    } else {
                        extracted += Math.min(stored, request * weight);
                    }
                }

                return extracted;
            }

            @Override
            public void forEachConcept(ConceptConsumerCancelable fn) {
                fn.accept(concept, getAmount(concept));
            }

            @Override
            public void clear() {

            }

            @Override
            public String toString() {
                return "NodeAuraField.IAuraBuffer{concept=" + concept + ", amount=" + getAmount(concept) + "}";
            }
        };
    }

    @Override
    public void scan(Void state, int chunkX, int chunkZ, FeatureLookup.FeatureInfo<AuraNode> info) {
        long seed = Fnv1a64.initialState();
        seed = Fnv1a64.hashStep(seed, world.getSeed());
        seed = Fnv1a64.hashStep(seed, chunkX);
        seed = Fnv1a64.hashStep(seed, chunkZ);

        scanRNG.setSeed(seed);

        if (scanRNG.nextFloat() > nodeGenerationChance) return;

        info.set(new AuraNode(
            chunkX, chunkZ,
            nodeGenRates.getValue(scanRNG),
            nodeCapacities.getValue(scanRNG),
            NodeSaveData.getNodeState(world, fieldId, chunkX, chunkZ)));
    }

    @Override
    public String toString() {
        return "NodeAuraField{" + "fieldId='" + fieldId + '\'' + '}';
    }

    @Desugar
    public record AuraNode(int chunkX, int chunkZ, double refillTime, double capacity, NodeSaveData.AuraNodeState state) {
        public double getStoredAmount(long now) {
            if (state.lastDrawTime == 0) {
                state.lastDrawTime = now;
                state.remainingAuraPostDraw = capacity;
            }

            double effectiveStartingTime = getNodeCapacityInverse(state.remainingAuraPostDraw / capacity);
            double elapsedRefillAmount = (now - state.lastDrawTime) / 20d / refillTime;

            double newTime = effectiveStartingTime + elapsedRefillAmount;

            if (newTime < 0) return 0; // huh?
            if (newTime > 1) return capacity;

            double generated = getNodeCapacity(newTime) * capacity;

            return DataUtils.clamp(state.remainingAuraPostDraw + generated, 0, capacity);
        }

        public double extract(long now, double request) {
            if (state.lastDrawTime == 0) {
                state.lastDrawTime = now;
                state.remainingAuraPostDraw = capacity;
            }

            double effectiveStartingTime = getNodeCapacityInverse(state.remainingAuraPostDraw / capacity);
            double elapsedRefillAmount = (now - state.lastDrawTime) / 20d / refillTime;

            double newTime = effectiveStartingTime + elapsedRefillAmount;

            double stored;

            if (newTime < 0) {
                stored = 0;
            } else if (newTime > 1) {
                stored = capacity;
            } else {
                stored = getNodeCapacity(newTime) * capacity;
            }

            double toExtract = Math.min(stored, request);

            state.remainingAuraPostDraw = stored - toExtract;
            state.lastDrawTime = now;

            return toExtract;
        }
    }

    private static double getNodeCapacity(double time) {
        return 0.5 * Math.tanh(5 * time - 2.5) + 0.5;
    }

    private static double getNodeCapacityInverse(double capacity) {
        return (atanh(capacity * 2 - 1) + 2.5) / 5;
    }

    private static double atanh(double x) {
        return (Math.log(1 + x) - Math.log(1 - x)) / 2;
    }

    private static class AuraNodeRefillCurve {

        private static final double[] VALUES = new double[1000];

        static {
            Vector2d result = new Vector2d();

            Arrays.fill(VALUES, -1d);

            int[] samples = new int[VALUES.length];

            for (double t = 0; t < 1d; t += 0.0001) {
                compute(t, result);

                int index = DataUtils.clamp((int) (result.x * VALUES.length), 0, VALUES.length);

                double value = result.y;

                int sampleCount = samples[index];

                if (sampleCount > 0) {
                    value = (sampleCount * VALUES[index] + value) / (sampleCount + 1);
                }

                samples[index]++;
                VALUES[index] = value;
            }

            for (int i = 0; i < samples.length; i++) {
                int lo = i;
                int hi = i;

                while (lo >= 0 && samples[lo] == 0) {
                    lo--;
                }

                while (hi < samples.length && samples[hi] == 0) {
                    hi++;
                }

                if (lo < 0 && hi >= samples.length) throw new IllegalStateException("could not fill in gap");

                double value = 0;

                int divisor = 0;

                if (lo >= 0) {
                    value += VALUES[lo];
                    divisor++;
                }

                if (hi < samples.length) {
                    value += VALUES[hi];
                    divisor++;
                }

                VALUES[i] = value / (double) divisor;
            }
        }

        private static void compute(double t, Vector2d out) {
            // 3(1-t)^{2}t\left(0.7,0\right)+3(1-t)t^{2}\cdot\left(0.3,1\right)+t^{3}\left(1,1\right)

            double a = 3 * pow2(1 - t) * t;
            double b = 3 * (1 - t) * pow2(t);
            double c = t * t * t;

            out.x = a * 0.7 + b * 0.3 + c;
            out.y = b + c;
        }

        private static double pow2(double k) {
            return k * k;
        }

        public static double getValue(double x) {
            int index = DataUtils.clamp((int) (x * VALUES.length), 0, VALUES.length);

            return VALUES[index];
        }
    }
}
