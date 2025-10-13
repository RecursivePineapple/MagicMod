package magicmod.common.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.AbstractUIFactory;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.gtnewhorizon.gtnhlib.capability.Capabilities;
import magicmod.common.tiles.TileEntityChalkMixer;

public class BlockChalkMixer extends BlockContainer {

    public static final BlockChalkMixer INSTANCE = new BlockChalkMixer();

    public BlockChalkMixer() {
        super(Material.iron);

        setBlockName("chalk-mixer");
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityChalkMixer();
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
