package io.github.nutant.leanobject;

import com.gto.fastcollection.cache.HashCache;
import com.gto.fastcollection.cache.IdentityHashCache;
import com.gto.fastcollection.cache.WeakHashInterner;
import com.gto.fastcollection.cache.WeakValueHashCache;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Function;

@SuppressWarnings("rawtypes")
public final class ResourceKeys {

    public static final HashCache<ResourceLocation, WeakValueHashCache<ResourceLocation, ResourceKey>> RESOURCE_LOCATION_TO_RESOURCEKEY_INTERNING_MAP = new HashCache<>(k -> new WeakValueHashCache<>(id -> new ResourceKey(k, id)));
    public static final IdentityHashCache<ResourceKey, WeakValueHashCache<ResourceLocation, ResourceKey>> RESOURCEKEY_INTERNING_MAP = new IdentityHashCache<>(k -> RESOURCE_LOCATION_TO_RESOURCEKEY_INTERNING_MAP.getCache(k.location()));
    public static final WeakValueHashCache<ResourceLocation, ResourceKey> ROOT_REGISTRY_MAP = RESOURCE_LOCATION_TO_RESOURCEKEY_INTERNING_MAP.getCache(BuiltInRegistries.ROOT_REGISTRY_NAME);

}
