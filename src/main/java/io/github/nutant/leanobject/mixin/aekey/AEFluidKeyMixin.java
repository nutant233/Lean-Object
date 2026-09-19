package io.github.nutant.leanobject.mixin.aekey;

import appeng.api.stacks.AEFluidKey;
import io.github.nutant.leanobject.compat.ae2.IAEFluid;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Deduplicates {@link AEFluidKey} onto one instance per logical fluid value, the counterpart of
 * {@link AEItemKeyMixin}.
 *
 * <p>The same shape applies: every factory that produces a key - both {@code of} overloads, the packet
 * reader and the codec factory that also backs {@code fromTag} - resolves through the per-fluid cache,
 * so a repeated value returns the existing key instead of allocating and hashing another one. Keys
 * carrying components are cached per fluid, keyed by their {@link DataComponentPatch}.
 *
 * <p>With one key per logical value, {@code equals} can be a reference check and the hash AE2 caches in
 * its constructor can be the identity hash - see the redirect below.
 */
@Mixin(value = AEFluidKey.class, priority = 100000)
public class AEFluidKeyMixin {

    @Shadow
    @Final
    private FluidStack stack;

    /**
     * AE2 computes the content hash in its constructor and stores it in a final field which
     * {@code hashCode()} merely returns. Injecting at the computation site therefore turns the cached
     * hash into the identity hash while leaving {@code hashCode()} itself - and every other use of the
     * field - untouched, keeping it consistent with the reference {@code equals} below.
     */
    @Redirect(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/fluids/FluidStack;hashFluidAndComponents(Lnet/neoforged/neoforge/fluids/FluidStack;)I"),
            remap = false
    )
    private int hash(FluidStack stack) {
        return System.identityHashCode(this);
    }

    /**
     * @author nutant233
     * @reason Codec factory behind {@code CODEC}/{@code fromTag}: reuse the component-free key, cache the
     * ones that carry components
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
     * @reason Route through the deduplicating FluidStack overload
     */
    @Overwrite(remap = false)
    public static AEFluidKey fromPacket(RegistryFriendlyByteBuf data) {
        return of(FluidStack.STREAM_CODEC.decode(data));
    }

    /**
     * @author nutant233
     * @reason Return the fluid's shared component-free key
     */
    @Overwrite(remap = false)
    public static AEFluidKey of(Fluid fluid) {
        var ae = (IAEFluid) fluid;
        return ae.lo$getAEKey();
    }

    /**
     * @author nutant233
     * @reason Return the fluid's shared component-free key
     */
    @Overwrite(remap = false)
    public AEFluidKey dropSecondary() {
        var ae = (IAEFluid) stack.getFluid();
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
}
