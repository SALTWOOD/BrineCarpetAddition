package top.saltwood.brine_carpet_addition;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import top.saltwood.brine_carpet_addition.network.BcaProtocol;

public class Main implements ModInitializer, CarpetExtension {
    public static final String MOD_ID = "brine_carpet_addition";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Nullable
    public static  MinecraftServer SERVER = null;

    @Override
    public void onInitialize() {
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(BcaSettings.class);
        CarpetServer.settingsManager.registerRuleObserver((serverCommandSource, currentRuleState, originalUserTest) ->
        {
            if (currentRuleState.categories().contains("BCA")) {
                switch (currentRuleState.name()) {
                    case "bcaProtocolEnabled":
                        // TODO
                    case "deathskull":
                        // TODO
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void onServerLoadedWorlds(MinecraftServer server) {
        BcaProtocol.init();
        Main.SERVER = server;
    }

    public static Identifier id(String id) {
        return Identifier.of(MOD_ID, id);
    }
}
