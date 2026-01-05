package top.saltwood.blue_ice_carpet_extension.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlayerSkullBlock;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
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
    public void afterBreak(@NotNull World level, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
        if (level.isClient()) return;
        if (blockEntity instanceof DeathSkullInterface) {
            DeathInfo deathInfo = ((DeathSkullInterface) blockEntity).deathInfo$get();
            if (deathInfo == null) {
                super.afterBreak(level, player, pos, state, blockEntity, stack);
            } else {
                player.incrementStat(Stats.MINED.getOrCreateStat(this));
                player.addExhaustion(0.005F);
                for (ItemStack itemStack : deathInfo.inventory.clearToList()) {
                    Block.dropStack(level, pos, itemStack);
                }

                int xp = deathInfo.exp;
                while (xp > 0) {
                    int spawnedXp = ExperienceOrbEntity.roundToOrbSize(xp);
                    xp -= spawnedXp;
                    level.spawnEntity(new ExperienceOrbEntity(level, pos.getX(), pos.getY(), pos.getZ(), spawnedXp));
                }
            }
        }
    }
}
