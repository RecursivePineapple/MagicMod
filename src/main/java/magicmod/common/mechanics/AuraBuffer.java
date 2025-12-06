package magicmod.common.mechanics;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import magicmod.common.util.MCUtils;

public class AuraBuffer implements IAuraBuffer {

    public final Object2DoubleOpenHashMap<IConcept> energies = new Object2DoubleOpenHashMap<>();

    @Override
    public Set<IConcept> getStoredConcepts() {
        return energies.keySet();
    }

    @Override
    public double getAmount(IConcept concept) {
        double amount = energies.getDouble(concept);

        return Double.isNaN(amount) ? 0 : amount;
    }

    @Override
    public double add(IConcept concept, double amount) {
        amount = energies.getDouble(concept) + amount;

        if (amount <= 0) {
            energies.removeDouble(concept);
        } else {
            energies.put(concept, amount);
        }

        return 0;
    }

    @Override
    public List<AuraStack> addAll(Iterable<AuraStack> toInject) {
        for (AuraStack stack : toInject) {
            add(stack.concept, stack.amount);
        }

        return Collections.emptyList();
    }

    @Override
    public double extract(IConcept concept, double request, boolean simulate) {
        double present = energies.getDouble(concept);

        double toExtract = Math.min(present, request);

        if (!simulate) {
            if (toExtract < request) {
                energies.removeDouble(concept);
            } else {
                energies.put(concept, present - toExtract);
            }
        }

        return toExtract;
    }

    @Override
    public double getTotalAmount() {
        var iter = Object2DoubleMaps.fastIterator(energies);

        double sum = 0;

        while (iter.hasNext()) {
            var e = iter.next();

            double amount = e.getDoubleValue();

            if (amount <= 0 || Double.isNaN(amount)) {
                iter.remove();
                continue;
            }

            sum += amount;
        }

        return sum;
    }

    @Override
    public void forEachConcept(ConceptConsumerCancelable fn) {
        var iter = Object2DoubleMaps.fastIterator(energies);

        while (iter.hasNext()) {
            var e = iter.next();

            double amount = e.getDoubleValue();

            if (amount <= 0 || Double.isNaN(amount)) continue;

            if (!fn.accept(e.getKey(), e.getDoubleValue())) break;
        }
    }

    @Override
    public void clear() {
        energies.clear();
    }

    public AuraBuffer split(int numerator, int denominator) {
        if (numerator <= 0) throw new IllegalArgumentException("numerator must be >0");
        if (denominator <= 0) throw new IllegalArgumentException("denominator must be >0");
        if (numerator > denominator) throw new IllegalArgumentException("numerator must be >denominator");

        AuraBuffer buffer = new AuraBuffer();

        if (numerator == denominator) {
            buffer.energies.putAll(energies);
            energies.clear();
        } else {
            var iter = energies.object2DoubleEntrySet().fastIterator();

            double share = (double) numerator / (double) denominator;

            while (iter.hasNext()) {
                var e = iter.next();

                double extracted = e.getDoubleValue() * share;

                e.setValue(e.getDoubleValue() - extracted);
                buffer.energies.put(e.getKey(), extracted);
            }
        }

        return buffer;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        NBTTagList contents = new NBTTagList();

        Object2DoubleMaps.fastForEach(energies, e -> {
            NBTTagCompound energy = new NBTTagCompound();

            NBTTagCompound concept = new NBTTagCompound();
            e.getKey().writeToNBT(concept);

            energy.setTag("c", concept);
            energy.setDouble("k", e.getDoubleValue());

            contents.appendTag(energy);
        });

        if (!contents.tagList.isEmpty()) tag.setTag("c", contents);

        return tag;
    }

    public void readFromNBT(NBTTagCompound tag) {
        energies.clear();

        for (NBTTagCompound energy : MCUtils.getTagList(tag, "c")) {
            energies.addTo(IConcept.readFromNBT(energy.getCompoundTag("c")), energy.getDouble("k"));
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof AuraBuffer that)) return false;

        return energies.equals(that.energies);
    }

    @Override
    public int hashCode() {
        return energies.hashCode();
    }

    @Override
    public String toString() {
        return "AuraBuffer{" + "energies=" + energies + '}';
    }
}
