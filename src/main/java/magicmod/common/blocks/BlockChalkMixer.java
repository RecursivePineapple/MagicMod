package magicmod.common.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.AbstractUIFactory;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.gtnewhorizon.gtnhlib.blockstate.properties.DirectionBlockProperty;
import com.gtnewhorizon.gtnhlib.capability.Capabilities;
import com.gtnewhorizon.gtnhlib.client.model.ModelISBRH;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import magicmod.common.tiles.TileEntityChalkMixer;

public class BlockChalkMixer extends BlockContainer {

    public static final BlockChalkMixer INSTANCE = new BlockChalkMixer();

    public static final DirectionBlockProperty FACING = DirectionBlockProperty.facing(0b11, 0, 1, 2, 3, -1, -1);

    public BlockChalkMixer() {
        super(Material.iron);

        setBlockName("chalk-mixer");

        float pixel = 1f /16f;

        setBlockBounds(
            pixel * 1, 0, pixel * 1,
            pixel * 15, pixel * 8, pixel * 15);
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
        return new TileEntityChalkMixer();
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {
        super.onBlockPlacedBy(worldIn, x, y, z, placer, itemIn);

        int direction = (int) (((placer.rotationYaw + 45f) / 90f + 4f) % 4f);

        FACING.setValue(worldIn, x, y, z, switch (direction) {
            case 0 -> ForgeDirection.SOUTH;
            case 1 -> ForgeDirection.WEST;
            case 2 -> ForgeDirection.NORTH;
            case 3 -> ForgeDirection.EAST;
            default -> ForgeDirection.SOUTH;
        });
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX, float subY, float subZ) {
        if (!worldIn.isRemote) {
            PosGuiData data = new PosGuiData(player, x, y, z);
            GuiManager.open(GuiHandler.INSTANCE, data, (EntityPlayerMP) player);
        }

        return true;
    }

    public static class GuiHandler extends AbstractUIFactory<PosGuiData> {

        public static final GuiHandler INSTANCE = new GuiHandler();

        public static final int MAX_INTERACTION_DISTANCE = 8 * 8;

        public GuiHandler() {
            super("magicmod:chalk-mixer");
        }

        @SuppressWarnings("unchecked")
        @Override
        public @NotNull IGuiHolder<PosGuiData> getGuiHolder(PosGuiData data) {
            TileEntity te = data.getTileEntity();

            return (IGuiHolder<PosGuiData>) Capabilities.getCapability(te, IGuiHolder.class);
        }

        @Override
        public boolean canInteractWith(EntityPlayer player, PosGuiData guiData) {
            return super.canInteractWith(player, guiData) && guiData.getTileEntity() instanceof TileEntityChalkMixer
                && guiData.getSquaredDistance(player) <= MAX_INTERACTION_DISTANCE;
        }

        @Override
        public void writeGuiData(PosGuiData guiData, PacketBuffer buffer) {
            buffer.writeVarIntToBuffer(guiData.getX());
            buffer.writeVarIntToBuffer(guiData.getY());
            buffer.writeVarIntToBuffer(guiData.getZ());
        }

        @Override
        public @NotNull PosGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
            return new PosGuiData(
                player,
                buffer.readVarIntFromBuffer(),
                buffer.readVarIntFromBuffer(),
                buffer.readVarIntFromBuffer());
        }
    }
}
