package magicmod.common.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import magicmod.MagicMod;
import magicmod.client.rendering.RuneISBRH;
import magicmod.common.items.ItemChalk;
import magicmod.common.runes.Rune;
import magicmod.common.tiles.TileEntityRune;

public class BlockRune extends BlockContainer {

    public static final BlockRune INSTANCE = new BlockRune();

    private static final double CHALK_HEIGHT = 0.1;
    private static final double CHALK_CONN_WIDTH = 0.2;
    private static final double CHALK_CONN_LENGTH = 0.4;

    private static final AxisAlignedBB[] CHALK_CONNECTIONS = {
        AxisAlignedBB.getBoundingBox(
            0.5 - CHALK_CONN_WIDTH, 0, 0,
            0.5 + CHALK_CONN_WIDTH, CHALK_HEIGHT, CHALK_CONN_LENGTH),
        AxisAlignedBB.getBoundingBox(
            1 - CHALK_CONN_LENGTH, 0, 0.5 - CHALK_CONN_WIDTH,
            1, CHALK_HEIGHT, 0.5 + CHALK_CONN_WIDTH),
        AxisAlignedBB.getBoundingBox(
            0.5 - CHALK_CONN_WIDTH, 0, 1 - CHALK_CONN_LENGTH,
            0.5 + CHALK_CONN_WIDTH, CHALK_HEIGHT, 1),
        AxisAlignedBB.getBoundingBox(
            0, 0, 0.5 - CHALK_CONN_WIDTH,
            CHALK_CONN_LENGTH, CHALK_HEIGHT, 0.5 + CHALK_CONN_WIDTH),
        AxisAlignedBB.getBoundingBox(
            0.5, 0, 0.5,
            0.5, CHALK_HEIGHT, 0.5)
            .expand(0.5 - CHALK_CONN_LENGTH, 0, 0.5 - CHALK_CONN_LENGTH),
    };

    public enum BoundingBox {
        North,
        East,
        South,
        West,
        Center;

        public ForgeDirection getDirection() {
            return switch (this) {
                case North -> ForgeDirection.NORTH;
                case East -> ForgeDirection.EAST;
                case South -> ForgeDirection.SOUTH;
                case West -> ForgeDirection.WEST;
                case Center -> ForgeDirection.UNKNOWN;
            };
        }
    }

    private BlockRune() {
        super(Material.sand);

        setBlockName("rune");
        setLightOpacity(0);
        setBlockBounds(0, 0, 0, 1, 0.1f, 1);
        setHardness(0);
        setResistance(0);
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return Rune.RUNES_BY_ID.containsKey(metadata);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return Rune.RUNES_BY_ID.get(meta).createInstance();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderType() {
        return RuneISBRH.INSTANCE.id;
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
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        ItemStack held = player.getHeldItem();

        if (held != null && held.getItem() == ItemChalk.INSTANCE) {
            Vec3 start = player.getPosition(0);
            Vec3 look = player.getLookVec();

            float dist = Minecraft.getMinecraft().playerController.getBlockReachDistance();

            Vec3 end = Vec3.createVectorHelper(start.xCoord + look.xCoord * dist, start.yCoord + look.yCoord * dist, start.zCoord + look.zCoord * dist);

            AxisAlignedBB pooled = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);

            for (AxisAlignedBB bb : CHALK_CONNECTIONS) {
                pooled.setBB(bb);
                pooled.offset(x, y, z);

                MovingObjectPosition hit = pooled.calculateIntercept(start, end);

                if (hit != null) return pooled;
            }
        }

        return super.getSelectedBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return null;
    }

    @Override
    public boolean canBlockStay(World world, int x, int y, int z) {
        return world.getBlock(x, y - 1, z).isSideSolid(world, x, y - 1, z, ForgeDirection.UP);
    }

    @Override
    public void registerBlockIcons(IIconRegister reg) {
        for (Rune rune : Rune.values()) {
            rune.icon = reg.registerIcon(MagicMod.MODID + ":runes/" + rune.name());
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        Rune rune = Rune.RUNES_BY_ID.get(meta);

        return rune == null ? Blocks.stone.getIcon(0, 0) : rune.icon;
    }

    @Override
    public int damageDropped(int meta) {
        return meta;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float subX, float subY, float subZ) {
        ItemStack held = player.getHeldItem();

        if (!(held.getItem() == ItemChalk.INSTANCE)) return false;

        if (world.isRemote) return true;

        Vec3 start = player.getPosition(0);
        Vec3 look = player.getLookVec();

        start.yCoord += player.getEyeHeight();

        float dist = Minecraft.getMinecraft().playerController.getBlockReachDistance();

        Vec3 end = Vec3.createVectorHelper(start.xCoord + look.xCoord * dist, start.yCoord + look.yCoord * dist, start.zCoord + look.zCoord * dist);

        AxisAlignedBB pooled = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);

        for (int i = 0; i < CHALK_CONNECTIONS.length; i++) {
            AxisAlignedBB bb = CHALK_CONNECTIONS[i];
            pooled.setBB(bb);
            pooled.offset(x, y, z);

            MovingObjectPosition hit = pooled.calculateIntercept(start, end);

            if (hit != null) {
                ((TileEntityRune) world.getTileEntity(x, y, z)).onUseChalk(player, held, BoundingBox.values()[i]);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onBlockPreDestroy(World worldIn, int x, int y, int z, int meta) {
        super.onBlockPreDestroy(worldIn, x, y, z, meta);

        if (worldIn.getTileEntity(x, y, z) instanceof TileEntityRune rune) {
            rune.onBlockPreDestroy();
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        if (worldIn.getTileEntity(x, y, z) instanceof TileEntityRune rune) {
            rune.onBlockPlaced();
        }
    }
}
