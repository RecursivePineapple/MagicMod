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
        new MagicModMaterial(MagicMaterials.Gypsum)
            .setColor(Color.fromRGB(0x999999))
            .setTextureSet(MagicModTextureSet.DULL);

        new MagicModMaterial(MagicMaterials.AuraStone)
            .setColor(Color.fromRGB(0x99AADD))
            .setTextureSet(MagicModTextureSet.DULL)
            .disablePrefix(OrePrefix.gem);

        new MagicModMaterial(MagicMaterials.Air)
            .setColor(Color.fromRGB(0xddcb63))
            .setTextureSet(MagicModTextureSet.DULL)
            .disablePrefix(OrePrefix.gem);

        new MagicModMaterial(MagicMaterials.Fire)
            .setColor(Color.fromRGB(0xdd1d22))
            .setTextureSet(MagicModTextureSet.DULL)
            .disablePrefix(OrePrefix.gem);

        new MagicModMaterial(MagicMaterials.Water)
            .setColor(Color.fromRGB(0x2b3add))
            .setTextureSet(MagicModTextureSet.DULL)
            .disablePrefix(OrePrefix.gem);

        new MagicModMaterial(MagicMaterials.Earth)
            .setColor(Color.fromRGB(0x6cd21c))
            .setTextureSet(MagicModTextureSet.DULL)
            .disablePrefix(OrePrefix.gem);

        new MagicModMaterial(MagicMaterials.Order)
            .setColor(Color.fromRGB(0xe3edf0))
            .setTextureSet(MagicModTextureSet.DULL)
            .disablePrefix(OrePrefix.gem);

        new MagicModMaterial(MagicMaterials.Entropy)
            .setColor(Color.fromRGB(0x202232))
            .setTextureSet(MagicModTextureSet.DULL)
            .disablePrefix(OrePrefix.gem);

        MagicModMaterialRepo.INSTANCE.registerFromList(MagicMaterials.class);
    }
}
