package magicmod.common.runes;

import static magicmod.common.runes.impl.SimpleRune.SIMPLE_RUNE;

import java.util.function.Supplier;

import net.minecraft.util.IIcon;

import cpw.mods.fml.common.registry.GameRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import magicmod.MagicMod;
import magicmod.common.runes.impl.AuraFieldRune;
import magicmod.common.runes.impl.DohRune;
import magicmod.common.runes.impl.ExtractRune;
import magicmod.common.runes.impl.InputRune;
import magicmod.common.runes.impl.LahRune;
import magicmod.common.runes.impl.OrderRune;
import magicmod.common.runes.impl.OriginRune;
import magicmod.common.runes.impl.OutputRune;
import magicmod.common.runes.impl.RuneA;
import magicmod.common.runes.impl.SimpleRune;
import magicmod.common.runes.impl.TohRune;
import magicmod.common.runes.impl.TransferRune;
import magicmod.common.tiles.TileEntityRune;

public enum Rune {
    A(0, RuneA::new),
    Input(1, InputRune::new),
    Output(2, OutputRune::new),
    Transfer(3, TransferRune::new),
    Extract(4, ExtractRune::new),
    AuraField(5, AuraFieldRune::new),
    Order(6, OrderRune::new),
    Toh(7, TohRune::new),
    Lah(8, LahRune::new),
    Doh(9, DohRune::new),
    Origin(10, OriginRune::new),
    //
    ;

    public final int id;
    private final Supplier<TileEntityRune> runeSupplier;

    public IIcon icon;

    static {
        GameRegistry.registerTileEntity(SimpleRune.class, MagicMod.MODID + ":rune.simple");
    }

    Rune(int id, Supplier<TileEntityRune> runeSupplier) {
        this.id = id;
        this.runeSupplier = runeSupplier;

        if (runeSupplier != SIMPLE_RUNE) {
            GameRegistry.registerTileEntity(runeSupplier.get().getClass(), MagicMod.MODID + ":rune." + id);
        }
    }

    public TileEntityRune createInstance() {
        return runeSupplier.get();
    }

    public static final Int2ObjectOpenHashMap<Rune> RUNES_BY_ID = new Int2ObjectOpenHashMap<>();

    static {
        for (Rune rune : values()) {
            RUNES_BY_ID.put(rune.id, rune);
        }
    }
}
