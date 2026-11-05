package io.github.nutant.leanobject.mixin.nbt;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = ListTag.class, priority = 100000)
public class ListTagMixin {

    @Shadow
    private byte type;

    @Shadow
    @Final
    private List<Tag> list;

    /**
     * @author .
     * @reason .
     */
    @Overwrite
    public ListTag copy() {
        if (TagTypes.getType(this.type).isValue()) {
            return new ListTag(new ArrayList<>(this.list), this.type);
        } else {
            var copy = new ArrayList<Tag>(this.list.size());
            this.list.forEach(t -> copy.add(t.copy()));
            return new ListTag(copy, this.type);
        }
    }
}
