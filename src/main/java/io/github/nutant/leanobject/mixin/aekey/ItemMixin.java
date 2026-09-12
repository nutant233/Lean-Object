package io.github.nutant.leanobject.mixin.aekey;

import appeng.api.stacks.AEItemKey;
import com.gto.fastcollection.cache.WeakValueHashCache;
import io.github.nutant.leanobject.compat.ae2.IAEItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Item.class)
public class ItemMixin implements IAEItem {

    @Unique
    private AEItemKey ae2$itemKey;
    @Unique
    private WeakValueHashCache<CompoundTag, AEItemKey> ae2$cache;


    @Override
    public AEItemKey lo$getAEKey() {
        var key = ae2$itemKey;
        if (key == null) {
            ae2$itemKey = key = AEItemKeyAccess.of((Item) (Object) this, null, null);
        }
        return key;
    }

    @Override
    public WeakValueHashCache<CompoundTag, AEItemKey> lo$getTagAEKeyCache() {
        var cache = this.ae2$cache;
        if (cache == null) {
            cache = this.ae2$cache = new WeakValueHashCache<>(t -> AEItemKeyAccess.of((Item) (Object) this, t, null));
        }
        return cache;
    }
}
