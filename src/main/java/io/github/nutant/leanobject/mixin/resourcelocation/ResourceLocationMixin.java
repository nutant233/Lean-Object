package io.github.nutant.leanobject.mixin.resourcelocation;

import net.minecraft.resources.ResourceLocation;
import io.github.nutant.leanobject.LeanObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value = ResourceLocation.class, priority = 100000)
public class ResourceLocationMixin {

    @Shadow
    private static String assertValidNamespace(String namespae, String path) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    private static String assertValidPath(String namespace, String path) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Redirect(method = "<init>(Ljava/lang/String;Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;assertValidNamespace(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"))
    private static String initnamespace(String namespae, String path) {
        return LeanObject.NAMESPACE_INTERNER.intern(namespae, s -> assertValidNamespace(s, path));
    }

    @Redirect(method = "<init>(Ljava/lang/String;Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;assertValidPath(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"))
    private static String initpath(String namespace, String path) {
        return LeanObject.PATH_INTERNER.intern(path, s -> assertValidPath(namespace, s));
    }

    @Redirect(method = "withPath(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;assertValidPath(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"))
    private String withpath(String namespace, String path) {
        return LeanObject.PATH_INTERNER.intern(path, s -> assertValidPath(namespace, s));
    }

    @Redirect(method = "withDefaultNamespace", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;assertValidPath(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"))
    private static String withdefaultnamespace(String namespace, String path) {
        return LeanObject.PATH_INTERNER.intern(path, s -> assertValidPath(namespace, s));
    }
}
