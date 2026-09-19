package io.github.nutant.leanobject.compat.ae2;

import appeng.api.stacks.AEFluidKey;
import com.gto.fastcollection.cache.WeakValueHashCache;
import net.minecraft.core.component.DataComponentPatch;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Implemented by the mixin on {@code Fluid} so a fluid can hand out its shared {@link AEFluidKey}.
 */
public interface IAEFluid {

    /** The key for this fluid with no components, cached on the fluid itself. */
    AEFluidKey lo$getAEKey();

    /** The cache for keys of this fluid that carry component data. */
    WeakValueHashCache<DataComponentPatch, AEFluidKey> lo$getComponentAEKeyCache();
}
