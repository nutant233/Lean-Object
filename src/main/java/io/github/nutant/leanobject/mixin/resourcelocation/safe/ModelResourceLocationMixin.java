package io.github.nutant.leanobject.mixin.resourcelocation.safe;

import io.github.nutant.leanobject.ResourceLocations;
import net.minecraft.client.resources.model.ModelResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Replaces the variant lowercasing in {@link ModelResourceLocation}'s canonical constructor with a
 * cached lookup.
 *
 * <p>1.21 turned this class into a record and collapsed the constructors down to the single
 * {@code (ResourceLocation, String)} one, so the 1.20 redirects on the removed overloads have no
 * counterpart; the record constructor keeps its fields final and cannot be overwritten, so the
 * lowercasing - the only allocation it performs - is redirected instead.
 */
@Mixin(value = ModelResourceLocation.class, priority = 100000)
public class ModelResourceLocationMixin {

    @Redirect(
            method = "<init>(Lnet/minecraft/resources/ResourceLocation;Ljava/lang/String;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelResourceLocation;lowercaseVariant(Ljava/lang/String;)Ljava/lang/String;")
    )
    private static String leanObject$cachedLowercaseVariant(String variant) {
        return ResourceLocations.VARIANT_CACHE.getCache(variant);
    }
}
