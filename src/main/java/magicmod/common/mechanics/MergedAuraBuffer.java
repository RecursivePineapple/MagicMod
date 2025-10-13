package magicmod.common.mechanics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.cleanroommc.modularui.value.IntValue;
import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

@Desugar
public record MergedAuraBuffer(Collection<IAuraBuffer> buffers) implements IAuraBuffer {

    @Override
    public Set<IConcept> getStoredConcepts() {
        return buffers.stream()
            .flatMap(b -> b.getStoredConcepts().stream())
            .collect(Collectors.toSet());
    }

    @Override
    public double getAmount(IConcept concept) {
        double sum = 0;

        for (IAuraBuffer buffer : buffers) {
            sum += buffer.getAmount(concept);
        }

        return sum;
    }

    @Override
    public double add(IConcept concept, double amount) {
        if (buffers.isEmpty()) return amount;

        int i = 0;

        for (IAuraBuffer buffer : buffers) {
            double share = amount / (buffers.size() - i);
            amount -= share;

            amount += buffer.add(concept, share);

            i++;
        }

        return amount;
    }

    @Override
    public List<AuraStack> addAll(Iterable<AuraStack> toInject) {
        @SuppressWarnings("unchecked")
        List<AuraStack> rejected = Collections.EMPTY_LIST;

        for (AuraStack stack : toInject) {
            double amountRejected = add(stack.concept, stack.amount);

            if (amountRejected > 0) {
                if (rejected == Collections.EMPTY_LIST) rejected = new ArrayList<>();

                rejected.add(new AuraStack(stack.concept, amountRejected));
            }
        }

        return rejected;
    }

    @Override
    public double extract(IConcept concept, double request, boolean simulate) {
        double totalInv = 1d / getAmount(concept);

        double extracted = 0;

        for (IAuraBuffer buffer : buffers) {
            double weight = buffer.getAmount(concept) * totalInv;

            extracted += buffer.extract(concept, weight * request, simulate);
        }

        return extracted;
    }

    @Override
    public void forEachConcept(ConceptConsumerCancelable fn) {
        Object2DoubleOpenHashMap<IConcept> stored = new Object2DoubleOpenHashMap<>();

        for (IAuraBuffer buffer : buffers) {
            buffer.forEachConcept((concept, amount) -> {
                stored.addTo(concept, amount);

                return true;
            });
        }

        var iter = Object2DoubleMaps.fastIterator(stored);

        while (iter.hasNext()) {
            var e = iter.next();

            if (!fn.accept(e.getKey(), e.getDoubleValue())) break;
        }
    }

    @Override
    public void clear() {
        buffers.forEach(IAuraBuffer::clear);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("MergedAuraBuffer{contents=[");

        IntValue counter = new IntValue(0);

        forEachConcept((concept, amount) -> {
            if (counter.getIntValue() > 0) {
                builder.append(", ");
            }

            builder.append(concept)
                .append("=")
                .append(amount);

            counter.setIntValue(counter.getIntValue() + 1);

            return true;
        });

        builder.append("]}");

        return builder.toString();
    }
}
