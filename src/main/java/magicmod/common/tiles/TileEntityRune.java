package magicmod.common.tiles;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import magicmod.common.blocks.BlockRune;
import magicmod.common.factory.ArrayFactoryElement;
import magicmod.common.factory.ArrayFactoryGrid;
import magicmod.common.factory.ArrayFactoryNetwork;
import magicmod.common.factory.FormationFactoryGrid;
import magicmod.common.factory.FormationNeighbourAdder;
import magicmod.common.items.ItemChalk;
import magicmod.common.mechanics.AuraBuffer;
import magicmod.common.mechanics.IAuraBuffer;
import magicmod.common.mechanics.IConcept;
import magicmod.common.runes.Rune;
import magicmod.common.util.DataUtils;
import magicmod.common.util.MCUtils;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class TileEntityRune extends TileEntity implements ArrayFactoryElement {

    public final EnumSet<ForgeDirection> strongConnections = EnumSet.noneOf(ForgeDirection.class);
    public final EnumSet<ForgeDirection> weakConnections = EnumSet.noneOf(ForgeDirection.class);

    public ArrayFactoryNetwork arrayNetwork;

    public final AuraBuffer auraBuffer = new AuraBuffer();

    protected int tickCounter;

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        saveData(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        loadData(compound);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();

        saveData(tag);

        return tag.hasNoTags() ? null : new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, blockMetadata, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        loadData(pkt.func_148857_g());
        onStateChanged();
    }

    protected void saveData(NBTTagCompound tag) {
        tag.setLong("strong", DataUtils.enumSetToLong(strongConnections));
        tag.setLong("weak", DataUtils.enumSetToLong(weakConnections));

        NBTTagCompound aura = new NBTTagCompound();
        auraBuffer.writeToNBT(aura);
        if (!aura.hasNoTags()) tag.setTag("aura", aura);
    }

    protected void loadData(NBTTagCompound tag) {
        DataUtils.longToEnumSet(ForgeDirection.class, tag.getLong("strong"), strongConnections);
        DataUtils.longToEnumSet(ForgeDirection.class, tag.getLong("weak"), weakConnections);

        auraBuffer.readFromNBT(tag.getCompoundTag("aura"));
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        tickCounter++;

        if (!worldObj.isRemote) {
            if (tickCounter == 2) {
                ArrayFactoryGrid.INSTANCE.updateElement(this);
            }

            if (tickCounter > 2) {
                if (worldObj.getTotalWorldTime() % 20 == 0) {
                    doRuneTick();
                }
            }
        }
    }

    public void onBlockPreDestroy() {
        ArrayFactoryGrid.INSTANCE.removeElement(this);
    }

    public void onBlockPlaced() {
        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            if (side.offsetY != 0) continue;

            TileEntityRune adjacent = getRuneOnSide(side);

            if (adjacent == null) continue;

            if (adjacent.weakConnections.contains(side.getOpposite())) {
                this.weakConnections.add(side);
            } else if (adjacent.strongConnections.contains(side.getOpposite())) {
                this.strongConnections.add(side);
            }
        }
    }

    protected void doRuneTick() {

    }

    @Override
    public IAuraBuffer getInternalAuraBuffer() {
        return auraBuffer;
    }

    public IAuraBuffer getEffectiveAuraBuffer() {
        return arrayNetwork == null ? auraBuffer : arrayNetwork.auraBuffer;
    }

    public double getResonance(IConcept concept) {
        return worldObj.rand.nextDouble();
    }

    public void onUseChalk(EntityPlayer player, ItemStack chalk, BlockRune.BoundingBox bb) {
        switch (ItemChalk.getMode(chalk)) {
            case PlaceRune -> {

            }
            case ToggleStrong -> {
                chalk.damageItem(1, player);

                ForgeDirection side = bb.getDirection();

                boolean added = DataUtils.toggleValue(strongConnections, side);
                weakConnections.remove(side);

                TileEntityRune neighbour = getRuneOnSide(side);

                if (neighbour != null) {
                    neighbour.weakConnections.remove(side.getOpposite());
                    neighbour.strongConnections.remove(side.getOpposite());

                    if (added) neighbour.strongConnections.add(side.getOpposite());

                    neighbour.onStateChanged();
                }

                ArrayFactoryGrid.INSTANCE.updateElement(this);

                onStateChanged();
            }
            case ToggleWeak -> {
                chalk.damageItem(1, player);

                ForgeDirection side = bb.getDirection();

                boolean added = DataUtils.toggleValue(weakConnections, side);
                strongConnections.remove(side);

                TileEntityRune neighbour = getRuneOnSide(side);

                if (neighbour != null) {
                    neighbour.weakConnections.remove(side.getOpposite());
                    neighbour.strongConnections.remove(side.getOpposite());

                    if (added) neighbour.weakConnections.add(side.getOpposite());

                    neighbour.onStateChanged();
                }

                FormationFactoryGrid.INSTANCE.updateElement(arrayNetwork);

                onStateChanged();
            }
            case Debug -> {
                if (bb == BlockRune.BoundingBox.Center) {
                    MCUtils.sendChatMessage(player, "Internal: " + auraBuffer.toString());
                    MCUtils.sendChatMessage(player, "Network: " + getEffectiveAuraBuffer().toString());
                }
            }
        }
    }

    public void onStateChanged() {
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);

        if (!worldObj.isRemote) markDirty();
    }

    @Override
    public void getNeighbours(Collection<ArrayFactoryElement> neighbours) {
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (strongConnections.contains(dir)) {
                TileEntityRune neighbour = getRuneOnSide(dir);

                if (neighbour != null) {
                    neighbours.add(neighbour);
                }
            }
        }
    }

    @Override
    public void getFormationConnections(FormationNeighbourAdder adder) {
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (!weakConnections.contains(dir)) continue;

            TileEntityRune neighbour = getRuneOnSide(dir);

            if (neighbour == null) continue;
            if (neighbour.arrayNetwork == null || neighbour.arrayNetwork == this.arrayNetwork) continue;

            addFormationNeighbour(neighbour, adder);
        }
    }

    protected void addFormationNeighbour(TileEntityRune neighbour, FormationNeighbourAdder adder) {
        adder.addNeighbour(neighbour);
    }

    public TileEntityRune getRuneOnSide(ForgeDirection side) {
        TileEntity te = worldObj.getTileEntity(xCoord + side.offsetX, yCoord + side.offsetY, zCoord + side.offsetZ);

        return te instanceof TileEntityRune rune ? rune : null;
    }

    @Override
    public ArrayFactoryNetwork getNetwork() {
        return arrayNetwork;
    }

    @Override
    public void setNetwork(ArrayFactoryNetwork arrayFactoryNetwork) {
        this.arrayNetwork = arrayFactoryNetwork;
    }

    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y, int z) {
        tag.setString("array", Objects.toString(arrayNetwork));
        tag.setString("formation", Objects.toString(arrayNetwork == null ? null : arrayNetwork.formationNetwork));

        AuraBuffer temp = new AuraBuffer();
        temp.addAll(getEffectiveAuraBuffer());

        tag.setTag("aura", temp.writeToNBT(new NBTTagCompound()));
    }

    public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        currenttip.add("Array: " + accessor.getNBTData().getString("array"));
        currenttip.add("Formation: " + accessor.getNBTData().getString("formation"));

        AuraBuffer buffer = new AuraBuffer();
        buffer.readFromNBT(accessor.getNBTData().getCompoundTag("aura"));

        buffer.forEachConcept((concept, amount) -> {
            currenttip.add(concept + ": " + amount);

            return true;
        });
    }

    @Override
    public Rune getRune() {
        return Rune.RUNES_BY_ID.get(getBlockMetadata());
    }
}
