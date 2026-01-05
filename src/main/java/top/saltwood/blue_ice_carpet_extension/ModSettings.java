package top.saltwood.blue_ice_carpet_extension;

import carpet.api.settings.Rule;
import carpet.api.settings.RuleCategory;

public class ModSettings {
    public static final String MOD = "B-ICE";
    public static final String PROTOCOL = "protocol";

    // protocol
    @Rule(categories = {MOD, PROTOCOL})
    public static boolean pcaProtocolEnabled = false;

    @Rule(categories = {MOD, PROTOCOL})
    public static PLAYER_SELECTOR syncPlayer = PLAYER_SELECTOR.OPS;

    // optimization
    @Rule(categories = {MOD, RuleCategory.FEATURE})
    public static boolean avoidAnvilTooExpensive = false;

    // feature
    @Rule(categories = {MOD, RuleCategory.FEATURE})
    public static boolean deathskull = false;

    @Rule(categories = {MOD, RuleCategory.FEATURE})
    public static boolean emeraldAttractsVillager = false;

    @Rule(categories = {MOD, RuleCategory.FEATURE})
    public static boolean daydream = false;

    @Rule(categories = {MOD, RuleCategory.FEATURE})
    public static PLAYER_SELECTOR viewPlayerInv = PLAYER_SELECTOR.NOBODY;

    public enum PLAYER_SELECTOR {
        NOBODY, BOT, OPS, OPS_AND_SELF, EVERYONE
    }
}