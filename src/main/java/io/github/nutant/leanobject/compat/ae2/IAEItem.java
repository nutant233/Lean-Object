package io.github.nutant.leanobject.compat.ae2;

import appeng.api.stacks.AEItemKey;
import com.gto.fastcollection.cache.WeakValueHashCache;
import net.minecraft.core.component.DataComponentPatch;

/**
 * Implemented by the mixin on {@code Item} so an item can hand out its shared {@link AEItemKey}.
 */
public interface IAEItem {

    AEItemKey lo$getAEKey();
    AEItemKey lo$getDefaultStackAEKey();

    /** The cache for keys of this item that carry component data. */
    WeakValueHashCache<DataComponentPatch, AEItemKey> lo$getComponentAEKeyCache();
}
