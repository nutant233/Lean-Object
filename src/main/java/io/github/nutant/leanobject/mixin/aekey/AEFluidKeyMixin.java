package io.github.nutant.leanobject.mixin.aekey;

import appeng.api.stacks.AEFluidKey;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.nutant.leanobject.compat.ae2.IAEFluid;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.UnaryOperator;

/**
 * Deduplicates {@link AEFluidKey} onto one instance per logical fluid value, the counterpart of
 * {@link AEItemKeyMixin}.
 */
@Mixin(value = AEFluidKey.class, priority = 100000)
public class AEFluidKeyMixin {


    /**
     * @author nutant233
     * @reason Reuse the key for stacks without components; cache the ones that carry them
     */
    @Overwrite(remap = false)
    private static AEFluidKey lambda$static$4(Holder<Fluid> fluid, DataComponentPatch patch) {
        var ae = (IAEFluid) fluid.value();
        if (patch.isEmpty()) return ae.lo$getAEKey();
        var cache = ae.lo$getComponentAEKeyCache();
        return cache.getCache(patch, cache.createFunction());
    }

    /**
     * @author nutant233
     * @reason Reuse the key for stacks without components; cache the ones that carry them
     */
    @Overwrite(remap = false)
    public static @Nullable AEFluidKey of(FluidStack stack) {
        if (stack.isEmpty()) return null;
        var ae = (IAEFluid) stack.getFluid();
        var patch = stack.getComponentsPatch();
        if (patch.isEmpty()) return ae.lo$getAEKey();
        var cache = ae.lo$getComponentAEKeyCache();
        return cache.getCache(patch, cache.createFunction());
    }

    /**
     * @author nutant233
     * @reason Reuse the key for stacks without components; cache the ones that carry them
     */
    @Overwrite(remap = false)
    public static AEFluidKey fromPacket(RegistryFriendlyByteBuf data) {
        return of(FluidStack.STREAM_CODEC.decode(data));
    }

    /**
     * @author nutant233
     * @reason Route through the deduplicating FluidStack overload
     */
    @Overwrite(remap = false)
    public static AEFluidKey of(Fluid fluid) {
        var ae = (IAEFluid) fluid;
       return ae.lo$getAEKey();
    }

    /**
     * @author nutant233
     * @reason Interned keys are unique by reference
     */
    @Overwrite(remap = false)
    public boolean equals(Object other) {
        return other == this;
    }

    /**
     * @author nutant233
     * @reason Identity hash, consistent with the identity equals above
     */
    @Overwrite(remap = false)
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
