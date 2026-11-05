package io.github.nutant.leanobject.mixin.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

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

    /**
     * @author
     * @reason
     */
    @Overwrite
    public CompoundTag copy() {
        var map = new HashMap<String, Tag>(tags.size());
        tags.forEach((k,v) -> map.put(k, v.copy()));
        return new CompoundTag(map);
    }
}
