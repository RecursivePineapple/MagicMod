package magicmod.network;

import magicmod.network.packets.PacketEncoderAuraRiftStatus;

public enum MMPacketEntry {

    AuraRiftStatus(new PacketEncoderAuraRiftStatus()),
    //
    ;

    public final int id = ordinal();
    public final MMPacketEncoder<?> encoder;

    MMPacketEntry(MMPacketEncoder<?> encoder) {
        this.encoder = encoder;
    }
}
