package io.github.nutant.leanobject.mixin.model;

import net.minecraft.client.renderer.block.model.BlockElementFace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Interns the texture name carried by a model face.
 *
 * <p>A block model names the same texture on many faces, and each face holds its own String; interning makes
 * them one instance, which also lets the model bakery's cache hit on reference equality afterwards.
 */
@Mixin(BlockElementFace.class)
public class BlockElementFaceMixin {

    @ModifyVariable(method = "<init>(Lnet/minecraft/core/Direction;ILjava/lang/String;Lnet/minecraft/client/renderer/block/model/BlockFaceUV;)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static String leanObject$internTexture(String texture) {
        return texture == null ? null : texture.intern();
    }
}