package top.saltwood.blue_ice_carpet_extension.util;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Objects;

public class DeathHandle {
    public static void handle(@NotNull ServerPlayerEntity player) {
        World world = player.getWorld();
        PlayerInventory items = player.getInventory();
        if (items.isEmpty()) return;

        if (world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            return;
        }

        for (Hand hand : Hand.values()) {
            if (player.getStackInHand(hand).isOf(Items.TOTEM_OF_UNDYING)) return;
        }

        SimpleInventory inventory = new SimpleInventory(41);

        // items without vanishing curse will be added to inventory.
        for (int i = 0; i < items.size(); ++i) {
            ItemStack itemStack = items.getStack(i);
            if (!itemStack.isEmpty() && !EnchantmentHelper.hasAnyEnchantmentsWith(itemStack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) {
                inventory.addStack(itemStack);
            }
        }
        int experience = player.totalExperience / 2;

        items.clear();
        player.experienceLevel = 0;
        player.playerScreenHandler.sendContentUpdates();

        // place the skull down
        BlockPos skullPos = DeathSkullChecker.findSkullPos(player);
        world.setBlockState(skullPos, Blocks.PLAYER_HEAD.getDefaultState(), 3);
        SkullBlockEntity skullEntity = (SkullBlockEntity) world.getBlockEntity(skullPos);
        if (skullEntity != null) {
            ProfileComponent playerProfileComponent = new ProfileComponent(player.getGameProfile());
            skullEntity.setOwner(playerProfileComponent);
        }
        DeathSkullInterface death = ((DeathSkullInterface) Objects.requireNonNull(skullEntity));
        death.deathInfo$set(new DeathInfo(new Date().getTime(), experience, inventory));
        skullEntity.markDirty();
    }
}
