package top.saltwood.blue_ice_carpet_extension.util;

import carpet.patches.EntityPlayerMPFake;
import com.mojang.datafixers.util.Pair;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import top.saltwood.blue_ice_carpet_extension.ModSettings;

import java.util.ArrayList;
import java.util.List;

public class ViewInventoryHandler extends ScreenHandler {
    private final Inventory target;

    public ViewInventoryHandler(int syncId, PlayerInventory playerInv, Inventory targetInv) {
        super(ScreenHandlerType.GENERIC_9X6, syncId);
        this.target = targetInv;

        // 1. Inventory (0-35)
        for (int i = 0; i < 36; i++) addSlot(new MirrorSlot(target, i, 8 + (i % 9) * 18, 18 + (i / 9) * 18));

        // 2. Split line (36-44)
        Inventory line = new SimpleInventory(9);
        ItemStack sep = Items.STRUCTURE_VOID.getDefaultStack();
        sep.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§7---"));
        for (int i = 0; i < 9; i++) {
            line.setStack(i, sep);
            addSlot(new LockedSlot(line, i, 8 + i * 18, 90));
        }

        // 3. Armors (45-48) + PlaceHolder (49-52) + Offhand (53)
        for (int i = 0; i < 4; i++) addSlot(new MirrorSlot(target, 36 + i, 8 + i * 18, 108));
        Inventory dummy = new SimpleInventory(4);
        for (int i = 0; i < 4; i++) addSlot(new LockedSlot(dummy, i, 8 + (4 + i) * 18, 108));
        addSlot(new MirrorSlot(target, 40, 8 + 8 * 18, 108));

        // 4. Viewer's inventory (54-89)
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 9; j++) addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 140 + i * 18));
        for (int i = 0; i < 9; i++) addSlot(new Slot(playerInv, i, 8 + i * 18, 198));
    }

    public static ActionResult useOnPlayer(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult ignoredResult) {
        if (!(entity instanceof ServerPlayerEntity targetPlayer)) return ActionResult.PASS;
        boolean allow = switch (ModSettings.viewPlayerInv) {
            case NOBODY -> false;
            case BOT -> targetPlayer instanceof EntityPlayerMPFake;
            case OPS -> player.hasPermissionLevel(2);
            case OPS_AND_SELF -> player.hasPermissionLevel(2) || entity == player;
            case EVERYONE -> true;
        };
        if (!allow) return ActionResult.PASS;
        if (!world.isClient && hand == Hand.MAIN_HAND) {
            if (player.hasPermissionLevel(2)) {
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

                NamedScreenHandlerFactory factory = new SimpleNamedScreenHandlerFactory(
                        (syncId, inv, p) -> new ViewInventoryHandler(syncId, inv, targetPlayer.getInventory()),
                        Text.literal(targetPlayer.getName().getString() + "'s Inventory")
                );

                serverPlayer.openHandledScreen(factory);

                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public void sendContentUpdates() {
        super.sendContentUpdates();
        for (int i = 0; i < 54; i++) {
            if (slots.get(i) instanceof MirrorSlot s) {
                ItemStack real = target.getStack(s.getIndex());
                if (!ItemStack.areEqual(s.getStack(), real)) s.setStackNoCallbacks(real.copy());
            }
        }
        this.updateToClient();
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot slot = slots.get(index);
        if (slot.hasStack()) {
            ItemStack stack = slot.getStack(), copy = stack.copy();
            if (index < 54) {
                if (!insertItem(stack, 54, 90, true)) return ItemStack.EMPTY;
            } else {
                if (!insertItem(stack, 0, 36, false)) return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.setStack(ItemStack.EMPTY);
            else slot.markDirty();
            return copy;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity p) {
        return true;
    }

    private static class MirrorSlot extends Slot {
        public MirrorSlot(Inventory inv, int idx, int x, int y) {
            super(inv, idx, x, y);
        }

        @Override
        public void markDirty() {
            super.markDirty();
            sync();
        }

        @Override
        public void setStack(ItemStack stack) {
            this.inventory.setStack(getIndex(), stack);
            super.setStack(stack);
            sync();
        }

        private void sync() {
            if (inventory instanceof PlayerInventory pi && pi.player instanceof ServerPlayerEntity sp) {
                sp.currentScreenHandler.sendContentUpdates();
                List<Pair<EquipmentSlot, ItemStack>> list = new ArrayList<>();
                for (EquipmentSlot s : EquipmentSlot.values()) list.add(Pair.of(s, sp.getEquippedStack(s)));
                EntityEquipmentUpdateS2CPacket p = new EntityEquipmentUpdateS2CPacket(sp.getId(), list);
                sp.getServerWorld().getChunkManager().sendToNearbyPlayers(sp, p);
                sp.networkHandler.sendPacket(p);
            }
        }
    }

    private static class LockedSlot extends Slot {
        public LockedSlot(Inventory i, int idx, int x, int y) {
            super(i, idx, x, y);
        }

        @Override
        public boolean canTakeItems(PlayerEntity p) {
            return false;
        }

        @Override
        public boolean canInsert(ItemStack s) {
            return false;
        }
    }
}
