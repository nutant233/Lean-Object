package io.github.nutant.leanobject.mixin.model;

import net.minecraft.client.renderer.block.model.BlockElementFace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BlockElementFace.class)
public class BlockElementFaceMixin {

    @ModifyVariable(method = "<init>(Lnet/minecraft/core/Direction;ILjava/lang/String;Lnet/minecraft/client/renderer/block/model/BlockFaceUV;Lnet/minecraftforge/client/model/ForgeFaceData;)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static String allyouneed$internTexture(String texture) {
        return texture == null ? null : texture.intern();
    }
}
