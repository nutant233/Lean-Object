package io.github.nutant.leanobject.mixin.resourcekey.safe;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import io.github.nutant.leanobject.ResourceKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ResourceKey.class, priority = 100000)
@SuppressWarnings("rawtypes")
public class ResourceKeyMixin {

    @Inject(method = "codec", at = @At("HEAD"), cancellable = true)
    private static <T> void codec(ResourceKey<? extends Registry<T>> registryKey, CallbackInfoReturnable<Codec<ResourceKey>> cir) {
        var cache = ResourceKeys.RESOURCEKEY_INTERNING_MAP.getCache(registryKey);
        cir.setReturnValue(ResourceLocation.CODEC.xmap(cache::getCache, ResourceKey::location));
    }

    @Inject(method = "create(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/resources/ResourceKey;", at = @At("HEAD"), cancellable = true)
    private static <T> void createFromRegistryKey(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation location, CallbackInfoReturnable<ResourceKey> cir) {
        cir.setReturnValue(ResourceKeys.RESOURCEKEY_INTERNING_MAP.getCache(registryKey).getCache(location));
    }

    @Inject(method = "createRegistryKey", at = @At("HEAD"), cancellable = true)
    private static void createRegistryKey(ResourceLocation location, CallbackInfoReturnable<ResourceKey> cir) {
        cir.setReturnValue(ResourceKeys.ROOT_REGISTRY_MAP.getCache(location));
    }

    @Inject(method = "create(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/resources/ResourceKey;", at = @At("HEAD"), cancellable = true)
    private static void createFromLocation(ResourceLocation registryName, ResourceLocation location, CallbackInfoReturnable<ResourceKey> cir) {
        cir.setReturnValue(ResourceKeys.RESOURCE_LOCATION_TO_RESOURCEKEY_INTERNING_MAP.getCache(registryName).getCache(location));
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
