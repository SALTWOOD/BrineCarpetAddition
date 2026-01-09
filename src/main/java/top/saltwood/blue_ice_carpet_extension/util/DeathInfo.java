package top.saltwood.blue_ice_carpet_extension.util;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record DeathInfo(long time, int exp, SimpleInventory inventory, @Nullable UUID display) {
    public static @NotNull DeathInfo fromCompound(
            @NotNull NbtCompound nbt,
            RegistryWrapper.WrapperLookup lookup
    ) {
        long time = nbt.getLong("DeathTime");
        int exp = nbt.getInt("Experience");
        SimpleInventory inventory = new SimpleInventory(41);
        inventory.readNbtList(nbt.getList("Inventory", NbtElement.COMPOUND_TYPE), lookup);
        UUID text = nbt.contains("DeathDisplay") ? nbt.getUuid("DeathDisplay") : null;
        return new DeathInfo(time, exp, inventory, text);
    }

    public @NotNull NbtCompound toCompound(RegistryWrapper.WrapperLookup lookup) {
        NbtCompound nbt = new NbtCompound();
        nbt.putLong("DeathTime", this.time);
        nbt.putInt("Experience", this.exp);
        nbt.put("Inventory", this.inventory.toNbtList(lookup));
        if (this.display != null) nbt.putUuid("DeathDisplay", this.display);
        return nbt;
    }
}
