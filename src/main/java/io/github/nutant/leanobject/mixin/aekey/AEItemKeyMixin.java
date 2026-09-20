package io.github.nutant.leanobject.mixin.aekey;

import appeng.api.stacks.AEItemKey;
import appeng.core.AELog;
import io.github.nutant.leanobject.LeanObject;
import io.github.nutant.leanobject.compat.ae2.IAEItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = AEItemKey.class, priority = 100000)
public abstract class AEItemKeyMixin {

    @Shadow(remap = false)
    @Final
    private Item item;

    @Shadow
    public abstract ItemStack getReadOnlyStack();

    @Unique
    private int leanObject$maxDamage;

    @Redirect(method = "<init>",at = @At(value = "INVOKE", target = "Ljava/util/Objects;hash([Ljava/lang/Object;)I"))
    private int hash(Object[] values) {
        leanObject$maxDamage = -1;
        return System.identityHashCode(this);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public int getFuzzySearchMaxValue() {
        var max = leanObject$maxDamage;
        if (max == -1) {
            max = leanObject$maxDamage = getReadOnlyStack().getMaxDamage();
        }
        return max;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public static @Nullable AEItemKey of(ItemStack stack) {
        var item = stack.getItem();
        if (item == Items.AIR) return null;
        var tag = stack.getTag();
        var ae = (IAEItem) item;
        if (tag == null || tag.isEmpty()) {
            return ae.lo$getAEKey();
        } else {
            var cache = ae.lo$getTagAEKeyCache();
            return cache.getCache(tag, cache.createFunction(), LeanObject.NBT_COPY);
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public static AEItemKey of(ItemLike item) {
        var ae = (IAEItem) item.asItem();
        return ae.lo$getAEKey();
    }

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public static AEItemKey of(ItemLike item, @Nullable CompoundTag tag) {
        var ae = (IAEItem) item.asItem();
        if (tag == null || tag.isEmpty()) {
            return ae.lo$getAEKey();
        } else {
            var cache = ae.lo$getTagAEKeyCache();
            return cache.getCache(tag, cache.createFunction(), LeanObject.NBT_COPY);
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public AEItemKey dropSecondary() {
        var ae = (IAEItem) this.item;
        return ae.lo$getAEKey();
    }

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public static @Nullable AEItemKey fromTag(CompoundTag tag) {
        try {
            var ae = (IAEItem) BuiltInRegistries.ITEM.getOptional(new ResourceLocation(tag.getString("id"))).orElseThrow(() -> new IllegalArgumentException("Unknown item id."));
            var extraTag = tag.contains("tag") ? tag.getCompound("tag") : null;
            if (extraTag == null || extraTag.isEmpty()) {
                return ae.lo$getAEKey();
            } else {
                var cache = ae.lo$getTagAEKeyCache();
                return cache.getCache(extraTag, cache.createFunction());
            }
        } catch (Exception e) {
            AELog.debug("Tried to load an invalid item key from NBT: %s", tag, e);
            return null;
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public static AEItemKey fromPacket(FriendlyByteBuf data) {
        var ae = (IAEItem) Item.byId(data.readVarInt());
        var tag = data.readNbt();
        if (tag == null || tag.isEmpty()) {
            return ae.lo$getAEKey();
        } else {
            var cache = ae.lo$getTagAEKeyCache();
            return cache.getCache(tag, cache.createFunction());
        }
    }


    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public boolean equals(Object o) {
        return this == o;
    }
}
