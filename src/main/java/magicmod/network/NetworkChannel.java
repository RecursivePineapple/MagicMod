package magicmod.network;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.MessageToMessageCodec;
import magicmod.MagicMod;

@SuppressWarnings("unused")
@ChannelHandler.Sharable
public class NetworkChannel extends MessageToMessageCodec<FMLProxyPacket, MMPacket> {

    private final EnumMap<Side, FMLEmbeddedChannel> channel;
    private final MMPacketEncoder<MMPacket>[] encoders;

    public static final NetworkChannel CHANNEL = new NetworkChannel();

    public NetworkChannel() {
        this.channel = NetworkRegistry.INSTANCE.newChannel(MagicMod.MODID + "1", this, new HandlerShared());

        MMPacketEncoder<?>[] packetTypes = Arrays.stream(MMPacketEntry.values())
            .map(e -> e.encoder)
            .toArray(MMPacketEncoder[]::new);

        final int maxPacketID = Arrays.stream(packetTypes)
            .mapToInt(MMPacketEncoder::getPacketID)
            .max()
            .getAsInt();

        // noinspection unchecked
        this.encoders = new MMPacketEncoder[maxPacketID + 1];

        for (MMPacketEncoder<?> packetType : packetTypes) {
            int packetID = packetType.getPacketID();
            if (this.encoders[packetID] == null) {
                // noinspection unchecked
                this.encoders[packetID] = (MMPacketEncoder<MMPacket>) packetType;
            } else {
                throw new IllegalArgumentException("Duplicate Packet ID! " + packetID);
            }
        }
    }

    public static void init() {
        // forces this class to be loaded
    }

    @Override
    protected void encode(ChannelHandlerContext context, MMPacket packet, List<Object> output) {
        ByteBuf backing = Unpooled.buffer();
        MMPacketBuffer buffer = new MMPacketBuffer(backing);
        buffer.writeVarIntToBuffer(packet.getPacketID());

        MMPacketEncoder<MMPacket> encoder = this.encoders[packet.getPacketID()];
        encoder.writePacket(buffer, packet);

        output.add(
            new FMLProxyPacket(
                buffer,
                context.channel()
                    .attr(NetworkRegistry.FML_CHANNEL)
                    .get()));
    }

    @Override
    protected void decode(ChannelHandlerContext context, FMLProxyPacket proxyPacket, List<Object> output) {
        MMPacketBuffer buffer = new MMPacketBuffer(proxyPacket.payload());

        MMPacketEncoder<MMPacket> encoder = this.encoders[buffer.readVarIntFromBuffer()];
        MMPacket packet = encoder.readPacket(buffer);
        encoder.setINetHandler(proxyPacket.handler(), packet);
        output.add(packet);
    }

    public void sendToPlayer(MMPacket packet, EntityPlayerMP player) {
        if (packet == null) {
            MagicMod.LOG.info("packet null");
            return;
        }
        if (player == null) {
            MagicMod.LOG.info("player null");
            return;
        }
        this.channel.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(FMLOutboundHandler.OutboundTarget.PLAYER);
        this.channel.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
            .set(player);
        this.channel.get(Side.SERVER)
            .writeAndFlush(packet);
    }

    public void sendToAllAround(MMPacket packet, NetworkRegistry.TargetPoint position) {
        this.channel.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
        this.channel.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
            .set(position);
        this.channel.get(Side.SERVER)
            .writeAndFlush(packet);
    }

    public void sendToAll(MMPacket packet) {
        this.channel.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(FMLOutboundHandler.OutboundTarget.ALL);
        this.channel.get(Side.SERVER)
            .writeAndFlush(packet);
    }

    public void sendToServer(MMPacket packet) {
        this.channel.get(Side.CLIENT)
            .attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        this.channel.get(Side.CLIENT)
            .writeAndFlush(packet);
    }

    public void sendPacketToPlayersWatching(World world, MMPacket packet, int x, int z) {
        if (!world.isRemote) {
            Chunk tChunk = world.getChunkFromBlockCoords(x, z);

            for (EntityPlayer player : world.playerEntities) {
                if (!(player instanceof EntityPlayerMP playerMP)) continue;

                if (playerMP.getServerForPlayer()
                    .getPlayerManager()
                    .isPlayerWatchingChunk(playerMP, tChunk.xPosition, tChunk.zPosition)) {
                    sendToPlayer(packet, playerMP);
                }
            }
        }
    }

    @Sharable
    private class HandlerShared extends SimpleChannelInboundHandler<MMPacket> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, MMPacket packet) {
            World world = FMLCommonHandler.instance()
                .getEffectiveSide()
                .isClient() ? getClientWorld() : null;

            encoders[packet.getPacketID()].process(world, packet);
        }

        @SideOnly(Side.CLIENT)
        private World getClientWorld() {
            return Minecraft.getMinecraft().theWorld;
        }
    }
}
