package magicmod.mixin;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

public enum Mixins implements IMixins {
    STRUCTURE_REDIRECTS(new MixinBuilder()
        .addCommonMixins("minecraft.MixinMapGenStructure", "minecraft.MixinStructureStart")
        .setPhase(Phase.EARLY)),
    WORLD_EXT(new MixinBuilder()
        .addCommonMixins("minecraft.MixinWorld")
        .setPhase(Phase.EARLY)),
    AURA_RIFT_SPAWNING(new MixinBuilder()
        .addCommonMixins("minecraft.MixinWorldGenLakes")
        .setPhase(Phase.EARLY)),
    //
    ;

    private final MixinBuilder builder;

    Mixins(MixinBuilder builder) {
        this.builder = builder;
    }

    @Override
    public @NotNull MixinBuilder getBuilder() {
        return builder;
    }
}
