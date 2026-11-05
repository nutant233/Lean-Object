package io.github.nutant.leanobject.mixin.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = CompoundTag.class, priority = 100000)
public abstract class CompoundTagMixin {

    @Shadow
    public Map<String, Tag> tags;

    /**
     * @author nutant233
     * @reason Copy into a map sized for the source instead of growing one entry at a time
     */
    @Overwrite
    public CompoundTag copy() {
        var map = new HashMap<String, Tag>(tags.size());
        tags.forEach((k, v) -> map.put(k, v.copy()));
        return new CompoundTag(map);
    }
}