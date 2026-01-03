package top.saltwood.brine_carpet_addition;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.utils.Translations;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import top.saltwood.brine_carpet_addition.network.PcaProtocol;
import top.saltwood.brine_carpet_addition.util.DeathHandle;

import java.util.Map;

public class Main implements ModInitializer, CarpetExtension {
    public static final String MOD_ID = "brine_carpet_addition";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Nullable
    public static MinecraftServer SERVER = null;

    public static Identifier id(String id) {
        return Identifier.of(MOD_ID, id);
    }

    @Override
    public void onInitialize() {
        CarpetServer.manageExtension(this);
        registerPlayerDeathEvent();
    }

    private void registerPlayerDeathEvent() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (!BcaSettings.deathskull) return true;
            if (!(entity instanceof ServerPlayerEntity player)) return true;
            DeathHandle.handle(player);
            return true;
        });
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(BcaSettings.class);

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

        EntitySleepEvents.ALLOW_SLEEP_TIME.register((player, sleepingPos, vanillaResult) -> BcaSettings.daydream ? ActionResult.SUCCESS : ActionResult.PASS);
    }

    @Override
    public void onServerLoadedWorlds(MinecraftServer server) {
        PcaProtocol.init();
        Main.SERVER = server;
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        return Translations.getTranslationFromResourcePath("assets/" + MOD_ID + "/lang/%s.json".formatted(lang));
    }
}
