package magicmod.network.packets;

import net.minecraft.world.World;

import com.github.bsideup.jabel.Desugar;
import magicmod.common.mechanics.AuraBuffer;
import magicmod.common.mechanics.rifts.EntityAuraRift;
import magicmod.network.MMPacket;
import magicmod.network.MMPacketBuffer;
import magicmod.network.MMPacketEncoder;
import magicmod.network.MMPacketEntry;
import magicmod.network.packets.PacketEncoderAuraRiftStatus.PacketAuraRiftStatus;

public class PacketEncoderAuraRiftStatus extends MMPacketEncoder<PacketAuraRiftStatus> {

    @Desugar
    public record PacketAuraRiftStatus(int entityId, AuraBuffer production) implements MMPacket {

        @Override
        public int getPacketID() {
            return MMPacketEntry.AuraRiftStatus.id;
        }
    }

    public static PacketAuraRiftStatus create(EntityAuraRift rift) {
        return new PacketAuraRiftStatus(rift.getEntityId(), rift.getProduction());
    }

    @Override
    public int getPacketID() {
        return MMPacketEntry.AuraRiftStatus.id;
    }

    @Override
    public void writePacket(MMPacketBuffer buffer, PacketAuraRiftStatus packet) {
        buffer.writeVarIntToBuffer(packet.entityId);
        buffer.writeAuraBuffer(packet.production);
    }

    @Override
    public PacketAuraRiftStatus readPacket(MMPacketBuffer buffer) {
        return new PacketAuraRiftStatus(buffer.readVarIntFromBuffer(), buffer.readAuraBuffer());
    }

    @Override
    public void process(World world, PacketAuraRiftStatus packet) {
        if (world.getEntityByID(packet.entityId) instanceof EntityAuraRift rift) {
            rift.setProduction(packet.production);
        }
    }
}
