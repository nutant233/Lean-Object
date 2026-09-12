package io.github.nutant.leanobject.compat.ae2;

import appeng.api.stacks.AEFluidKey;
import com.gto.fastcollection.cache.WeakValueHashCache;
import net.minecraft.nbt.CompoundTag;

public interface IAEFluid {

    AEFluidKey lo$getAEKey();

    WeakValueHashCache<CompoundTag, AEFluidKey> lo$getTagAEKeyCache();

}
