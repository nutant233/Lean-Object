package io.github.nutant.leanobject.mixin.resourcekey;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import io.github.nutant.leanobject.ResourceKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = ResourceKey.class, priority = 100000)
@SuppressWarnings("rawtypes")
public class ResourceKeyMixin {

    /**
     * @author nutant233
     * @reason Route codec through LeanObject's intern cache
     */
    @Overwrite
    public static <T> Codec codec(ResourceKey<? extends Registry<T>> registryName) {
        var cache = ResourceKeys.RESOURCEKEY_INTERNING_MAP.getCache(registryName);
        return ResourceLocation.CODEC.xmap(cache::getCache, ResourceKey::location);
    }

    /**
     * @author nutant233
     * @reason Intern ResourceKey via registry-key cache
     */
    @Overwrite
    public static <T> ResourceKey create(ResourceKey<? extends Registry<T>> registryName, ResourceLocation location) {
        return ResourceKeys.RESOURCEKEY_INTERNING_MAP.getCache(registryName).getCache(location);
    }

    /**
     * @author nutant233
     * @reason Intern root registry keys
     */
    @Overwrite
    public static ResourceKey createRegistryKey(ResourceLocation identifier) {
        return ResourceKeys.ROOT_REGISTRY_MAP.getCache(identifier);
    }

    /**
     * @author nutant233
     * @reason Intern ResourceKey via registry-location cache
     */
    @Overwrite
    public static ResourceKey create(ResourceLocation registryName, ResourceLocation location) {
        return ResourceKeys.RESOURCE_LOCATION_TO_RESOURCEKEY_INTERNING_MAP.getCache(registryName).getCache(location);
    }

    /**
     * @author nutant233
     * @reason Identity equals — interned keys are unique by reference
     */
    @Overwrite(remap = false)
    public boolean equals(Object o) {
        return o == this;
    }
}
