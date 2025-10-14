package magicmod.common.materials;

import magicmod.MagicMod;
import materiallib.api.repository.StandardMaterialRepository;

public class MagicModMaterialRepo extends StandardMaterialRepository<MagicModMaterial> {

    public static final MagicModMaterialRepo INSTANCE = new MagicModMaterialRepo();

    public MagicModMaterialRepo() {
        super(MagicMod.MODID);
    }
}
