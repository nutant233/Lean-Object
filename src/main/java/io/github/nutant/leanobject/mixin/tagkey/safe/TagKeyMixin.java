package io.github.nutant.leanobject.mixin.tagkey.safe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import io.github.nutant.leanobject.TagKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TagKey.class, priority = 100000)
@SuppressWarnings("rawtypes")
public class TagKeyMixin {

    @Inject(method = "codec", at = @At("HEAD"), cancellable = true)
    private static <T> void codec(ResourceKey<? extends Registry<T>> registry, CallbackInfoReturnable<Codec<TagKey>> cir) {
        var cache = TagKeys.TAG_INTERNING_MAP.getCache(registry);
        cir.setReturnValue(ResourceLocation.CODEC.xmap(cache::getCache, TagKey::location));
    }

    @Inject(method = "hashedCodec", at = @At("HEAD"), cancellable = true)
    private static <T> void hashedCodec(ResourceKey<? extends Registry<T>> registry, CallbackInfoReturnable<Codec<TagKey>> cir) {
        var cache = TagKeys.TAG_INTERNING_MAP.getCache(registry);
        cir.setReturnValue(Codec.STRING.comapFlatMap(name -> name.startsWith("#") ? ResourceLocation.read(name.substring(1)).map(cache::getCache) : DataResult.error(() -> "Not a tag id"), e -> "#" + e.location()));
    }

    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private static <T> void create(ResourceKey<? extends Registry<T>> registry, ResourceLocation location, CallbackInfoReturnable<TagKey> cir) {
        cir.setReturnValue(TagKeys.TAG_INTERNING_MAP.getCache(registry).getCache(location));
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
