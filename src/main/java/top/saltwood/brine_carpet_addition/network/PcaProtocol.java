package top.saltwood.brine_carpet_addition.network;

import carpet.patches.EntityPlayerMPFake;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.saltwood.brine_carpet_addition.BcaSettings;
import top.saltwood.brine_carpet_addition.Main;
import top.saltwood.brine_carpet_addition.network.packets.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

// PCA mod compatible
public class PcaProtocol {

    public static final ReentrantLock lock = new ReentrantLock(true);
    public static final ReentrantLock pairLock = new ReentrantLock(true);

    private static final Map<ServerPlayerEntity, Pair<Identifier, BlockPos>> playerWatchBlockPos = new HashMap<>();
    private static final Map<ServerPlayerEntity, Pair<Identifier, Entity>> playerWatchEntity = new HashMap<>();
    private static final Map<Pair<Identifier, BlockPos>, Set<ServerPlayerEntity>> blockPosWatchPlayerSet = new HashMap<>();
    private static final Map<Pair<Identifier, Entity>, Set<ServerPlayerEntity>> entityWatchPlayerSet = new HashMap<>();
    private static final MutablePair<Identifier, Entity> identifierEntityPair = new MutablePair<>();
    private static final MutablePair<Identifier, BlockPos> identifierBlockPosPair = new MutablePair<>();

    public static Identifier id(String id) {
        return Identifier.of("pca", id);
    }

