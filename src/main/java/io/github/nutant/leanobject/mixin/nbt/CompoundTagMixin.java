package io.github.nutant.leanobject.mixin.nbt;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = CompoundTag.class, priority = 100000)
public abstract class CompoundTagMixin {

    @Shadow
    public Map<String, Tag> tags;

    /**
     * @author .
     * @reason .
     */
    @Overwrite
    protected Map<String, Tag> entries() {
        return tags;
    }

    @Inject(method = "<init>(Ljava/util/Map;)V", at = @At("TAIL"))
    private void init(Map<String, Tag> tags, CallbackInfo ci) {
        if (tags.getClass() == O2OOpenCacheHashMap.class) return;
        this.tags = new O2OOpenCacheHashMap<>(tags);
    }

    @ModifyArg(method = "<init>()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;<init>(Ljava/util/Map;)V"))
    private static Map<String, Tag> useFasterCollection(Map<String, Tag> oldMap) {
        return new O2OOpenCacheHashMap<>(0);
    }

    @Redirect(method = "<init>()V", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", remap = false))
    private static HashMap<?, ?> removeOldMapAlloc() {
        return null;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public CompoundTag copy() {
        var map = new O2OOpenCacheHashMap<String, Tag>(tags.size());
        ((Object2ObjectOpenHashMap<String, Tag>) tags).object2ObjectEntrySet().fastForEach(entry -> map.put(entry.getKey(), entry.getValue().copy()));
        return new CompoundTag(map);
    }

    @Mixin(targets = "net.minecraft.nbt.CompoundTag$1")
    static class Type {

        @ModifyVariable(method = "load(Ljava/io/DataInput;ILnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/CompoundTag;", at = @At(value = "INVOKE_ASSIGN", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", remap = false))
        private Map<String, Tag> useFasterCollection(Map<String, Tag> map) {
            return new O2OOpenCacheHashMap<>(0);
        }

        @Redirect(method = "load(Ljava/io/DataInput;ILnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/CompoundTag;", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", remap = false))
        private HashMap<?, ?> removeOldMapAlloc() {
            return null;
        }
    }
}
