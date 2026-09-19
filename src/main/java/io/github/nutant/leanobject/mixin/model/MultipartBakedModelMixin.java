package io.github.nutant.leanobject.mixin.model;

import com.google.common.collect.ImmutableList;
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

import java.util.List;
import java.util.function.Predicate;

/**
 * Freezes the selector list into an {@link ImmutableList}, which is smaller than the list the bakery
 * hands over.
 *
 * <p>Only the list is left to do here: 1.21 already builds the selector cache as a reference-keyed
 * map itself, so the 1.20 swap for it has no counterpart.
 */
@Mixin(value = MultiPartBakedModel.class, priority = 100000)
public abstract class MultipartBakedModelMixin {

    @Mutable
    @Shadow
    @Final
    private List<Pair<Predicate<BlockState>, BakedModel>> selectors;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void leanObject$compactCollections(List<Pair<Predicate<BlockState>, BakedModel>> selectors, CallbackInfo ci) {
        if (this.selectors != null) this.selectors = ImmutableList.copyOf(this.selectors);
    }
}
