package magicmod.common.mechanics.rifts;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;

import org.jetbrains.annotations.ApiStatus;

import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lombok.Getter;
import magicmod.common.interop.waila.IWailaEntity;
import magicmod.common.mechanics.AuraBuffer;
import magicmod.common.mechanics.AuraStack;
import magicmod.common.mechanics.IAuraBuffer;
import magicmod.common.util.DataUtils;
import magicmod.network.NetworkChannel;
import magicmod.network.packets.PacketEncoderAuraRiftStatus;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;

@EventBusSubscriber
public class EntityAuraRift extends Entity implements IAuraRift, IWailaEntity {

    @Getter
    private final AuraBuffer production = new AuraBuffer();

    private final AuraBuffer buffer = new AuraBuffer();

    private double capacity;

    @ApiStatus.Internal
    public EntityAuraRift(World world) {
        super(world);

        setSize(2, 5);
    }

    public EntityAuraRift(World worldIn, AuraBuffer production, double capacity) {
        this(worldIn);
        this.production.addAll(production);
        this.capacity = capacity;
    }

    @SubscribeEvent
    public static void watchRift(PlayerEvent.StartTracking event) {
        if (event.target instanceof EntityAuraRift rift) {
            NetworkChannel.CHANNEL.sendPacketToPlayersWatching(
                rift.worldObj,
                PacketEncoderAuraRiftStatus.create(rift),
                MathHelper.floor_double(rift.posX),
                MathHelper.floor_double(rift.posZ));
        }
    }

    public void setProduction(AuraBuffer production) {
        this.production.clear();
        this.production.addAll(production);

        if (!worldObj.isRemote) {
            NetworkChannel.CHANNEL.sendPacketToPlayersWatching(
                worldObj,
                PacketEncoderAuraRiftStatus.create(this),
                MathHelper.floor_double(posX),
                MathHelper.floor_double(posZ));
        }
    }

    @Override
    protected void entityInit() {

    }

    @Override
    public void travelToDimension(int dimensionId) {

    }

    @Override
    public boolean handleWaterMovement() {
        return false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();

        double k = DataUtils.clamp(buffer.getTotalAmount() / capacity, 0, 1);

        double rate = (Math.sin(Math.PI * (2 * k + 1)) + 1) / 2;

        for (AuraStack stack : this.production) {
            buffer.add(stack.concept, stack.amount * rate);
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        production.readFromNBT(tag.getCompoundTag("prod"));
        buffer.readFromNBT(tag.getCompoundTag("buffer"));
        capacity = tag.getDouble("capacity");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        tag.setTag("prod", production.writeToNBT(new NBTTagCompound()));
        tag.setTag("buffer", buffer.writeToNBT(new NBTTagCompound()));
        tag.setDouble("capacity", capacity);
    }

    @Override
    public IAuraBuffer getBuffer() {
        return buffer;
    }

    @Override
    public int getBlockX() {
        return MathHelper.floor_double(this.posX);
    }

    @Override
    public int getBlockY() {
        return MathHelper.floor_double(this.posY);
    }

    @Override
    public int getBlockZ() {
        return MathHelper.floor_double(this.posZ);
    }

    @Override
    public int getBlockRadius() {
        return 32;
    }

    @Override
    public void getNBTData(EntityPlayerMP player, NBTTagCompound tag, World world) {
        tag.setTag("production", production.writeToNBT(new NBTTagCompound()));
        tag.setTag("buffer", buffer.writeToNBT(new NBTTagCompound()));
        tag.setDouble("capacity", capacity);
    }

    @Override
    public void getWailaBody(List<String> tooltip, IWailaEntityAccessor accessor,
        IWailaConfigHandler config) {
        AuraBuffer production = new AuraBuffer();
        AuraBuffer buffer = new AuraBuffer();

        production.readFromNBT(accessor.getNBTData().getCompoundTag("production"));
        buffer.readFromNBT(accessor.getNBTData().getCompoundTag("buffer"));

        tooltip.add("Production: " + production);
        tooltip.add("Buffer: " + buffer);
        tooltip.add("Capacity: " + accessor.getNBTData().getDouble("capacity"));
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }
}
