package io.github.nutant.leanobject.mixin.aekey;

import appeng.api.stacks.AEFluidKey;
import com.gto.fastcollection.cache.WeakValueHashCache;
import io.github.nutant.leanobject.compat.ae2.IAEFluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Caches the AE2 storage keys belonging to this fluid, the counterpart of {@link ItemMixin}: both values
 * are built outside the monitor, and the thread that loses a race publishes and returns the winner's
 * instance, which identity comparison requires.
 */
@Mixin(Fluid.class)
public class FluidMixin implements IAEFluid {

    @Unique
    private volatile AEFluidKey ae2$itemKey;
    @Unique
    private volatile WeakValueHashCache<CompoundTag, AEFluidKey> ae2$cache;

    @Override
    public AEFluidKey lo$getAEKey() {
        var key = ae2$itemKey;
        if (key != null) return key;
        var created = AEFluidKeyAccess.of((Fluid) (Object) this, null);
        synchronized (this) {
            if (ae2$itemKey == null) {
                ae2$itemKey = created;
                return created;
            }
            return ae2$itemKey;
        }
    }

    @Override
    public WeakValueHashCache<CompoundTag, AEFluidKey> lo$getTagAEKeyCache() {
        var cache = this.ae2$cache;
        if (cache != null) return cache;
        var created = new WeakValueHashCache<CompoundTag, AEFluidKey>(t -> AEFluidKeyAccess.of((Fluid) (Object) this, t));
        synchronized (this) {
            if (ae2$cache == null) {
                ae2$cache = created;
                return created;
            }
            return ae2$cache;
        }
    }
}
