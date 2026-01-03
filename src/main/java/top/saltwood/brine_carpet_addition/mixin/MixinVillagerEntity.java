package top.saltwood.brine_carpet_addition.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.saltwood.brine_carpet_addition.BcaSettings;

@Mixin(VillagerEntity.class)
public abstract class MixinVillagerEntity extends MerchantEntity {
    @Unique
    private boolean bca$isGoalAdded;

    @Unique
    private TemptGoal bca$emeraldGoal;

    @Unique
    private TemptGoal bca$emeraldBlockGoal;

    public MixinVillagerEntity(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;Lnet/minecraft/village/VillagerType;)V", at = @At(value = "RETURN"))
    private void init(EntityType<?> entityType, World world, VillagerType type, CallbackInfo ci) {
        if (this.getWorld().isClient()) return;

        // Goals with different speed
        // Emerald
        this.bca$emeraldGoal = new TemptGoal(this, 0.5D, Ingredient.ofItems(Items.EMERALD), false);
        // Emerald block
        this.bca$emeraldBlockGoal = new TemptGoal(this, 0.75D, Ingredient.ofItems(Items.EMERALD_BLOCK), false);
    }

    @Inject(method = "tick", at = @At(value = "RETURN"))
    private void checkVillagersAttractedByEmerald(CallbackInfo ci) {
        if (this.getWorld().isClient()) return;

        if (BcaSettings.emeraldAttractsVillager) {
            if (!bca$isGoalAdded) {
                // Priority sets to 0
                this.goalSelector.add(0, bca$emeraldGoal);
                this.goalSelector.add(0, bca$emeraldBlockGoal);
                bca$isGoalAdded = true;
            }
        } else {
            if (bca$isGoalAdded) {
                this.goalSelector.remove(bca$emeraldGoal);
                this.goalSelector.remove(bca$emeraldBlockGoal);
                bca$isGoalAdded = false;
            }
        }
    }
}
