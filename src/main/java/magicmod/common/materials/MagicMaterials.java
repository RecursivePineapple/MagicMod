package magicmod.common.materials;

import materiallib.api.material.IMaterialHandle;

public enum MagicMaterials implements IMaterialHandle<MagicModMaterial> {
    Gypsum,
    AuraStone,
    //
    ;

    private MagicModMaterial material;

    @Override
    public MagicModMaterial getMaterial() {
        return material;
    }

    @Override
    public void setMaterial(MagicModMaterial material) {
        this.material = material;
    }
}
