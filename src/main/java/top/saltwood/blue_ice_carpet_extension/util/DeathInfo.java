package top.saltwood.blue_ice_carpet_extension.util;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.NotNull;

public class DeathInfo {
    public final long time;
    public final int exp;
    public final SimpleInventory inventory;

    public DeathInfo(long time, int exp, SimpleInventory inventory) {
        this.time = time;
        this.exp = exp;
        this.inventory = inventory;
    }

    public static @NotNull DeathInfo fromCompound(
            @NotNull NbtCompound nbt,
            RegistryWrapper.WrapperLookup lookup
    ) {
        long time = nbt.getLong("DeathTime");
        int exp = nbt.getInt("Experience");
        SimpleInventory inventory = new SimpleInventory(41);
        inventory.readNbtList(nbt.getList("Inventory", NbtElement.COMPOUND_TYPE), lookup);
        return new DeathInfo(time, exp, inventory);
    }

    public @NotNull NbtCompound toCompound(RegistryWrapper.WrapperLookup lookup) {
        NbtCompound nbt = new NbtCompound();
        nbt.putLong("DeathTime", this.time);
        nbt.putInt("Experience", this.exp);
        nbt.put("Inventory", this.inventory.toNbtList(lookup));
        return nbt;
    }
}
