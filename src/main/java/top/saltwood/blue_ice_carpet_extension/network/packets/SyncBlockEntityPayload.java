package top.saltwood.blue_ice_carpet_extension.network.packets;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import top.saltwood.blue_ice_carpet_extension.network.PcaProtocol;

public record SyncBlockEntityPayload(BlockPos pos) implements CustomPayload {
    public static final Identifier PACKET_ID = PcaProtocol.id("sync_block_entity");
    public static final Id<SyncBlockEntityPayload> ID = new Id<>(PACKET_ID);
    public static final PacketCodec<PacketByteBuf, SyncBlockEntityPayload> CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC, SyncBlockEntityPayload::pos,
                    SyncBlockEntityPayload::new
            );

    @Override
    public @NotNull Id<? extends CustomPayload> getId() {
        return ID;
    }
}