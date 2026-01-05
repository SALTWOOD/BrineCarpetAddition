package top.saltwood.blue_ice_carpet_extension.network.packets;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import top.saltwood.blue_ice_carpet_extension.network.PcaProtocol;

public record SyncEntityPayload(int id) implements CustomPayload {
    public static final Identifier PACKET_ID = PcaProtocol.id("sync_entity");
    public static final Id<SyncEntityPayload> ID = new Id<>(PACKET_ID);
    public static final PacketCodec<PacketByteBuf, SyncEntityPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER, SyncEntityPayload::id,
                    SyncEntityPayload::new
            );

    @Override
    public @NotNull Id<? extends CustomPayload> getId() {
        return ID;
    }
}