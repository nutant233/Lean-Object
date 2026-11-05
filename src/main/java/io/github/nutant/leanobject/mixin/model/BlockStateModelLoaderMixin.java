package io.github.nutant.leanobject.mixin.model;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

/**
 * Compacts the block state resource map.
 *
 * <p>1.20 kept this map on {@code ModelBakery}; 1.21 moved block state loading into
 * {@link BlockStateModelLoader}, which is where the same map now lives, so the compaction moved with it.
 */
@Mixin(value = BlockStateModelLoader.class, priority = 100000)
public class BlockStateModelLoaderMixin {

    @Mutable
    @Shadow
    @Final
    private Map<ResourceLocation, List<BlockStateModelLoader.LoadedJson>> blockStateResources;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void leanObject$compactBlockStateResources(CallbackInfo ci) {
        if (this.blockStateResources != null) {
            this.blockStateResources = new Object2ObjectOpenHashMap<>(this.blockStateResources);
        }
    }
}