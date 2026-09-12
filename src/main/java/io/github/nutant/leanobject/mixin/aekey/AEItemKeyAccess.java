package io.github.nutant.leanobject.mixin.aekey;

import appeng.api.stacks.AEItemKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AEItemKey.class)
public interface AEItemKeyAccess {

    @Invoker(value = "of", remap = false)
    static AEItemKey of(ItemLike item, @Nullable CompoundTag tag, @Nullable CompoundTag caps) {
        throw new RuntimeException("Not Implemented");
    }
}
