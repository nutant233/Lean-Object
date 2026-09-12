package io.github.nutant.leanobject.mixin.aekey;

import appeng.api.stacks.AEFluidKey;
import com.gto.fastcollection.cache.WeakValueHashCache;
import io.github.nutant.leanobject.compat.ae2.IAEFluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Fluid.class)
public class FluidMixin implements IAEFluid {

    @Unique
    private AEFluidKey ae2$itemKey;
    @Unique
    private WeakValueHashCache<CompoundTag, AEFluidKey> ae2$cache;

    @Override
    public AEFluidKey lo$getAEKey() {
        var key = ae2$itemKey;
        if (key == null) {
            ae2$itemKey = key = AEFluidKeyAccess.of((Fluid) (Object) this, null);
        }
        return key;
    }

    @Override
    public WeakValueHashCache<CompoundTag, AEFluidKey> lo$getTagAEKeyCache() {
        var cache = this.ae2$cache;
        if (cache == null) {
            cache = this.ae2$cache = new WeakValueHashCache<>(t -> AEFluidKeyAccess.of((Fluid) (Object) this, t));
        }
        return cache;
    }
}
