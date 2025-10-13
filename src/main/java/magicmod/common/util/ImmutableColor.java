package magicmod.common.util;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface ImmutableColor {

    int getRed();
    int getGreen();
    int getBlue();
    int getAlpha();

    default int toIntRGB() {
        return ((getRed() & 0xFF) << 16) | ((getGreen() & 0xFF) << 8) | (getBlue() & 0xFF);
    }

    default int toIntARGB() {
        return ((getAlpha() & 0xFF) << 24) | ((getRed() & 0xFF) << 16) | ((getGreen() & 0xFF) << 8) | (getBlue() & 0xFF);
    }

    default int toIntRGBA() {
        return  ((getRed() & 0xFF) << 24) | ((getGreen() & 0xFF) << 16) | ((getBlue() & 0xFF) << 8) | ((getAlpha() & 0xFF));
    }

    default Color toMutable() {
        return new Color(getRed(), getGreen(), getBlue(), getAlpha());
    }

    default HSVColor toHSV() {
        float[] hsv = java.awt.Color.RGBtoHSB(getRed(), getGreen(), getBlue(), null);

        return new HSVColor(hsv[0], hsv[1], hsv[2]);
    }

    @SideOnly(Side.CLIENT)
    default void makeActive() {
        GL11.glColor4f(getRed() / 256f, getGreen() / 256f, getBlue() / 256f, getAlpha() / 256f);
    }

    default short[] toShorts() {
        return new short[] { (short) getRed(), (short) getGreen(), (short) getBlue(), (short) getAlpha() };
    }

    static HSVColor lerp(ImmutableColor a, ImmutableColor b, float k) {
        HSVColor hsvA = a instanceof HSVColor hsv ? hsv : a.toHSV();
        HSVColor hsvB = b instanceof HSVColor hsv ? hsv : b.toHSV();

        return new HSVColor(
            DataUtils.lerp(hsvA.hue, hsvB.hue, k),
            DataUtils.lerp(hsvA.saturation, hsvB.saturation, k),
            DataUtils.lerp(hsvA.brightness, hsvB.brightness, k));
    }
}
