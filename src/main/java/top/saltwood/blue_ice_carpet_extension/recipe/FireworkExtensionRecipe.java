package top.saltwood.blue_ice_carpet_extension.recipe;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

import java.util.List;

public class FireworkExtensionRecipe extends SpecialCraftingRecipe {
    public static final RecipeSerializer<FireworkExtensionRecipe> SERIALIZER = new SpecialRecipeSerializer<>(FireworkExtensionRecipe::new);

    public FireworkExtensionRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        int rocketCount = 0;
        int gunpowderCount = 0;

        for (int i = 0; i < input.getSize(); ++i) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            if (stack.isOf(Items.FIREWORK_ROCKET)) rocketCount++;
            else if (stack.isOf(Items.GUNPOWDER)) gunpowderCount++;
            else return false;
        }
        return rocketCount == 1 && gunpowderCount >= 1;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput inventory, RegistryWrapper.WrapperLookup lookup) {
        ItemStack rocket = ItemStack.EMPTY;
        int gunpowderCount = 0;

        for (int i = 0; i < inventory.getSize(); ++i) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isOf(Items.FIREWORK_ROCKET)) {
                rocket = stack.copy();
            } else if (stack.isOf(Items.GUNPOWDER)) {
                gunpowderCount++;
            }
        }

        if (!rocket.isEmpty()) {
            FireworksComponent current = rocket.get(DataComponentTypes.FIREWORKS);
            int currentFlight = (current != null) ? current.flightDuration() : 1;

            int newFlight = Math.min(255, currentFlight + gunpowderCount);

            FireworksComponent newData = new FireworksComponent(
                    newFlight,
                    (current != null) ? current.explosions() : List.of()
            );

            // Apply to new
            rocket.set(DataComponentTypes.FIREWORKS, newData);
            rocket.setCount(1);
            return rocket;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
