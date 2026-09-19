package io.github.nutant.leanobject.mixin.aekey;

import appeng.api.stacks.AEItemKey;
import com.gto.fastcollection.cache.WeakValueHashCache;
import io.github.nutant.leanobject.compat.ae2.IAEItem;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Caches the AE2 storage keys belonging to this item.
 *
 * <p>The key for the item with no components is held directly, and keys carrying component data are
 * looked up in a weak cache keyed by a copy of the stack - {@code AEItemKey.of} itself copies the
 * stack, so a copy is the right key. With one key per logical value,
 * {@link AEItemKeyMixin} can compare by reference.
 */
@Mixin(Item.class)
public abstract class ItemMixin implements IAEItem {

    @Unique
    private AEItemKey leanObject$aeKey;

    @Unique
    private WeakValueHashCache<DataComponentPatch, AEItemKey> leanObject$componentKeys;

    @Override
    public AEItemKey lo$getAEKey() {
        var cached = leanObject$aeKey;
        if (cached == null) {
            leanObject$aeKey = cached = AEItemKeyAccess.of(((Item) (Object) this).getDefaultInstance());
        }
        return cached;
    }

    @Override
    public WeakValueHashCache<DataComponentPatch, AEItemKey> lo$getComponentAEKeyCache() {
        var cache = leanObject$componentKeys;
        if (cache == null) {
            leanObject$componentKeys = cache = new WeakValueHashCache<>(p->AEItemKeyAccess.of(new ItemStack(((Item) (Object) this).builtInRegistryHolder(),1,p)));
        }
        return cache;
    }
}
