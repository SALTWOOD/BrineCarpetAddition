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
    public static BCA_SYNC_PLAYER_ENTITY_OPTIONS syncPlayer = BCA_SYNC_PLAYER_ENTITY_OPTIONS.OPS;

    // optimization
    @Rule(categories = {BCA, RuleCategory.FEATURE})
    public static boolean avoidAnvilTooExpensive = false;

    // feature
    @Rule(categories = {BCA, RuleCategory.FEATURE})
    public static boolean deathskull = false;

    @Rule(categories = {BCA, RuleCategory.FEATURE})
    public static boolean emeraldAttractsVillager = false;

    public enum BCA_SYNC_PLAYER_ENTITY_OPTIONS {
        NOBODY, BOT, OPS, OPS_AND_SELF, EVERYONE
    }
}