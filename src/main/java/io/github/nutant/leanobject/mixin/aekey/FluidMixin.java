package io.github.nutant.leanobject.mixin.aekey;

import appeng.api.stacks.AEFluidKey;
import com.gto.fastcollection.cache.WeakValueHashCache;
import io.github.nutant.leanobject.compat.ae2.IAEFluid;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Caches the AE2 storage keys belonging to this fluid, the counterpart of {@link ItemMixin}.
 *
 * <p>The component-free key is held directly, and keys that carry component data are looked up in a
 * weak cache keyed by their {@link DataComponentPatch}. Both are built through
 * {@link AEFluidKeyAccess}, which invokes AE2's private constructor because the public factories are
 * overwritten to read these caches.
 */
@Mixin(Fluid.class)
public abstract class FluidMixin implements IAEFluid {

    @Unique
    private volatile AEFluidKey leanObject$aeKey;

    @Unique
    private volatile WeakValueHashCache<DataComponentPatch, AEFluidKey> leanObject$componentKeys;

    @Override
    public AEFluidKey lo$getAEKey() {
        var cached = leanObject$aeKey;
        if (cached != null) return cached;
        // Built outside the monitor: AE2's constructor may run another mod's dynamic hooks, and those
        // must never execute while this lock is held. The loser of a race drops its own instance, since
        // these keys compare by identity.
        var key = AEFluidKeyAccess.of(new FluidStack((Fluid) (Object) this, 1));
        synchronized (this) {
            if (leanObject$aeKey == null) {
                leanObject$aeKey = key;
                return key;
            }
            return leanObject$aeKey;
        }
    }

    @Override
    public WeakValueHashCache<DataComponentPatch, AEFluidKey> lo$getComponentAEKeyCache() {
        var cache = leanObject$componentKeys;
        if (cache != null) return cache;
        var created = new WeakValueHashCache<DataComponentPatch, AEFluidKey>(
                p -> AEFluidKeyAccess.of(new FluidStack(((Fluid) (Object) this).builtInRegistryHolder(), 1, p)));
        synchronized (this) {
            if (leanObject$componentKeys == null) {
                leanObject$componentKeys = created;
                return created;
            }
            return leanObject$componentKeys;
        }
    }
}
