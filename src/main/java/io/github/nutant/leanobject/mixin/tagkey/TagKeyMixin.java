package io.github.nutant.leanobject.mixin.tagkey;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import io.github.nutant.leanobject.TagKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = TagKey.class, priority = 100000)
@SuppressWarnings("rawtypes")
public class TagKeyMixin {

    /**
     * @author nutant233
     * @reason Route codec through LeanObject's TagKey intern cache
     */
    @Overwrite
    public static <T> Codec codec(ResourceKey<? extends Registry<T>> registryName) {
        var cache = TagKeys.TAG_INTERNING_MAP.getCache(registryName);
        return ResourceLocation.CODEC.xmap(cache::getCache, TagKey::location);
    }

    /**
     * @author nutant233
     * @reason Route hashed codec through LeanObject's TagKey intern cache
     */
    @Overwrite
    public static <T> Codec hashedCodec(ResourceKey<? extends Registry<T>> registryName) {
        var cache = TagKeys.TAG_INTERNING_MAP.getCache(registryName);
        return Codec.STRING.comapFlatMap(name -> name.startsWith("#") ? ResourceLocation.read(name.substring(1)).map(cache::getCache) : DataResult.error(() -> "Not a tag id"), e -> "#" + e.location());
    }

    /**
     * @author nutant233
     * @reason Intern TagKey instances
     */
    @Overwrite
    public static <T> TagKey create(ResourceKey<? extends Registry<T>> registry, ResourceLocation location) {
        return TagKeys.TAG_INTERNING_MAP.getCache(registry).getCache(location);
    }

    /**
     * @author nutant233
     * @reason Identity hashCode for faster HashMap key use after interning
     */
    @Overwrite(remap = false)
    public final int hashCode() {
        return System.identityHashCode(this);
    }

    /**
     * @author nutant233
     * @reason Identity equals — interned tags are unique by reference
     */
    @Overwrite(remap = false)
    public final boolean equals(Object o) {
        return o == this;
    }
}
