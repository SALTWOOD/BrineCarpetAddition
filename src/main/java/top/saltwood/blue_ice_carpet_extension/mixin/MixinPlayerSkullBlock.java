package top.saltwood.blue_ice_carpet_extension.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.PlayerSkullBlock;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import top.saltwood.blue_ice_carpet_extension.util.DeathInfo;
import top.saltwood.blue_ice_carpet_extension.util.DeathSkullInterface;

@Mixin(PlayerSkullBlock.class)
public abstract class MixinPlayerSkullBlock extends SkullBlock {
    protected MixinPlayerSkullBlock(Type skullType, Settings settings) {
        super(skullType, settings);
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.isOf(newState.getBlock())) {
            super.onStateReplaced(state, world, pos, newState, moved);
            return;
        }

        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            BlockEntity blockEntity = serverWorld.getBlockEntity(pos);
            if (!(blockEntity instanceof DeathSkullInterface skull)) return;

            DeathInfo deathInfo = skull.deathInfo$get();
            if (deathInfo == null) return;

            // 1. Items
            ItemScatterer.spawn(serverWorld, pos, deathInfo.inventory);

            // 2. Experience orbs
            int xp = deathInfo.exp;
            while (xp > 0) {
                int spawnedXp = ExperienceOrbEntity.roundToOrbSize(xp);
                xp -= spawnedXp;
                serverWorld.spawnEntity(new ExperienceOrbEntity(serverWorld, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, spawnedXp));
            }

            // 3. TextDisplay
            if (deathInfo.display != null) {
                Entity display = serverWorld.getEntity(deathInfo.display);
                if (display != null) {
                    display.discard();
                }
            }
        }

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public void afterBreak(@NotNull World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
        if (blockEntity instanceof DeathSkullInterface skull && skull.deathInfo$get() != null) {
            player.incrementStat(Stats.MINED.getOrCreateStat(this));
            player.addExhaustion(0.005F);
            return;
        }

        super.afterBreak(world, player, pos, state, blockEntity, stack);
    }
}
