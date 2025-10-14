package magicmod.common.materials;

import materiallib.api.annotations.LoadedEagerly;
import materiallib.api.enums.OrePrefix;
import materiallib.api.util.Color;

@LoadedEagerly
public class MagicMaterialInit {

    static {
        init();
    }

    private static void init() {
        new MagicModMaterial(MagicModMaterialIds.Gypsum, "Gypsum")
            .setColor(Color.fromRGB(0x999999))
            .setTextureSet(MagicModTextureSet.DULL)
            .finish(MagicMaterials.Gypsum);

        new MagicModMaterial(MagicModMaterialIds.AuraStone, "AuraStone")
            .setColor(Color.fromRGB(0x99AADD))
            .setTextureSet(MagicModTextureSet.DULL)
            .disablePrefix(OrePrefix.gem)
            .finish(MagicMaterials.AuraStone);

        MagicModMaterialRepo.INSTANCE.registerFromList(MagicMaterials.class);
    }
}
