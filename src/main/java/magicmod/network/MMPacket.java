package magicmod.network;

import net.minecraft.entity.player.EntityPlayerMP;

public interface MMPacket {

    int getPacketID();

    default void sendToPlayer(EntityPlayerMP player) {
        NetworkChannel.CHANNEL.sendToPlayer(this, player);
    }
}
