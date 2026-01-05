package top.saltwood.blue_ice_carpet_extension.network.packets;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import top.saltwood.blue_ice_carpet_extension.network.PcaProtocol;

public record UpdateBlockEntityPayload(
        Identifier identifier,
        BlockPos pos,
        NbtCompound nbt
) implements CustomPayload {
    public static final Identifier PACKET_ID = PcaProtocol.id("update_block_entity");
    public static final Id<UpdateBlockEntityPayload> ID = new Id<>(PACKET_ID);
    public static final PacketCodec<PacketByteBuf, UpdateBlockEntityPayload> CODEC =
            PacketCodec.tuple(
                    Identifier.PACKET_CODEC, UpdateBlockEntityPayload::identifier,
                    BlockPos.PACKET_CODEC, UpdateBlockEntityPayload::pos,
                    PacketCodecs.NBT_COMPOUND, UpdateBlockEntityPayload::nbt,
                    UpdateBlockEntityPayload::new
            );

    public static UpdateBlockEntityPayload of(@NotNull ServerPlayerEntity player, @NotNull BlockEntity entity, RegistryWrapper.WrapperLookup lookup) {
        Identifier identifier = player.getWorld().getRegistryKey().getValue();
        BlockPos pos = entity.getPos();
        NbtCompound nbt = entity.createNbt(lookup);
        return new UpdateBlockEntityPayload(identifier, pos, nbt);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}