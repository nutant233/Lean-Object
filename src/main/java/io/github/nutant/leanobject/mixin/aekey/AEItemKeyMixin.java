package io.github.nutant.leanobject.mixin.aekey;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import io.github.nutant.leanobject.compat.ae2.IAEFluid;
import io.github.nutant.leanobject.compat.ae2.IAEItem;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.UnaryOperator;

/**
 * Deduplicates {@link AEItemKey} onto one instance per logical item value and reduces comparison to a
 * reference check.
 *
 * <p>AE2 builds a fresh key - and computes {@code ItemStack.hashItemAndComponents} - on every
 * {@code of} call, and a large ME network calls this constantly. Both {@code of} overloads route
 * through the per-item cache, so repeats return the existing key instead of allocating and hashing
 * another one, and {@code equals} can then be {@code ==}. The component-carrying keys are keyed by a
 * copy of the stack, matching what AE2 itself stores.
 */
@Mixin(value = AEItemKey.class, priority = 100000)
public class AEItemKeyMixin {


    /**
     * @author nutant233
     * @reason Reuse the key for stacks without components; cache the ones that carry them
     */
    @Overwrite(remap = false)
    private static AEItemKey lambda$static$4(Holder<Item> item, DataComponentPatch patch) {
        var ae = (IAEItem) item.value();
        if (patch.isEmpty()) return ae.lo$getAEKey();
        var cache = ae.lo$getComponentAEKeyCache();
        return cache.getCache(patch, cache.createFunction());
    }

    /**
     * @author nutant233
     * @reason Reuse the key for stacks without components; cache the ones that carry them
     */
    @Overwrite(remap = false)
    public static @Nullable AEItemKey of(ItemStack stack) {
        if (stack.isEmpty()) return null;
        var ae = (IAEItem) stack.getItem();
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
    public static AEItemKey fromPacket(RegistryFriendlyByteBuf data) {
        return of(ItemStack.STREAM_CODEC.decode(data));
    }

    /**
     * @author nutant233
     * @reason Route through the deduplicating ItemStack overload
     */
    @Overwrite(remap = false)
    public static AEItemKey of(ItemLike item) {
        var ae = (IAEItem) item.asItem();
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
