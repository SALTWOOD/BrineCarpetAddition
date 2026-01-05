package top.saltwood.blue_ice_carpet_extension.network.packets;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import top.saltwood.blue_ice_carpet_extension.network.PcaProtocol;

public record UpdateEntityPayload(
        Identifier identifier,
        int id,
        NbtCompound nbt
) implements CustomPayload {
    public static final Identifier PACKET_ID = PcaProtocol.id("update_entity");
    public static final Id<UpdateEntityPayload> ID = new Id<>(PACKET_ID);
    public static final PacketCodec<PacketByteBuf, UpdateEntityPayload> CODEC =
            PacketCodec.tuple(
                    Identifier.PACKET_CODEC, UpdateEntityPayload::identifier,
                    PacketCodecs.INTEGER, UpdateEntityPayload::id,
                    PacketCodecs.NBT_COMPOUND, UpdateEntityPayload::nbt,
                    UpdateEntityPayload::new
            );

    public static UpdateEntityPayload of(@NotNull ServerPlayerEntity player, @NotNull Entity entity) {
        Identifier identifier = player.getWorld().getRegistryKey().getValue();
        int id = entity.getId();
        NbtCompound nbt = entity.writeNbt(new NbtCompound());
        return new UpdateEntityPayload(identifier, id, nbt);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}