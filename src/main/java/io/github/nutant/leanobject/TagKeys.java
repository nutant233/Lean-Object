package io.github.nutant.leanobject;

import com.gto.fastcollection.cache.IdentityHashCache;
import com.gto.fastcollection.cache.WeakValueHashCache;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

@SuppressWarnings("rawtypes")
public final class TagKeys {

    public static final IdentityHashCache<ResourceKey, WeakValueHashCache<ResourceLocation, TagKey>> TAG_INTERNING_MAP = new IdentityHashCache<>(k -> new WeakValueHashCache<>(id -> new TagKey(k, id)));

    public static final WeakValueHashCache<ResourceLocation, TagKey> ITEM = TAG_INTERNING_MAP.getCache(Registries.ITEM);
    public static final WeakValueHashCache<ResourceLocation, TagKey> BLOCK = TAG_INTERNING_MAP.getCache(Registries.BLOCK);
    public static final WeakValueHashCache<ResourceLocation, TagKey> FLUID = TAG_INTERNING_MAP.getCache(Registries.FLUID);
    public static final WeakValueHashCache<ResourceLocation, TagKey> BIOME = TAG_INTERNING_MAP.getCache(Registries.BIOME);
    public static final WeakValueHashCache<ResourceLocation, TagKey> ENTITY_TYPE = TAG_INTERNING_MAP.getCache(Registries.ENTITY_TYPE);
}
