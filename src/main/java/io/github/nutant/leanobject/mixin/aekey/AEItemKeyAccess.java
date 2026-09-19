package io.github.nutant.leanobject.mixin.aekey;

import appeng.api.stacks.AEItemKey;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Factory accessor for AE2's private {@code AEItemKey(ItemStack)} constructor.
 *
 * <p>{@code AEItemKey.of} is overwritten to read the per-item caches, so those caches need a way to
 * build the value they store without going through it again - this invoker is that path. It always
 * creates a new key; interning is the cache's job, not the constructor's.
 */
@Mixin(AEItemKey.class)
public interface AEItemKeyAccess {

    @Invoker(value = "<init>", remap = false)
    static AEItemKey of(ItemStack stack) {
        throw new RuntimeException("Not Implemented");
    }
}
