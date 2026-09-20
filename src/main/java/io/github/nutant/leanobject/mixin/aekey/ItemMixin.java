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
 * <p>Two values are held: the key of {@code new ItemStack(item)}, which is the value an empty component
 * patch has, and the key of the item's default stack, which is not the same thing for items whose
 * defaults carry components - a potion's {@code getDefaultInstance()} has {@code potion_contents} while
 * a freshly built stack has none.
 *
 * <p>Both are built outside the monitor. AE2's constructor asks the item for its stack size and damage,
 * and a mod's override of those hooks may do anything - including construct a key itself - so running
 * one under this lock can deadlock two threads that reach the same key together. Publication is what
 * synchronizes: the thread that loses the race drops its own instance and returns the published one,
 * because these keys compare by identity.
 *
 * <p>Both caches build their entries through {@link AEItemKeyAccess}, which invokes AE2's private
 * constructor: the public factories are overwritten to read these very caches, so they cannot be used
 * to produce the value that is being cached.
 */
@Mixin(Item.class)
public abstract class ItemMixin implements IAEItem {

    @Unique
    private volatile AEItemKey leanObject$aeKey;
    @Unique
    private volatile AEItemKey leanObject$defaultStackAEKey;

    @Unique
    private volatile WeakValueHashCache<DataComponentPatch, AEItemKey> leanObject$componentKeys;

    @Override
    public AEItemKey lo$getAEKey() {
        var cached = leanObject$aeKey;
        if (cached != null) return cached;
        var key = AEItemKeyAccess.of(new ItemStack((Item) (Object) this));
        synchronized (this) {
            if (leanObject$aeKey == null) {
                leanObject$aeKey = key;
                return key;
            }
            return leanObject$aeKey;
        }
    }

    @Override
    public AEItemKey lo$getDefaultStackAEKey() {
        var cached = leanObject$defaultStackAEKey;
        if (cached != null) return cached;
        var key = AEItemKey.of(((Item) (Object) this).getDefaultInstance());
        synchronized (this) {
            if (leanObject$defaultStackAEKey == null) {
                leanObject$defaultStackAEKey = key;
                return key;
            }
            return leanObject$defaultStackAEKey;
        }
    }

    @Override
    public WeakValueHashCache<DataComponentPatch, AEItemKey> lo$getComponentAEKeyCache() {
        var cache = leanObject$componentKeys;
        if (cache != null) return cache;
        var created = new WeakValueHashCache<DataComponentPatch, AEItemKey>(
                p -> AEItemKeyAccess.of(new ItemStack(((Item) (Object) this).builtInRegistryHolder(), 1, p)));
        synchronized (this) {
            if (leanObject$componentKeys == null) {
                leanObject$componentKeys = created;
                return created;
            }
            return leanObject$componentKeys;
        }
    }
}
