package magicmod.common.mechanics;

import net.minecraft.nbt.NBTTagCompound;

import magicmod.common.util.Color;
import magicmod.common.util.ImmutableColor;

public enum BaseConcept implements IConcept {
    Order(Color.fromRGB(0xE1E1FF)),
    Entropy(Color.fromRGB(0x192332)),
    Air(Color.fromRGB(0xbeb723)),
    Fire(Color.fromRGB(0xfa5d64)),
    Water(Color.fromRGB(0x4e5be4)),
    Earth(Color.fromRGB(0x3f9f39)),
    Motion(Color.fromRGB(0x839bbe)),
    //
    ;

    public final ImmutableColor color;

    BaseConcept(ImmutableColor color) {
        this.color = color;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger("type", 1);
        tag.setString("n", name());
    }

    @Override
    public ImmutableColor getColor() {
        return color;
    }

    public static BaseConcept readFromNBT(NBTTagCompound tag) {
        return valueOf(tag.getString("n"));
    }
}