    public static void enable(@NotNull ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new PcaProtocolEnablePayload());
        lock.lock();
        lock.unlock();
    }

    public static void disable(@NotNull ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new PcaProtocolDisablePayload());
    }

    public static void updateEntity(@NotNull ServerPlayerEntity player, @NotNull Entity entity) {
        ServerPlayNetworking.send(player, UpdateEntityPayload.of(player, entity));
    }

    public static void updateBlockEntity(@NotNull ServerPlayerEntity player, @NotNull BlockEntity entity) {
        if (!entity.hasWorld()) return;
        if (Main.SERVER == null) return;
        ServerPlayNetworking.send(player, UpdateBlockEntityPayload.of(player, entity, Main.SERVER.getRegistryManager()));
    }

    public static void init() {
        PayloadTypeRegistry.playS2C().register(UpdateEntityPayload.ID, UpdateEntityPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateBlockEntityPayload.ID, UpdateBlockEntityPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PcaProtocolDisablePayload.ID, PcaProtocolDisablePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PcaProtocolEnablePayload.ID, PcaProtocolEnablePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SyncEntityPayload.ID, SyncEntityPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SyncBlockEntityPayload.ID, SyncBlockEntityPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CancelSyncEntityPayload.ID, CancelSyncEntityPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CancelSyncBlockEntityPayload.ID, CancelSyncBlockEntityPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SyncEntityPayload.ID, PcaProtocol::syncEntity);
        ServerPlayNetworking.registerGlobalReceiver(SyncBlockEntityPayload.ID, PcaProtocol::syncBlockEntity);
        ServerPlayNetworking.registerGlobalReceiver(CancelSyncEntityPayload.ID,
                (payload, context) -> cancelSyncEntity(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(CancelSyncBlockEntityPayload.ID,
                (payload, context) -> cancelSyncEntity(context.player()));

        ServerPlayConnectionEvents.JOIN.register(PcaProtocol::onJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(PcaProtocol::onDisconnect);
    }

    private static void onJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        if (BcaSettings.pcaProtocolEnabled) enable(handler.player);
    }

    private static void onDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
        if (BcaSettings.pcaProtocolEnabled) cancelSyncing(handler.player);
    }

    private static void syncEntity(SyncEntityPayload payload, ServerPlayNetworking.Context context) {
        if (Main.SERVER == null || !BcaSettings.pcaProtocolEnabled) return;
        int entityId = payload.id();
        ServerPlayerEntity player = context.player();
        ServerWorld world = player.getServerWorld();
        Entity entity = world.getEntityById(entityId);
        if (entity == null) {
            Main.LOGGER.debug("Entity {} not found.", entityId);
            return;
        }
        cancelSyncing(player);
        if (entity instanceof PlayerEntity) {
            switch (BcaSettings.syncPlayer) {
                case NOBODY -> {
                    return;
                }
                case BOT -> {
                    if (!(entity instanceof EntityPlayerMPFake)) return;
                }
                case OPS -> {
                    if (!(entity instanceof EntityPlayerMPFake) && player.hasPermissionLevel(2))
                        return;
                }
                case OPS_AND_SELF -> {
                    if (!(entity instanceof EntityPlayerMPFake) && player.hasPermissionLevel(2) && entity != player)
                        return;
                }
                case EVERYONE -> {
                }
                default -> {
                    Main.LOGGER.error("BcaSettings.syncPlayer is out of range.");
                    return;
                }
            }
        }
        updateEntity(player, entity);
        Pair<Identifier, Entity> pair = new ImmutablePair<>(entity.getWorld().getRegistryKey().getValue(), entity);
        lock.lock();
        playerWatchEntity.put(player, pair);
        entityWatchPlayerSet.computeIfAbsent(pair, k -> new HashSet<>()).add(player);
        lock.unlock();
    }

    private static void syncBlockEntity(SyncBlockEntityPayload payload, ServerPlayNetworking.Context context) {
        if (Main.SERVER == null || !BcaSettings.pcaProtocolEnabled) return;
        ServerPlayerEntity player = context.player();
        ServerWorld world = player.getServerWorld();
        BlockPos pos = payload.pos();
        BlockState state = world.getBlockState(pos);
        cancelSyncing(player);

        BlockEntity adjBlockEntity = null;
        if (state.getBlock() instanceof ChestBlock && state.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
            BlockPos adjPos = pos.offset(ChestBlock.getFacing(state));
            adjBlockEntity = world.getWorldChunk(adjPos).getBlockEntity(adjPos);
        } else if (state.isOf(Blocks.BARREL)) {
            Direction opposite = state.get(BarrelBlock.FACING).getOpposite();
            BlockPos adjPos = pos.offset(opposite);
            BlockState adjState = world.getBlockState(adjPos);
            if (adjState.isOf(Blocks.BARREL) && adjState.get(BarrelBlock.FACING) == opposite) {
                adjBlockEntity = world.getWorldChunk(adjPos).getBlockEntity(adjPos);
            }
        }

        if (adjBlockEntity != null) updateBlockEntity(player, adjBlockEntity);
        BlockEntity blockEntity = world.getWorldChunk(pos).getBlockEntity(pos);
        if (blockEntity != null) {
            updateBlockEntity(player, blockEntity);
            Pair<Identifier, BlockPos> pair = new ImmutablePair<>(world.getRegistryKey().getValue(), pos);
            lock.lock();
            playerWatchBlockPos.put(player, pair);
            blockPosWatchPlayerSet.computeIfAbsent(pair, k -> new HashSet<>()).add(player);
            lock.unlock();
        }
    }

    private static MutablePair<Identifier, Entity> getIdentifierEntityPair(Identifier id, Entity entity) {
        pairLock.lock();
        identifierEntityPair.setLeft(id);
        identifierEntityPair.setRight(entity);
        pairLock.unlock();
        return identifierEntityPair;
    }

    private static MutablePair<Identifier, BlockPos> getIdentifierBlockPosPair(Identifier id, BlockPos pos) {
        pairLock.lock();
        identifierBlockPosPair.setLeft(id);
        identifierBlockPosPair.setRight(pos);
        pairLock.unlock();
        return identifierBlockPosPair;
    }

    @Nullable
    private static Set<ServerPlayerEntity> getWatchPlayerList(@NotNull Entity entity) {
        return entityWatchPlayerSet.get(getIdentifierEntityPair(entity.getWorld().getRegistryKey().getValue(), entity));
    }

    @Nullable
    private static Set<ServerPlayerEntity> getWatchPlayerList(@NotNull World world, @NotNull BlockPos pos) {
        return blockPosWatchPlayerSet.get(getIdentifierBlockPosPair(world.getRegistryKey().getValue(), pos));
    }

    public static boolean syncEntityToClient(@NotNull Entity entity) {
        if (entity.getWorld().isClient()) return false;
        lock.lock();
        Set<ServerPlayerEntity> players = getWatchPlayerList(entity);
        boolean ret = false;
        if (players != null) {
            for (ServerPlayerEntity player : players) {
                updateEntity(player, entity);
                ret = true;
            }
        }
        lock.unlock();
        return ret;
    }

    public static boolean syncBlockEntityToClient(@NotNull BlockEntity blockEntity) {
        if (blockEntity.getWorld() == null || blockEntity.getWorld().isClient()) return false;
        boolean ret = false;
        World world = blockEntity.getWorld();
        BlockPos pos = blockEntity.getPos();
        BlockState state = world.getBlockState(pos);
        lock.lock();
        Set<ServerPlayerEntity> players = getWatchPlayerList(world, pos);
        Set<ServerPlayerEntity> adjPlayers = null;

        if (state.getBlock() instanceof ChestBlock && state.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
            BlockPos adjPos = pos.offset(ChestBlock.getFacing(state));
            adjPlayers = getWatchPlayerList(world, adjPos);
        } else if (state.isOf(Blocks.BARREL)) {
            Direction opposite = state.get(BarrelBlock.FACING).getOpposite();
            BlockPos adjPos = pos.offset(opposite);
            BlockState adjState = world.getBlockState(adjPos);
            if (adjState.isOf(Blocks.BARREL) && adjState.get(BarrelBlock.FACING) == opposite) {
                adjPlayers = getWatchPlayerList(world, adjPos);
            }
        }

        if (adjPlayers != null) {
            if (players == null) players = adjPlayers;
            else players.addAll(adjPlayers);
        }

        if (players != null) {
            for (ServerPlayerEntity player : players) {
                updateBlockEntity(player, blockEntity);
                ret = true;
            }
        }
        lock.unlock();
        return ret;
    }

    private static void cancelSyncEntity(ServerPlayerEntity player) {
        lock.lock();
        Pair<Identifier, Entity> pair = playerWatchEntity.get(player);
        if (pair != null) {
            entityWatchPlayerSet.getOrDefault(pair, new HashSet<>()).remove(player);
            if (entityWatchPlayerSet.get(pair) != null && entityWatchPlayerSet.get(pair).isEmpty()) {
                entityWatchPlayerSet.remove(pair);
            }
            playerWatchEntity.remove(player);
        }
        lock.unlock();
    }

    private static void cancelSyncBlockEntity(ServerPlayerEntity player) {
        lock.lock();
        Pair<Identifier, BlockPos> pair = playerWatchBlockPos.get(player);
        if (pair != null) {
            blockPosWatchPlayerSet.getOrDefault(pair, new HashSet<>()).remove(player);
            if (blockPosWatchPlayerSet.get(pair) != null && blockPosWatchPlayerSet.get(pair).isEmpty()) {
                blockPosWatchPlayerSet.remove(pair);
            }
            playerWatchBlockPos.remove(player);
        }
        lock.unlock();
    }

    public static void cancelSyncing(ServerPlayerEntity player) {
        cancelSyncEntity(player);
        cancelSyncBlockEntity(player);
    }

    public static void disableBcaProtocolGlobal() {
        if (Main.SERVER == null) return;
        lock.lock();
        playerWatchBlockPos.clear();
        playerWatchEntity.clear();
        blockPosWatchPlayerSet.clear();
        entityWatchPlayerSet.clear();
        lock.unlock();
        for (ServerPlayerEntity player : Main.SERVER.getPlayerManager().getPlayerList()) {
            disable(player);
        }
    }

    public static void enableBcaProtocolGlobal() {
        if (Main.SERVER == null) return;
        for (ServerPlayerEntity player : Main.SERVER.getPlayerManager().getPlayerList()) {
            enable(player);
        }
    }
}