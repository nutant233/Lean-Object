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
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Deduplicates {@link AEItemKey} onto one instance per logical item value and reduces comparison to a
 * reference check.
 *
 * <p>AE2 builds a fresh key - copying the stack and hashing it - on every {@code of} call, and a large
 * ME network calls this constantly. Every factory that produces a key goes through the per-item cache:
 * the two {@code of} overloads, the packet reader, and the codec factory that also backs
 * {@code fromTag}. A miss runs the cache level's own factory, which constructs the key directly, so
 * the vanilla key is still the only one created per value.
 *
 * <p>The keys that carry components are cached per item, keyed by their
 * {@link DataComponentPatch} - which is what identifies a key beyond the item itself. AE2's own
 * component-carrying keys are built from exactly that pair, so the same patch always resolves to the
 * same instance.
 *
 * <p>With one key per logical value, {@code equals} can be a reference check, and the hash AE2 caches
 * in its constructor can be the identity hash - see the redirect below.
 */
@Mixin(value = AEItemKey.class, priority = 100000)
public class AEItemKeyMixin {

    @Shadow
    @Final
    private ItemStack stack;

    /**
     * AE2 computes the content hash in its constructor and stores it in a final field which
     * {@code hashCode()} merely returns. Injecting at the computation site therefore turns the cached
     * hash into the identity hash while leaving {@code hashCode()} itself - and every other use of the
     * field - untouched, keeping it consistent with the reference {@code equals} below.
     */
    @Redirect(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hashItemAndComponents(Lnet/minecraft/world/item/ItemStack;)I"),
            remap = false
    )
    private int hash(ItemStack p_331961_) {
        return System.identityHashCode(this);
    }

    /**
     * @author nutant233
     * @reason Codec factory behind {@code CODEC}/{@code fromTag}: reuse the component-free key, cache the
     * ones that carry components
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
     * @reason Return the item's shared component-free key
     */
    @Overwrite(remap = false)
    public AEItemKey dropSecondary() {
        var ae = (IAEItem) stack.getItem();
        return ae.lo$getAEKey();
    }


    /**
     * @author nutant233
     * @reason Route through the deduplicating ItemStack overload
     */
    @Overwrite(remap = false)
    public static AEItemKey fromPacket(RegistryFriendlyByteBuf data) {
        return of(ItemStack.STREAM_CODEC.decode(data));
    }

    /**
     * @author nutant233
     * @reason Return the item's shared component-free key
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
}
