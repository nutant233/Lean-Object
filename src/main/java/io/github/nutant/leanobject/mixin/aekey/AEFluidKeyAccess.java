package io.github.nutant.leanobject.mixin.aekey;

import appeng.api.stacks.AEFluidKey;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Factory accessor for AE2's private {@code AEFluidKey(FluidStack)} constructor, the counterpart of
 * {@link AEItemKeyAccess}: the public factories are overwritten to read the per-fluid caches, so the
 * caches build their entries through this constructor instead.
 */
@Mixin(AEFluidKey.class)
public interface AEFluidKeyAccess {

    @Invoker(value = "<init>", remap = false)
    static AEFluidKey of(FluidStack stack) {
        throw new RuntimeException("Not Implemented");
    }
}
