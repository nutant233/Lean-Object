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
 */
@Mixin(Fluid.class)
public abstract class FluidMixin implements IAEFluid {

    @Unique
    private AEFluidKey leanObject$aeKey;

    @Unique
    private WeakValueHashCache<DataComponentPatch, AEFluidKey> leanObject$componentKeys;

    @Override
    public AEFluidKey lo$getAEKey() {
        var cached = leanObject$aeKey;
        if (cached == null) {
            leanObject$aeKey = cached = AEFluidKeyAccess.of(new FluidStack((Fluid) (Object) this,1));
        }
        return cached;
    }

    @Override
    public WeakValueHashCache<DataComponentPatch, AEFluidKey> lo$getComponentAEKeyCache() {
        var cache = leanObject$componentKeys;
        if (cache == null) {
            leanObject$componentKeys = cache = new WeakValueHashCache<>(p->AEFluidKeyAccess.of(new FluidStack(((Fluid) (Object) this).builtInRegistryHolder(),1,p)));
        }
        return cache;
    }
}
