package magicmod;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.factory.GuiManager;
import com.gtnewhorizon.gtnhlib.blockstate.core.BlockProperty;
import com.gtnewhorizon.gtnhlib.blockstate.registry.BlockPropertyRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import magicmod.common.blocks.BlockChalkMixer;
import magicmod.common.blocks.BlockItemHolder;
import magicmod.common.blocks.BlockRune;
import magicmod.common.interop.waila.WailaInit;
import magicmod.common.items.ItemAuraStone;
import magicmod.common.items.ItemChalk;
import magicmod.common.items.ItemRune;
import magicmod.common.materials.MagicMaterials;
import magicmod.common.mechanics.AuraBuffer;
import magicmod.common.mechanics.AuraDistribution;
import magicmod.common.mechanics.BaseConcept;
import magicmod.common.mechanics.aura.AuraFieldRegistry;
import magicmod.common.mechanics.aura.NodeAuraField;
import magicmod.common.mechanics.chalk.ChalkRegistry;
import magicmod.common.mechanics.chalk.ChalkTrait;
import magicmod.common.mechanics.chalk.IChalkModifier;
import magicmod.common.mechanics.rifts.AuraRiftSpawner;
import magicmod.common.mechanics.rifts.EntityAuraRift;
import magicmod.common.tiles.TileEntityChalkMixer;
import magicmod.common.util.Curve;
import magicmod.common.util.Mods;
import magicmod.common.worldgen.structure.core.StructurePieceCommand;
import magicmod.server.commands.AuraFieldCommand;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());

        GuiManager.registerFactory(BlockChalkMixer.GuiHandler.INSTANCE);

        EntityRegistry.registerModEntity(EntityAuraRift.class, "aura-rift", 0, MagicMod.MODID, 64, 20, false);
    }

    public void init(FMLInitializationEvent event) {
        GameRegistry.registerItem(ItemChalk.INSTANCE, "chalk");

        GameRegistry.registerItem(ItemAuraStone.INSTANCE, "aura-stone");
        OreDictionary.registerOre("gemAuraStone", new ItemStack(ItemAuraStone.INSTANCE, 1));

        GameRegistry.registerBlock(BlockRune.INSTANCE, ItemRune.class, "rune");
        GameRegistry.registerBlock(BlockItemHolder.INSTANCE, "item-holder");

        GameRegistry.registerBlock(BlockChalkMixer.INSTANCE, "chalk-mixer");
        GameRegistry.registerTileEntity(TileEntityChalkMixer.class, "chalk-mixer");

        BlockPropertyRegistry.registerProperty(BlockChalkMixer.INSTANCE, BlockChalkMixer.FACING);
        BlockPropertyRegistry.registerProperty(
            Item.getItemFromBlock(BlockChalkMixer.INSTANCE),
            BlockProperty.constant("facing", ForgeDirection.NORTH));

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

        GameRegistry.registerWorldGenerator(AuraRiftSpawner.INSTANCE, Integer.MAX_VALUE);
    }

    public void postInit(FMLPostInitializationEvent event) {
        ItemStack gypsum = MagicMaterials.Gypsum.getDust(1);

        ChalkRegistry.registerChalkBase(
            gypsum, new IChalkModifier() {

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
                    AuraBuffer aura = new AuraBuffer();
                    aura.add(BaseConcept.Fire, 10);
                    return aura;
                }
            });

        ItemStack auraStoneDust = MagicMaterials.AuraStone.getDust(1);

        ChalkRegistry.registerChalkDopant(
            auraStoneDust, new IChalkModifier() {

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
                    AuraBuffer aura = new AuraBuffer();
                    aura.add(BaseConcept.Order, 10);
                    return aura;
                }
            });
    }

    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new AuraFieldCommand());
        event.registerServerCommand(new StructurePieceCommand());
    }
}
