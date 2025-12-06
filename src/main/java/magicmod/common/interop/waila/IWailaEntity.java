package magicmod.common.interop.waila;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;

public interface IWailaEntity {

    void getNBTData(EntityPlayerMP player, NBTTagCompound tag, World world);

    void getWailaBody(List<String> tooltip, IWailaEntityAccessor accessor, IWailaConfigHandler config);
}
