package magicmod.common.mechanics;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import magicmod.common.util.DataUtils;
import magicmod.common.util.HSVColor;
import magicmod.common.util.ImmutableColor;
import magicmod.common.util.MCUtils;

public class CompoundConcept implements IConcept {

    public final Object2IntOpenHashMap<IConcept> formula = new Object2IntOpenHashMap<>();

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        NBTTagList contents = new NBTTagList();

        Object2IntMaps.fastForEach(
            formula, e -> {
            NBTTagCompound entry = new NBTTagCompound();

            NBTTagCompound concept = new NBTTagCompound();
            e.getKey().writeToNBT(concept);

            entry.setTag("c", concept);
            entry.setInteger("k", e.getIntValue());

            contents.appendTag(entry);
        });

        if (!contents.tagList.isEmpty()) tag.setTag("c", contents);
    }

    public static CompoundConcept readFromNBT(NBTTagCompound tag) {
        CompoundConcept out = new CompoundConcept();

        for (NBTTagCompound concept : MCUtils.getTagList(tag, "c")) {
            out.formula.addTo(IConcept.readFromNBT(concept.getCompoundTag("c")), concept.getInteger("k"));
        }

        return out;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof CompoundConcept that)) return false;

        return formula.equals(that.formula);
    }

    @Override
    public ImmutableColor getColor() {
        List<HSVColor> colors = DataUtils.mapToList(this.formula.keySet(), c -> c.getColor().toHSV());

        HSVColor sum = new HSVColor();

        for (var color : colors) {
            sum.hue += color.hue;
            sum.saturation += color.saturation;
            sum.brightness += color.brightness;
        }

        sum.hue /= colors.size();
        sum.saturation /= colors.size();
        sum.brightness /= colors.size();

        return sum.toRGB();
    }

    @Override
    public int hashCode() {
        return formula.hashCode();
    }

    @Override
    public String toString() {
        return "CompoundConcept{" + "formula=" + formula + '}';
    }
}
