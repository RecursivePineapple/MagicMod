package magicmod;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.factory.GuiManager;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import magicmod.common.blocks.BlockChalkMixer;
import magicmod.common.blocks.BlockItemHolder;
import magicmod.common.blocks.BlockRune;
import magicmod.common.interop.waila.WailaInit;
import magicmod.common.items.ItemAuraStone;
import magicmod.common.items.ItemChalk;
import magicmod.common.items.ItemRune;
import magicmod.common.mechanics.AuraBuffer;
import magicmod.common.mechanics.AuraDistribution;
import magicmod.common.mechanics.BaseConcept;
import magicmod.common.mechanics.aura.AuraFieldRegistry;
import magicmod.common.mechanics.aura.NodeAuraField;
import magicmod.common.mechanics.chalk.ChalkRegistry;
import magicmod.common.mechanics.chalk.ChalkTrait;
import magicmod.common.mechanics.chalk.IChalkModifier;
import magicmod.common.tiles.TileEntityChalkMixer;
import magicmod.common.util.Curve;
import magicmod.common.util.Mods;
import magicmod.server.commands.AuraFieldCommand;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());

        GuiManager.registerFactory(BlockChalkMixer.GuiHandler.INSTANCE);
    }

    public void init(FMLInitializationEvent event) {
        GameRegistry.registerItem(ItemChalk.INSTANCE, "chalk");
        GameRegistry.registerItem(ItemAuraStone.INSTANCE, "aura-stone");

        GameRegistry.registerBlock(BlockRune.INSTANCE, ItemRune.class, "rune");
        GameRegistry.registerBlock(BlockItemHolder.INSTANCE, "item-holder");

        GameRegistry.registerBlock(BlockChalkMixer.INSTANCE, "chalk-mixer");
        GameRegistry.registerTileEntity(TileEntityChalkMixer.class, "chalk-mixer");

        Item gypsum = new Item();
        gypsum.setUnlocalizedName("gypsum");
        GameRegistry.registerItem(gypsum, "gypsum");
        ChalkRegistry.registerChalkBase(new ItemStack(gypsum, 1));

        Item stuff = new Item();
        stuff.setUnlocalizedName("stuff");
        GameRegistry.registerItem(stuff, "stuff");
        ChalkRegistry.registerChalkDopant(
            new ItemStack(stuff, 1), new IChalkModifier() {

                @Override
                public @Nullable AuraDistribution getAuraDistribution() {
                    return null;
                }

                @Override
                public @NotNull List<ChalkTrait> getTraits() {
                    return new ArrayList<>();
                }

                @Override
                public AuraBuffer getRequiredMixingAura() {
                    return new AuraBuffer();
                }
            });

        if (Mods.Waila.isModLoaded()) {
            WailaInit.init();
        }

        AuraFieldRegistry.registerFactory(
            NodeAuraField.factory(
                "order",
                BaseConcept.Order,
                0.6f,
                Curve.normal(10, 500, 100, 50),
                Curve.normal(25, 2000, 100, 100)));
    }

    public void postInit(FMLPostInitializationEvent event) {}

    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new AuraFieldCommand());
    }
}
