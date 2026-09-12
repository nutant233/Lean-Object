package io.github.nutant.leanobject.compat.ae2;

import appeng.api.stacks.AEItemKey;
import com.gto.fastcollection.cache.WeakValueHashCache;
import net.minecraft.nbt.CompoundTag;

public interface IAEItem {

    AEItemKey lo$getAEKey();

    WeakValueHashCache<CompoundTag, AEItemKey> lo$getTagAEKeyCache();
}
