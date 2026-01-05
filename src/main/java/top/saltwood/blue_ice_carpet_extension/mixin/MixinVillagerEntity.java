package top.saltwood.blue_ice_carpet_extension.mixin;

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
import top.saltwood.blue_ice_carpet_extension.ModSettings;

@Mixin(VillagerEntity.class)
public abstract class MixinVillagerEntity extends MerchantEntity {
    @Unique
    private boolean bice$isGoalAdded;

    @Unique
    private TemptGoal bice$emeraldGoal;

    @Unique
    private TemptGoal bice$emeraldBlockGoal;

    public MixinVillagerEntity(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;Lnet/minecraft/village/VillagerType;)V", at = @At(value = "RETURN"))
    private void init(EntityType<?> entityType, World world, VillagerType type, CallbackInfo ci) {
        if (this.getWorld().isClient()) return;

        // Goals with different speed
        // Emerald
        this.bice$emeraldGoal = new TemptGoal(this, 0.5D, Ingredient.ofItems(Items.EMERALD), false);
        // Emerald block
        this.bice$emeraldBlockGoal = new TemptGoal(this, 0.75D, Ingredient.ofItems(Items.EMERALD_BLOCK), false);
    }

    @Inject(method = "tick", at = @At(value = "RETURN"))
    private void checkVillagersAttractedByEmerald(CallbackInfo ci) {
        if (this.getWorld().isClient()) return;

        if (ModSettings.emeraldAttractsVillager) {
            if (!bice$isGoalAdded) {
                // Priority sets to 0
                this.goalSelector.add(0, bice$emeraldGoal);
                this.goalSelector.add(0, bice$emeraldBlockGoal);
                bice$isGoalAdded = true;
            }
        } else {
            if (bice$isGoalAdded) {
                this.goalSelector.remove(bice$emeraldGoal);
                this.goalSelector.remove(bice$emeraldBlockGoal);
                bice$isGoalAdded = false;
            }
        }
    }
}
