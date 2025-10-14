package magicmod.common.materials;

import java.util.HashSet;

import org.jetbrains.annotations.Nullable;

import materiallib.api.enums.OrePrefix;
import materiallib.api.material.IMaterialHandle;
import materiallib.api.material.StandardMaterial;
import materiallib.api.material.metadata.MaterialColorMetaKey;
import materiallib.api.material.metadata.MaterialMetadata;
import materiallib.api.material.metadata.TextureSetMetaKey;
import materiallib.api.rendering.texture_set.ITextureSet;
import materiallib.api.util.ImmutableColor;

public class MagicModMaterial extends StandardMaterial {

    private ImmutableColor color;
    private ITextureSet textureSet;
    private final HashSet<OrePrefix> disabledPrefixes = new HashSet<>();

    public MagicModMaterial(MagicModMaterialIds id, String name) {
        super(MagicModMaterialRepo.INSTANCE, id.id, name);
    }

    public MagicModMaterial setColor(ImmutableColor color) {
        this.color = color;
        return this;
    }

    public MagicModMaterial setTextureSet(ITextureSet textureSet) {
        this.textureSet = textureSet;
        return this;
    }

    public MagicModMaterial disablePrefix(OrePrefix prefix) {
        this.disabledPrefixes.add(prefix);
        return this;
    }

    public void finish(IMaterialHandle<MagicModMaterial> handle) {
        handle.setMaterial(this);
    }

    @Override
    public boolean shouldGenerateItem(OrePrefix prefix) {
        return !disabledPrefixes.contains(prefix);
    }

    @Override
    public <T> @Nullable T getMeta(MaterialMetadata<T> key) {
        if (key == MaterialColorMetaKey.INSTANCE) return key.cast(color);
        if (key == TextureSetMetaKey.INSTANCE) return key.cast(textureSet);


        return super.getMeta(key);
    }
}
