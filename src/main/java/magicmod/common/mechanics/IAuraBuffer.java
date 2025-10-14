package magicmod.common.mechanics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.value.DoubleValue;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

public interface IAuraBuffer extends Iterable<AuraStack> {

    Set<IConcept> getStoredConcepts();

    default boolean isEmpty() {
        return Math.abs(getTotalAmount()) <= 0.00001;
    }

    /** Gets the amount of aura for a given concept that's stored in this buffer. */
    double getAmount(IConcept concept);

    /** Adds or subtracts aura from this buffer. Returns the amount of aura that was rejected */
    double add(IConcept concept, double amount);

    /** Adds aura stacks. Returns any rejected aura. */
    List<AuraStack> addAll(Iterable<AuraStack> toInject);

    /** Attempts to extract some aura from this buffer. */
    double extract(IConcept concept, double request, boolean simulate);

    interface ConceptConsumerCancelable {
        boolean accept(IConcept concept, double amount);
    }

    interface ConceptConsumer {
        void accept(IConcept concept, double amount);
    }

    void forEachConcept(ConceptConsumerCancelable fn);

    default void forEachConcept(ConceptConsumer fn) {
        forEachConcept((concept, amount) -> {
            fn.accept(concept, amount);
            return true;
        });
    }

    default Object2DoubleOpenHashMap<IConcept> toMap() {
        Object2DoubleOpenHashMap<IConcept> map = new Object2DoubleOpenHashMap<>();

        forEachConcept((ConceptConsumer) map::put);

        return map;
    }

    /** Empties the buffer. */
    void clear();

    default void transferAmount(IAuraBuffer target, double amount, double efficiency) {
        transferAmount(target, null, amount, efficiency);
    }

    default void transferAmount(IAuraBuffer target, @Nullable Resonance resonance, double amount, double efficiency) {
        var contents = this.toMap();

        AuraDistribution distribution = new AuraDistribution(contents);

        if (resonance != null) distribution.multiply(resonance);

        distribution.normalize();

        Object2DoubleMaps.fastForEach(contents, e -> {
            double extracted = this.extract(e.getKey(), distribution.getResonance(e.getKey()) * amount, false);

            target.add(e.getKey(), extracted * efficiency);
        });
    }

    default void transferAll(IAuraBuffer target, double efficiency) {
        var contents = toMap();

        clear();

        Object2DoubleMaps.fastForEach(contents, e -> {
            target.add(e.getKey(), e.getDoubleValue() * efficiency);
        });
    }

    default double getTotalAmount() {
        DoubleValue total = new DoubleValue(0);

        forEachConcept((concept, amount) -> {
            total.setDoubleValue(total.getDoubleValue() + amount);
        });

        return total.getDoubleValue();
    }

    @Override
    @NotNull
    default Iterator<AuraStack> iterator() {
        List<AuraStack> stacks = new ArrayList<>();

        forEachConcept((concept, amount) -> {
            stacks.add(new AuraStack(concept, amount));
        });

        return stacks.iterator();
    }

    default Stream<AuraStack> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}
