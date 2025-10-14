package magicmod.common.items;

import java.util.Arrays;

import magicmod.MagicMod;
import magicmod.common.materials.MagicModMaterial;
import magicmod.common.materials.MagicModMaterialRepo;
import materiallib.api.annotations.LoadedEagerly;
import materiallib.api.enums.OrePrefix;
import materiallib.api.items.StandardMaterialMetaItem;

@LoadedEagerly
public class MagicMaterialMetaItem extends StandardMaterialMetaItem<MagicModMaterial> {

    public static final MagicMaterialMetaItem INSTANCE = new MagicMaterialMetaItem();

    public MagicMaterialMetaItem() {
        super("metaitem", Arrays.asList(OrePrefix.dust, OrePrefix.gem), MagicModMaterialRepo.INSTANCE, 0);
    }

    @Override
    public String getModContainerName() {
        return MagicMod.MODID;
    }
}
