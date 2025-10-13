package magicmod.common.interop.waila;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import magicmod.MagicMod;
import magicmod.common.tiles.TileEntityRune;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;

public class RuneWailaDataProvider implements IWailaDataProvider {

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        if (accessor.getTileEntity() instanceof TileEntityRune rune) {
            try {
                rune.getWailaBody(itemStack, currenttip, accessor, config);
            } catch (Throwable t) {
                // waila doesn't print a useful stacktrace, so catch the error and rethrow it
                MagicMod.LOG.error("Could not call getWailaBody on {}", rune, t);
                throw t;
            }
        }

        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x,
        int y, int z) {
        if (tile instanceof TileEntityRune rune) {
            try {
                rune.getWailaNBTData(player, tile, tag, world, x, y, z);
            } catch (Throwable t) {
                MagicMod.LOG.error("Could not call getWailaNBTData on {}", tile, t);
                throw t;
            }
        }

        return tag;
    }
}
