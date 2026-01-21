package top.saltwood.blue_ice_carpet_extension.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class DeathSkullChecker {
    private static final int RANGE = 4;

    public static boolean canPlaceSkull(@NotNull ServerPlayerEntity player, BlockPos pos) {
        World world = player.getWorld();
        BlockState state = world.getBlockState(pos);

        if (!world.isInBuildLimit(pos)) return false;
        if (state.isAir()) return true;
        return state.canReplace(new AutomaticItemPlacementContext(world, pos,
                Direction.DOWN, ItemStack.EMPTY, Direction.UP));
    }

    public static int clampYPos(@NotNull ServerPlayerEntity player, int y) {
        var world = player.getWorld();

        int min = world.getBottomY();
        int max = world.getTopY();
        // to prevent the death skull from spawning above bedrock.
        if (world.getRegistryKey() == World.NETHER) {
            max = y < 127 ? 127 : world.getTopY();
        }
        return MathHelper.clamp(y, min, max);
    }

    public static BlockPos getNearestFloor(@NotNull ServerPlayerEntity player, BlockPos pos, World world) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        int minY = world.getBottomY() + 1;
        // Search downwards for solid blocks, up to 10 blocks.
        // Once found, place skull on top of the block.
        for (int y = pos.getY() - 1; y > minY; y--) {
            mutable.setY(clampYPos(player, y));
            if (!world.getBlockState(mutable).isAir()) {
                mutable.setY(y + 1);
                return mutable;
            }
            if (pos.getY() - y >= 10) break;
        }
        return pos;
    }

    /// Look for a place to place the skull down.
    public static BlockPos findSkullPos(@NotNull ServerPlayerEntity player) {
        World world = player.getWorld();
        BlockPos.Mutable pos = player.getBlockPos().mutableCopy();

        // Directly
        if (canPlaceSkull(player, pos)) return pos;

        // Cube
        int centerX = pos.getX();
        int centerY = pos.getY();
        int centerZ = pos.getZ();
        int maxY = clampYPos(player, centerY + RANGE);
        int minY = clampYPos(player, centerY - RANGE);

        for (int x = centerX + RANGE; x >= centerX - RANGE; x--) {
            for (int y = maxY; y >= minY; y--) {
                for (int z = centerZ + RANGE; z >= centerZ - RANGE; z--) {
                    pos.set(x, y, z);
                    if (canPlaceSkull(player, pos)) return getNearestFloor(player, pos, world);
                }
            }
        }

        // Lowest
        pos.set(centerX, world.getBottomY(), centerZ);
        while (world.getBlockState(pos).isOf(Blocks.BEDROCK)) {
            pos.setY(pos.getY() + 1);
        }
        return pos;
    }
}
