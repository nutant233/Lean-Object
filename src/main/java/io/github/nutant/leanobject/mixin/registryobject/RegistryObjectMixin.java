package io.github.nutant.leanobject.mixin.registryobject;

import net.minecraftforge.registries.RegistryObject;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RegistryObject.class)
public abstract class RegistryObjectMixin {

    private static final Throwable THROWABLE = new Throwable();

    @Redirect(method = "<init>(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/resources/ResourceLocation;Ljava/lang/String;Z)V", at = @At(value = "NEW", target = "(Ljava/lang/String;)Ljava/lang/Throwable;"))
    private Throwable allyouneed$lazyCallerStack(String message) {
        return THROWABLE;
    }
}
