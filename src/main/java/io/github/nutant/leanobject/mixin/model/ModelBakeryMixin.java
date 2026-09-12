package io.github.nutant.leanobject.mixin.model;

import com.google.common.collect.ImmutableList;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private Map<ResourceLocation, BakedModel> bakedTopLevelModels;
    @Mutable
    @Shadow
    @Final
    private Set<ResourceLocation> loadingStack;
    @Mutable
    @Shadow
    @Final
    private Map<ResourceLocation, UnbakedModel> topLevelModels;

    @Mutable
    @Shadow
    @Final
    private Map<ResourceLocation, List<ModelBakery.LoadedJson>> blockStateResources;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinit(BlockColors blockColors, ProfilerFiller profilerFiller, Map modelResources, Map blockStateResources, CallbackInfo ci) {
        if (this.unbakedCache != null) this.unbakedCache = new O2OOpenCacheHashMap<>(this.unbakedCache);
        if (this.bakedTopLevelModels != null)
            this.bakedTopLevelModels = new O2OOpenCacheHashMap<>(this.bakedTopLevelModels);
        if (this.modelResources != null) this.modelResources = new O2OOpenCacheHashMap<>(this.modelResources);
        if (this.topLevelModels != null) this.topLevelModels = new O2OOpenCacheHashMap<>(this.topLevelModels);
        if (this.loadingStack != null) this.loadingStack = new OpenCacheHashSet<>(this.loadingStack);
        if (this.blockStateResources != null) {
            var bsr = new O2OOpenCacheHashMap<ResourceLocation, List<ModelBakery.LoadedJson>>(this.blockStateResources.size());
            for (var entry : this.blockStateResources.entrySet()) {
                bsr.put(entry.getKey(), ImmutableList.copyOf(entry.getValue()));
            }
            this.blockStateResources = Map.copyOf(bsr);
        }
    }
}
