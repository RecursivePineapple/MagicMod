package magicmod.common.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.gtnewhorizon.gtnhlib.client.model.ModelISBRH;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import magicmod.common.util.MCUtils;

@SuppressWarnings("UnstableApiUsage")
public class BlockItemHolder extends BlockContainer {

    public static final BlockItemHolder INSTANCE = new BlockItemHolder();

    protected BlockItemHolder() {
        super(Material.rock);

        setBlockName("item-holder");

        MCUtils.setBlockBounds(this, 4, 0, 4, 12, 9, 12);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderType() {
        return ModelISBRH.JSON_ISBRH_ID;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister reg) {

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
