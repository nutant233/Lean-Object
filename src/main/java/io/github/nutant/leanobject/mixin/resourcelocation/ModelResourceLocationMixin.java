package io.github.nutant.leanobject.mixin.resourcelocation;

import net.minecraft.client.resources.model.ModelResourceLocation;
import io.github.nutant.leanobject.LeanObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Locale;

@Mixin(value = ModelResourceLocation.class, priority = 100000)
public class ModelResourceLocationMixin {

    @Redirect(method = "<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelResourceLocation;lowercaseVariant(Ljava/lang/String;)Ljava/lang/String;"))
    private String a(String variant) {
        return LeanObject.VARIANT_CACHE.getCache(variant);
    }

    @Redirect(method = "<init>(Lnet/minecraft/resources/ResourceLocation;Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelResourceLocation;lowercaseVariant(Ljava/lang/String;)Ljava/lang/String;"))
    private static String b(String variant) {
        return LeanObject.VARIANT_CACHE.getCache(variant);
    }


}
