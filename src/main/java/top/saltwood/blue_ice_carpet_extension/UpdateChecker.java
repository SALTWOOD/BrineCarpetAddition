package top.saltwood.blue_ice_carpet_extension;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UpdateChecker {
    private static final String API_URL = "https://api.github.com/repos/SALTWOOD/BlueIceCarpetExtension/releases/latest";

    private final Logger logger;
    private final String currentVersion;
    private final HttpClient httpClient;

    private static boolean hasUpdate = false;
    private static String latestVersionTag = "";
    private static String downloadUrl = "";
    private static final List<UUID> notifiedPlayers = new ArrayList<>();

    public UpdateChecker(String currentVersion, Logger logger) {
        this.currentVersion = currentVersion;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.logger = logger;
    }

    public void check() {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL)).header("Accept", "application/vnd.github.v3+json").header("User-Agent", "BlueIceCarpetExtension-Updater") // GitHub API 要求必须包含 User-Agent
                .GET().build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
            if (response.statusCode() == 200) {
                try {
                    JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                    String rawLatestTag = json.get("tag_name").getAsString();
                    downloadUrl = json.get("html_url").getAsString();

                    String latestVersionStr = rawLatestTag.startsWith("v") ? rawLatestTag.substring(1) : rawLatestTag;

                    try {
                        Version latestVersion = Version.parse(latestVersionStr);
                        Version current = Version.parse(currentVersion);

                        if (latestVersion.compareTo(current) > 0) {
                            hasUpdate = true;
                            latestVersionTag = rawLatestTag;
                            logger.info("Mod \"BlueIceCarpetExtension\" has an update available.");
                            logger.info("Latest: {} (Current: {})", latestVersionTag, currentVersion);
                            logger.info("You can download the update here: {}", downloadUrl);
                        } else {
                            logger.info("Mod \"BlueIceCarpetExtension\" is up to date ({}).", currentVersion);
                        }
                    } catch (VersionParsingException e) {
                        logger.error("Failed to parse version strings: Latest[{}], Current[{}]", latestVersionStr, currentVersion);
                    }

                } catch (Exception e) {
                    logger.error("Unable to process update response: {}", e.getMessage());
                }
            }
        }).exceptionally(ex -> {
            logger.error("Network request failed while running update checker: {}", ex.getMessage());
            return null;
        });
    }

    public static void registerTip(ServerPlayerEntity player) {
        if (!ModSettings.biceUpdateCheck) return;
        if (!hasUpdate) return;
        if (!player.hasPermissionLevel(2)) return;
        if (notifiedPlayers.contains(player.getUuid())) return;

        player.sendMessage(Text.literal("[BlueIceCarpetExtension] ").formatted(Formatting.AQUA)
                .append(Text.literal("A new version ").formatted(Formatting.WHITE))
                .append(Text.literal(latestVersionTag).formatted(Formatting.GREEN))
                .append(Text.literal(" is available ").formatted(Formatting.WHITE))
                .append(Text.literal("here!").formatted(Formatting.UNDERLINE)
                        .styled(style -> style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, downloadUrl))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Open GitHub Releases")))
                        )
                )
        );

        player.sendMessage(Text.literal("This message is shown only once per operator per server startup.")
                .formatted(Formatting.GRAY).formatted(Formatting.ITALIC)
        );

        player.sendMessage(Text.literal("You can disable this check via: ").formatted(Formatting.GRAY)
                .append(Text.literal("/carpet setDefault biceUpdateCheck false").formatted(Formatting.GOLD))
                .styled(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/carpet setDefault biceUpdateCheck false"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to fill this command!")))
                )
        );

        player.sendMessage(Text.literal("Thank you for using my mod!").formatted(Formatting.LIGHT_PURPLE));

        notifiedPlayers.add(player.getUuid());
    }
}
