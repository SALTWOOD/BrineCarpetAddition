package top.saltwood.blue_ice_carpet_extension;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.utils.Translations;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import top.saltwood.blue_ice_carpet_extension.command.ViewCommand;
import top.saltwood.blue_ice_carpet_extension.network.PcaProtocol;
import top.saltwood.blue_ice_carpet_extension.recipe.FireworkExtensionRecipe;
import top.saltwood.blue_ice_carpet_extension.recipe.ShulkerBoxRecolorRecipe;
import top.saltwood.blue_ice_carpet_extension.util.DeathHandle;
import top.saltwood.blue_ice_carpet_extension.util.ViewInventoryHandler;

import java.util.Map;

public class Main implements ModInitializer, CarpetExtension {
    public static final String MOD_ID = "blue_ice_carpet_extension";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Nullable
    public static MinecraftServer SERVER = null;

    public static Identifier id(String id) {
        return Identifier.of(MOD_ID, id);
    }

    @Override
    public void onInitialize() {
        CarpetServer.manageExtension(this);

        Registry.register(Registries.RECIPE_SERIALIZER, Main.id("firework_extension"), FireworkExtensionRecipe.SERIALIZER);
        Registry.register(Registries.RECIPE_SERIALIZER, Main.id("shulker_box_recolor"), ShulkerBoxRecolorRecipe.SERIALIZER);
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandBuildContext) {
        ViewCommand.register(dispatcher);
    }

    private void registerPlayerDeathEvent() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (!ModSettings.deathskull) return true;
            if (!(entity instanceof ServerPlayerEntity player)) return true;
            DeathHandle.handle(player);
            return true;
        });
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(ModSettings.class);

        CarpetServer.settingsManager.registerRuleObserver((serverCommandSource, currentRuleState, originalUserTest) -> {
            switch (currentRuleState.name()) {
                case "pcaProtocolEnabled":
                    if (currentRuleState.value() instanceof Boolean enabled) {
                        if (enabled) {
                            PcaProtocol.enableBcaProtocolGlobal();
                        } else {
                            PcaProtocol.disableBcaProtocolGlobal();
                        }
                    }
                    break;
            }
        });

        EntitySleepEvents.ALLOW_SLEEP_TIME.register((player, sleepingPos, vanillaResult) -> ModSettings.daydream ? ActionResult.SUCCESS : ActionResult.PASS);

        PcaProtocol.init();
        registerPlayerDeathEvent();

        UseEntityCallback.EVENT.register(ViewInventoryHandler::useOnPlayer);
    }

    @Override
    public void onServerLoadedWorlds(MinecraftServer server) {
        Main.SERVER = server;
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        return Translations.getTranslationFromResourcePath("assets/" + MOD_ID + "/lang/%s.json".formatted(lang));
    }
}
