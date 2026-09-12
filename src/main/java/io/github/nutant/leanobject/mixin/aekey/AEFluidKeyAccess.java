package io.github.nutant.leanobject.mixin.aekey;

import appeng.api.stacks.AEFluidKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AEFluidKey.class)
public interface AEFluidKeyAccess {

    @Invoker(value = "<init>", remap = false)
    static AEFluidKey of(Fluid fluid, CompoundTag tag) {
        throw new RuntimeException("Not Implemented");
    }
}
