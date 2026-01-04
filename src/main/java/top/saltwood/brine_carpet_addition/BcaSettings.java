package top.saltwood.brine_carpet_addition;

import carpet.api.settings.Rule;
import carpet.api.settings.RuleCategory;

public class BcaSettings {
    public static final String BCA = "BCA";
    public static final String PROTOCOL = "protocol";

    // protocol
    @Rule(categories = {BCA, PROTOCOL})
    public static boolean pcaProtocolEnabled = false;

    @Rule(categories = {BCA, PROTOCOL})
    public static PLAYER_SELECTOR syncPlayer = PLAYER_SELECTOR.OPS;

    // optimization
    @Rule(categories = {BCA, RuleCategory.FEATURE})
    public static boolean avoidAnvilTooExpensive = false;

    // feature
    @Rule(categories = {BCA, RuleCategory.FEATURE})
    public static boolean deathskull = false;

    @Rule(categories = {BCA, RuleCategory.FEATURE})
    public static boolean emeraldAttractsVillager = false;

    @Rule(categories = {BCA, RuleCategory.FEATURE})
    public static boolean daydream = false;

    @Rule(categories = {BCA, RuleCategory.FEATURE})
    public static PLAYER_SELECTOR viewPlayerInv = PLAYER_SELECTOR.NOBODY;

    public enum PLAYER_SELECTOR {
        NOBODY, BOT, OPS, OPS_AND_SELF, EVERYONE
    }
}