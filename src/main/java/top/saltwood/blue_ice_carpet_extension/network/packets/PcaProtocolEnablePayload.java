package top.saltwood.blue_ice_carpet_extension.network.packets;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import top.saltwood.blue_ice_carpet_extension.network.PcaProtocol;

public record PcaProtocolEnablePayload() implements CustomPayload {
    public static final Identifier PACKET_ID = PcaProtocol.id("enable_pca_sync_protocol");
    public static final Id<PcaProtocolEnablePayload> ID = new Id<>(PACKET_ID);
    public static final PacketCodec<PacketByteBuf, PcaProtocolEnablePayload> CODEC =
            PacketCodec.unit(new PcaProtocolEnablePayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}