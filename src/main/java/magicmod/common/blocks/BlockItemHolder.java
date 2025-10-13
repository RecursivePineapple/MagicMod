package magicmod.common.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import com.gtnewhorizon.gtnhlib.client.model.ModelLoader;
import com.gtnewhorizon.gtnhlib.client.model.Variant;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import magicmod.MagicMod;
import magicmod.client.rendering.ItemHolderISBRH;

@SuppressWarnings("UnstableApiUsage")
public class BlockItemHolder extends BlockContainer {
    public static final Variant MODEL = new Variant(new ResourceLocation(MagicMod.MODID, "models/item_holder.json"), 0, 0, 0, false);

    public static final BlockItemHolder INSTANCE = new BlockItemHolder();

    protected BlockItemHolder() {
        super(Material.rock);

        setBlockName("item-holder");

        ModelLoader.registerModels(() -> {}, MODEL);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderType() {
        return ItemHolderISBRH.INSTANCE.id;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return null;
    }
}
