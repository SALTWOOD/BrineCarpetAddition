package top.saltwood.blue_ice_carpet_extension.client.mixin;

import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import top.saltwood.blue_ice_carpet_extension.ModSettings;

@Mixin(AnvilScreen.class)
public class MixinAnvilScreen {
    @ModifyConstant(
            method = "drawForeground",
            constant = @Constant(intValue = 40)
    )
    private int mixinLimitInt(int i) {
        return ModSettings.avoidAnvilTooExpensive ? Integer.MAX_VALUE : i;
    }
}
