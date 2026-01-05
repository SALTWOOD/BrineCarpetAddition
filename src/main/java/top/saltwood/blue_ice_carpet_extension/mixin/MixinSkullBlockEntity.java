package top.saltwood.blue_ice_carpet_extension.mixin;

import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.saltwood.blue_ice_carpet_extension.util.DeathInfo;
import top.saltwood.blue_ice_carpet_extension.util.DeathSkullInterface;

@Mixin(SkullBlockEntity.class)
public class MixinSkullBlockEntity implements DeathSkullInterface {
    @Unique
    private DeathInfo deathInfo;

    @Override
    public DeathInfo deathInfo$get() {
        return this.deathInfo;
    }

    @Override
    public void deathInfo$set(DeathInfo info) {
        this.deathInfo = info;
    }

    @Inject(method = "readNbt", at = @At(value = "RETURN"))
    private void postRead(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup, CallbackInfo ci) {
        if (nbt.contains("DeathInfo", NbtElement.COMPOUND_TYPE)) {
            this.deathInfo = DeathInfo.fromCompound(nbt.getCompound("DeathInfo"), lookup);
        }
    }

    @Inject(method = "writeNbt", at = @At(value = "RETURN"))
    private void postWrite(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup, CallbackInfo ci) {
        if (this.deathInfo != null) {
            nbt.put("DeathInfo", this.deathInfo.toCompound(lookup));
        }
    }
}
