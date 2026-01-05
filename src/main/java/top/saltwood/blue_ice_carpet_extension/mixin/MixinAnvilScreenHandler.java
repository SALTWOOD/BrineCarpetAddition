package top.saltwood.blue_ice_carpet_extension.mixin;

import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import top.saltwood.blue_ice_carpet_extension.ModSettings;

@Mixin(AnvilScreenHandler.class)
public class MixinAnvilScreenHandler {
    @ModifyConstant(
            method = "updateResult",
            constant = @Constant(intValue = 40, ordinal = 0),
            require = 1
    )
    private int mixinMultipleEnchantment(int i) {
        return ModSettings.avoidAnvilTooExpensive ? Integer.MAX_VALUE : i;
    }

    @ModifyConstant(
            method = "updateResult",
            constant = @Constant(intValue = 40, ordinal = 2),
            require = 1
    )
    private int mixinLimitInt(int i) {
        return ModSettings.avoidAnvilTooExpensive ? Integer.MAX_VALUE : i;
    }
}