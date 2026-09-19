package io.github.nutant.leanobject.mixin.nbt;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Replaces the backing map of {@link CompoundTag} with a leaner open-addressing map, on every path that
 * creates one - the empty constructor, the map constructor and NBT deserialization - and keeps
 * {@link #copy()} on the same container.
 *
 * <p>The vanilla {@code Maps.newHashMap()} allocations are redirected to {@code null} so the map they
 * would have produced is never built at all; the replacement is supplied by the neighbouring
 * {@code @ModifyArg} / {@code @ModifyVariable}.
 */
@Mixin(value = CompoundTag.class, priority = 100000)
public abstract class CompoundTagMixin {

    @Shadow
    public Map<String, Tag> tags;

    /**
     * @author nutant233
     * @reason Keep deserialized and passed-in maps on the compact container
     */
    @Inject(method = "<init>(Ljava/util/Map;)V", at = @At("TAIL"))
    private void leanObject$compactMap(Map<String, Tag> tags, CallbackInfo ci) {
        if (tags.getClass() == O2OOpenCacheHashMap.class) return;
        this.tags = new O2OOpenCacheHashMap<>(tags);
    }

    @ModifyArg(method = "<init>()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;<init>(Ljava/util/Map;)V"))
    private static Map<String, Tag> leanObject$useCompactMap(Map<String, Tag> original) {
        return new O2OOpenCacheHashMap<>(0);
    }

    @Redirect(method = "<init>()V", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", remap = false))
    private static HashMap<?, ?> leanObject$skipVanillaMapAlloc() {
        return null;
    }

    /**
     * @author nutant233
     * @reason Copy into the compact container and iterate it without entry-set boxing
     */
    @Overwrite
    public CompoundTag copy() {
        var map = new O2OOpenCacheHashMap<String, Tag>(tags.size());
        ((Object2ObjectOpenHashMap<String, Tag>) tags).object2ObjectEntrySet().fastForEach(entry -> map.put(entry.getKey(), entry.getValue().copy()));
        return new CompoundTag(map);
    }

    @Mixin(targets = "net.minecraft.nbt.CompoundTag$1")
    static class Type {

        @ModifyVariable(method = "loadCompound", require = 1, at = @At(value = "INVOKE_ASSIGN", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", remap = false))
        private static Map<String, Tag> leanObject$useCompactMap(Map<String, Tag> map) {
            return new O2OOpenCacheHashMap<>(0);
        }

        @Redirect(method = "loadCompound", require = 1, at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", remap = false))
        private static HashMap<?, ?> leanObject$skipVanillaMapAlloc() {
            return null;
        }
    }
}
