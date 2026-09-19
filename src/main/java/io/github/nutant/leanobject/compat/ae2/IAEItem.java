package io.github.nutant.leanobject.compat.ae2;

import appeng.api.stacks.AEItemKey;
import com.gto.fastcollection.cache.WeakValueHashCache;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;

/**
 * Implemented by the mixin on {@code Item} so an item can hand out its shared {@link AEItemKey}.
 */
public interface IAEItem {

    /** The key for this item with no components, cached on the item itself. */
    AEItemKey lo$getAEKey();

    /** The cache for keys of this item that carry component data. */
    WeakValueHashCache<DataComponentPatch, AEItemKey> lo$getComponentAEKeyCache();
}
