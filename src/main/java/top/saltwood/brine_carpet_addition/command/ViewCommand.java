package top.saltwood.brine_carpet_addition.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import top.saltwood.brine_carpet_addition.Main;
import top.saltwood.brine_carpet_addition.util.ViewInventoryHandler;

public class ViewCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("view")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("inv")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(ctx -> openView(ctx.getSource(), EntityArgumentType.getPlayer(ctx, "player"), false))))
                .then(CommandManager.literal("echest")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(ctx -> openView(ctx.getSource(), EntityArgumentType.getPlayer(ctx, "player"), true))))
        );
    }

    private static int openView(ServerCommandSource source, ServerPlayerEntity target, boolean isEnderChest) {
        ServerPlayerEntity admin = source.getPlayer();
        if (admin == null) return 0;

        NamedScreenHandlerFactory factory = new NamedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                return isEnderChest ?
                        Text.literal(target.getName().getString() + "'s Ender Chest") :
                        Text.literal(target.getName().getString() + "'s Inventory");
            }

            @Nullable
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, net.minecraft.entity.player.PlayerEntity player) {
                if (isEnderChest) {
                    // 末影箱是 3x9 (27格)
                    return GenericContainerScreenHandler.createGeneric9x3(syncId, inv, target.getEnderChestInventory());
                } else {
                    try {
                        // 物品栏建议用 6x9 (54格) 展示，虽然物品栏只有 41 格，但能确保装下
                        return new ViewInventoryHandler(syncId, inv, target.getInventory());
                    } catch (Exception e) {
                        Main.LOGGER.error(e);
                    }
                    return null;
                }
            }
        };

        admin.openHandledScreen(factory);
        return 1;
    }
}
