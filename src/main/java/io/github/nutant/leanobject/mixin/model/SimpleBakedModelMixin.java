package io.github.nutant.leanobject.mixin.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraftforge.client.RenderTypeGroup;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(value = SimpleBakedModel.class, priority = 100000)
public abstract class SimpleBakedModelMixin {

    @Mutable
    @Shadow
    @Final
    protected List<BakedQuad> unculledFaces;
    @Shadow
    @Final
    protected Map<Direction, List<BakedQuad>> culledFaces;

    @Inject(method = "<init>(Ljava/util/List;Ljava/util/Map;ZZZLnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/client/renderer/block/model/ItemTransforms;Lnet/minecraft/client/renderer/block/model/ItemOverrides;Lnet/minecraftforge/client/RenderTypeGroup;Lnet/minecraftforge/client/RenderTypeGroup;)V", at = @At("RETURN"))
    private void initialization(List unculledFaces, Map culledFaces, boolean hasAmbientOcclusion, boolean usesBlockLight, boolean isGui3d, TextureAtlasSprite particleIcon, ItemTransforms transforms, ItemOverrides overrides, RenderTypeGroup renderTypes, RenderTypeGroup renderTypesFast, CallbackInfo ci) {
        if (this.unculledFaces != null) this.unculledFaces = ImmutableList.copyOf(this.unculledFaces);
        if (this.culledFaces != null) for (var entry : this.culledFaces.entrySet()) {
            entry.setValue(ImmutableList.copyOf(entry.getValue()));
        }
    }

}