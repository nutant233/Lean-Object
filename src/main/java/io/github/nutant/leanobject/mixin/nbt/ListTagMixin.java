package io.github.nutant.leanobject.mixin.nbt;

import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = ListTag.class, priority = 100000)
public class ListTagMixin {

    @Shadow
    private byte type;

    @Shadow
    @Final
    private List<Tag> list;

    @ModifyArg(method = "<init>()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/ListTag;<init>(Ljava/util/List;B)V", ordinal = 0))
    private static List<Tag> useFasterCollection(List<Tag> list) {
        return new ObjectArrayList<>();
    }

    @Redirect(method = "<init>()V", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;", remap = false))
    private static ArrayList removeOldListAlloc() {
        return null;
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite
    public ListTag copy() {
        if (TagTypes.getType(this.type).isValue()) {
            return new ListTag(new ObjectArrayList<>(this.list), this.type);
        } else {
            var copy = new ObjectArrayList<Tag>(this.list.size());
            this.list.forEach(t -> copy.add(t.copy()));
            return new ListTag(copy, this.type);
        }
    }

    @Mixin(targets = "net.minecraft.nbt.ListTag$1")
    static class Type {

        @ModifyVariable(method = "load(Ljava/io/DataInput;ILnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/ListTag;", at = @At(value = "INVOKE_ASSIGN", target = "Lcom/google/common/collect/Lists;newArrayListWithCapacity(I)Ljava/util/ArrayList;", remap = false))
        private List<Tag> useFasterCollection(List<Tag> list, @Local(ordinal = 1) int i) {
            return new ObjectArrayList<>(i);
        }

        @Redirect(method = "load(Ljava/io/DataInput;ILnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/ListTag;", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayListWithCapacity(I)Ljava/util/ArrayList;", remap = false))
        private ArrayList removeOldListAlloc(int initialArraySize) {
            return null;
        }
    }
}
