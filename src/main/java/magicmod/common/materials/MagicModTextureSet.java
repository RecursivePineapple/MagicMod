package magicmod.common.materials;

import net.minecraft.util.ResourceLocation;

import magicmod.MagicMod;
import materiallib.api.enums.OrePrefix;
import materiallib.api.rendering.texture_set.StandardTextureSet;

public class MagicModTextureSet extends StandardTextureSet {

    public static final MagicModTextureSet DULL = new MagicModTextureSet("dull");

    public MagicModTextureSet(String name) {
        super(null);

        addItemIcon(OrePrefix.dust, name);
        addItemIcon(OrePrefix.gem, name);

        autoRegisterIcons();
    }

    private void addItemIcon(OrePrefix prefix, String textureSet) {
        addItemIcon(prefix, new ResourceLocation(MagicMod.MODID, "texture-sets/" + textureSet + "/" + prefix.name()));
    }

    private void addBlockIcon(OrePrefix prefix, String textureSet) {
        addBlockIcon(prefix, new ResourceLocation(MagicMod.MODID, "texture-sets/" + textureSet + "/" + prefix.name()));
    }
}
