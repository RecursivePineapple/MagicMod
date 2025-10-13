package magicmod.common.mechanics;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

public class AuraDistribution implements Resonance {

    public final Object2DoubleOpenHashMap<IConcept> distribution = new Object2DoubleOpenHashMap<>();

    public AuraDistribution() {

    }

    public AuraDistribution(Map<? extends IConcept, Double> distribution) {
        this.distribution.putAll(distribution);
    }

    @Override
    public double getResonance(IConcept concept) {
        return distribution.getDouble(concept);
    }

    public AuraDistribution copy() {
        AuraDistribution copy = new AuraDistribution();

        copy.distribution.putAll(this.distribution);

        return copy;
    }

    public void multiply(Resonance other) {
        var iter = Object2DoubleMaps.fastIterator(distribution);

        while (iter.hasNext()) {
            var e = iter.next();

            double inOther = other.getResonance(e.getKey());

            if (inOther == 0) {
                iter.remove();
            } else {
                e.setValue(e.getDoubleValue() * inOther);
            }
        }
    }

    public void union(AuraDistribution other) {
        for (var e : Object2DoubleMaps.fastIterable(other.distribution)) {
            this.distribution.addTo(e.getKey(), e.getDoubleValue());
        }
    }

    public void normalize() {
        double sumInv = 1d / this.distribution.values().doubleStream().sum();

        var iter = Object2DoubleMaps.fastIterator(distribution);

        while (iter.hasNext()) {
            var e = iter.next();

            e.setValue(e.getDoubleValue() * sumInv);
        }
    }

    public static AuraDistribution fromBuffer(IAuraBuffer buffer) {
        AuraDistribution dist = new AuraDistribution();

        buffer.forEachConcept((concept, amount) -> {
            dist.distribution.put(concept, amount);
        });

        return dist;
    }
}
