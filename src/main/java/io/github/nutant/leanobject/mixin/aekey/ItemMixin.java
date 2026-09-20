package io.github.nutant.leanobject.mixin.aekey;

import appeng.api.stacks.AEItemKey;
import com.gto.fastcollection.cache.WeakValueHashCache;
import io.github.nutant.leanobject.compat.ae2.IAEItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Caches the AE2 storage keys belonging to this item.
 *
 * <p>Both values are built outside the monitor. AE2's constructor asks the item for its own values, and
 * a mod override of those hooks may do anything - including constructing a key itself - so running one
 * under this lock deadlocks two threads that reach the same key together. Publication is what
 * synchronizes: the thread that loses the race drops its own instance and returns the published one,
 * because these keys are compared by identity.
 */
@Mixin(Item.class)
public class ItemMixin implements IAEItem {

    @Unique
    private volatile AEItemKey ae2$itemKey;
    @Unique
    private volatile WeakValueHashCache<CompoundTag, AEItemKey> ae2$cache;


    @Override
    public AEItemKey lo$getAEKey() {
        var key = ae2$itemKey;
        if (key != null) return key;
        var created = AEItemKeyAccess.of((Item) (Object) this, null, null);
        synchronized (this) {
            if (ae2$itemKey == null) {
                ae2$itemKey = created;
                return created;
            }
            return ae2$itemKey;
        }
    }

    @Override
    public WeakValueHashCache<CompoundTag, AEItemKey> lo$getTagAEKeyCache() {
        var cache = this.ae2$cache;
        if (cache != null) return cache;
        var created = new WeakValueHashCache<CompoundTag, AEItemKey>(t -> AEItemKeyAccess.of((Item) (Object) this, t, null));
        synchronized (this) {
            if (ae2$cache == null) {
                ae2$cache = created;
                return created;
            }
            return ae2$cache;
        }
    }
}
