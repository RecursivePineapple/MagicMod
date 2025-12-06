package magicmod.common.materials;

import materiallib.api.material.IMaterialHandle;

public enum MagicMaterials implements IMaterialHandle<MagicModMaterial> {
    Gypsum(0),
    AuraStone(1),
    Air(2),
    Fire(3),
    Water(4),
    Earth(5),
    Order(6),
    Entropy(7),
    //
    ;

    public final int id;

    MagicMaterials(int id) {
        this.id = id;
    }

    private MagicModMaterial material;

    @Override
    public MagicModMaterial getMaterial() {
        return material;
    }

    @Override
    public void setMaterial(MagicModMaterial material) {
        if (this.material != null) throw new IllegalStateException("Duplicate material registration: " + name());

        this.material = material;
    }
}
