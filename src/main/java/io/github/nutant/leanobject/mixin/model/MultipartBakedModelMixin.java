package io.github.nutant.leanobject.mixin.model;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Mixin(value = MultiPartBakedModel.class, priority = 100000)
public abstract class MultipartBakedModelMixin {

    @Mutable
    @Shadow
    @Final
    private Map<BlockState, BitSet> selectorCache;

    @Mutable
    @Shadow
    @Final
    private List<Pair<Predicate<BlockState>, BakedModel>> selectors;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void changeCollections(List<Pair<Predicate<BlockState>, BakedModel>> selectors, CallbackInfo ci) {
        if (this.selectorCache != null) this.selectorCache = new Reference2ObjectOpenHashMap<>();
        if (this.selectors != null) this.selectors = ImmutableList.copyOf(this.selectors);
    }
}