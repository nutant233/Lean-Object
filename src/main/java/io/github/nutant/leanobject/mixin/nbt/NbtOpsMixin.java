package io.github.nutant.leanobject.mixin.nbt;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(targets = "net.minecraft.nbt.NbtOps$NbtRecordBuilder", priority = 100000)
public class NbtOpsMixin {

    /**
     * @author .
     * @reason .
     */
    @Overwrite
    protected DataResult<Tag> build(CompoundTag p_129190_, Tag p_129191_) {
        if (p_129191_ != null && p_129191_ != EndTag.INSTANCE) {
            if (!(p_129191_ instanceof CompoundTag compoundtag)) {
                return DataResult.error(() -> "mergeToMap called with not a map: " + p_129191_, p_129191_);
            } else {
                CompoundTag compoundtag1 = new CompoundTag(new O2OOpenCacheHashMap<>(compoundtag.tags));
                p_129190_.tags.forEach(compoundtag1::put);
                return DataResult.success(compoundtag1);
            }
        } else {
            return DataResult.success(p_129190_);
        }
    }
}
