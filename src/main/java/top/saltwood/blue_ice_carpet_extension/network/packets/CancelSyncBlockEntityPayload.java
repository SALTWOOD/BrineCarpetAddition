// CancelSyncBlockEntityPayload.java
package top.saltwood.blue_ice_carpet_extension.network.packets;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import top.saltwood.blue_ice_carpet_extension.network.PcaProtocol;

public record CancelSyncBlockEntityPayload() implements CustomPayload {
    public static final Identifier PACKET_ID = PcaProtocol.id("cancel_sync_block_entity");
    public static final Id<CancelSyncBlockEntityPayload> ID = new Id<>(PACKET_ID);
    public static final PacketCodec<PacketByteBuf, CancelSyncBlockEntityPayload> CODEC = PacketCodec.unit(new CancelSyncBlockEntityPayload());

    @Override
    public @NotNull Id<? extends CustomPayload> getId() {
        return ID;
    }
}