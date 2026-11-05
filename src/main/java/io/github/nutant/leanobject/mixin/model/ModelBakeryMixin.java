package io.github.nutant.leanobject.mixin.model;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;

/**
 * Moves the bakery's lookup maps and its loading stack onto the compact containers.
 *
 * <p>1.21 no longer keeps {@code blockStateResources} on the bakery - block state loading moved to
 * {@code BlockStateModelLoader}, where BlockStateModelLoaderMixin compacts it instead. The top
 * level maps are also keyed by {@link ModelResourceLocation} rather than {@link ResourceLocation}.
 */
@Mixin(value = ModelBakery.class, priority = 100000)
public class ModelBakeryMixin {

    @Mutable
    @Shadow
    @Final
    private Map<ResourceLocation, UnbakedModel> unbakedCache;

    @Mutable
    @Shadow
    @Final
    private Map<ResourceLocation, BlockModel> modelResources;

    @Mutable
    @Shadow
    @Final
    private Map<ModelResourceLocation, BakedModel> bakedTopLevelModels;

    @Mutable
    @Shadow
    @Final
    private Set<ResourceLocation> loadingStack;

    @Mutable
    @Shadow
    @Final
    private Map<ModelResourceLocation, UnbakedModel> topLevelModels;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void leanObject$compactCollections(BlockColors blockColors, ProfilerFiller profiler,
                                               Map<ResourceLocation, BlockModel> modelResources,
                                               Map<ResourceLocation, ?> blockStateResources, CallbackInfo ci) {
        if (this.unbakedCache != null) this.unbakedCache = new Object2ObjectOpenHashMap<>(this.unbakedCache);
        if (this.modelResources != null) this.modelResources = new Object2ObjectOpenHashMap<>(this.modelResources);
        if (this.bakedTopLevelModels != null) this.bakedTopLevelModels = new Object2ObjectOpenHashMap<>(this.bakedTopLevelModels);
        if (this.topLevelModels != null) this.topLevelModels = new Object2ObjectOpenHashMap<>(this.topLevelModels);
        if (this.loadingStack != null) this.loadingStack = new ObjectOpenHashSet<>(this.loadingStack);
    }
}
