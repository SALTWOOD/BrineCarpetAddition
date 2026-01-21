package top.saltwood.blue_ice_carpet_extension.util;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class DeathHandle {
    public static void handle(@NotNull ServerPlayerEntity player) {
        World world = player.getWorld();
        PlayerInventory items = player.getInventory();

        if (items.isEmpty()
                || world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            return;
        }

        for (Hand hand : Hand.values()) {
            if (player.getStackInHand(hand).isOf(Items.TOTEM_OF_UNDYING)) return;
        }

        SimpleInventory inventory = new SimpleInventory(41);

        // items without vanishing curse will be added to inventory.
        for (int i = 0; i < items.size(); ++i) {
            ItemStack itemStack = items.getStack(i);
            if (!EnchantmentHelper.hasAnyEnchantmentsWith(itemStack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) {
                inventory.setStack(i, itemStack);
            }
        }

        int experience = player.totalExperience / 2;

        BlockPos skullPos = DeathSkullChecker.findSkullPos(player);
        world.setBlockState(skullPos, Blocks.PLAYER_HEAD.getDefaultState(), 3);
        BlockEntity blockEntity = world.getBlockEntity(skullPos);
        if (!(blockEntity instanceof SkullBlockEntity skullEntity)) return;

        items.clear();
        player.experienceLevel = 0;
        player.playerScreenHandler.sendContentUpdates();

        ProfileComponent playerProfileComponent = new ProfileComponent(player.getGameProfile());
        skullEntity.setOwner(playerProfileComponent);

        LocalDateTime now = LocalDateTime.now();
        long timestamp = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String formattedTime = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));

        DisplayEntity.TextDisplayEntity display = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
        display.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
        display.setText(player.getName().copy()
                .append(Text.literal("\n" + formattedTime))
        );
        display.setViewRange(0.125f); // Visible to nearby players
        display.setPosition(new Vec3d(skullPos.getX() + 0.5, skullPos.getY() + 1.2, skullPos.getZ() + 0.5));

        UUID displayUuid = display.getUuid();

        DeathSkullInterface death = ((DeathSkullInterface) skullEntity);
        death.deathInfo$set(new DeathInfo(timestamp, experience, inventory, displayUuid));

        world.spawnEntity(display);

        skullEntity.markDirty();
    }

    public static void restore(@NotNull ServerPlayerEntity player, @NotNull DeathInfo info) {
        PlayerInventory playerInventory = player.getInventory();
        SimpleInventory deathInventory = info.inventory();

        for (int i = 0; i < deathInventory.size(); ++i) {
            ItemStack stack = deathInventory.getStack(i);
            if (stack.isEmpty()) continue;

            ItemStack copy = stack.copy();
            ItemStack currentOccupant = playerInventory.getStack(i);

            if (currentOccupant.isEmpty()) playerInventory.setStack(i, copy);
            else if (!playerInventory.insertStack(copy)) {
                // Find an available slot if target slot is used
                // Drop if full
                player.dropItem(copy, false);
            }
        }

        // Experiences
        player.addExperience(info.exp());

        player.sendMessage(Text.literal("Restored your inventory!").formatted(Formatting.GREEN), true);
    }
}
