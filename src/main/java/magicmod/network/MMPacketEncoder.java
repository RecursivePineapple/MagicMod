package magicmod.network;

import net.minecraft.network.INetHandler;
import net.minecraft.world.World;

public abstract class MMPacketEncoder<Packet extends MMPacket> {

    protected MMPacketEncoder() {}

    /**
     * Unique ID of this packet.
     */
    public abstract int getPacketID();

    /**
     * Encode the data into given byte buffer.
     */
    public abstract void writePacket(MMPacketBuffer buffer, Packet packet);

    /**
     * Decode byte buffer into packet object.
     */
    public abstract Packet readPacket(MMPacketBuffer buffer);

    /**
     * Process the received packet.
     *
     * @param world null if message is received on server side, the client world if message is received on client side
     */
    public abstract void process(World world, Packet packet);

    /**
     * This will be called just before {@link #process(World, MMPacket)}} to inform the handler about the source and
     * type of
     * connection.
     */
    public void setINetHandler(INetHandler handler, Packet packet) {}
}
