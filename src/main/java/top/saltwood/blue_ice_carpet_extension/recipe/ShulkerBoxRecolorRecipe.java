package top.saltwood.blue_ice_carpet_extension.recipe;

import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import top.saltwood.blue_ice_carpet_extension.ModSettings;

public class ShulkerBoxRecolorRecipe extends SpecialCraftingRecipe {
    public static final RecipeSerializer<ShulkerBoxRecolorRecipe> SERIALIZER = new SpecialRecipeSerializer<>(ShulkerBoxRecolorRecipe::new);

    public ShulkerBoxRecolorRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        if (!ModSettings.shulkerBoxRecoloring) return false;

        int shulkerCount = 0;
        int dyeCount = 0;

        for (int i = 0; i < input.getSize(); ++i) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            if (Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock) shulkerCount++;
            else if (stack.getItem() instanceof DyeItem) dyeCount++;
            else return false;
        }
        return shulkerCount == 1 && dyeCount == 1;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        ItemStack shulkerStack = ItemStack.EMPTY;
        DyeItem dyeItem = null;

        for (int i = 0; i < input.getSize(); ++i) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            if (Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock) {
                shulkerStack = stack;
            } else if (stack.getItem() instanceof DyeItem) {
                dyeItem = (DyeItem) stack.getItem();
            }
        }

        if (!shulkerStack.isEmpty() && dyeItem != null) {
            Block newShulkerBlock = ShulkerBoxBlock.get(dyeItem.getColor());
            ItemStack result = new ItemStack(newShulkerBlock);

            result.applyComponentsFrom(shulkerStack.getComponents());

            result.setCount(1);
            return result;
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
