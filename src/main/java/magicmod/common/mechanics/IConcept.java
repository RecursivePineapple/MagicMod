package magicmod.common.mechanics;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;

import materiallib.api.util.ImmutableColor;

public interface IConcept {

    default @Nullable BaseConcept getBaseConcept() {
        if (this instanceof BaseConcept base) return base;

        return null;
    }

    default @Nullable CompoundConcept getCompoundConcept() {
        if (this instanceof CompoundConcept compound) return compound;

        return null;
    }

    void writeToNBT(NBTTagCompound tag);

    ImmutableColor getColor();

    static IConcept readFromNBT(NBTTagCompound tag) {
        switch (tag.getInteger("t")) {
            case 0 -> {
                return BaseConcept.readFromNBT(tag);
            }
            case 1 -> {
                return CompoundConcept.readFromNBT(tag);
            }
            default -> {
                return null;
            }
        }
    }
}
