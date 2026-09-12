package io.github.nutant.leanobject.mixin.aekey;

import appeng.api.stacks.AEFluidKey;
import appeng.core.AELog;
import io.github.nutant.leanobject.LeanObject;
import io.github.nutant.leanobject.compat.ae2.IAEFluid;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = AEFluidKey.class, priority = 100000)
public class AEFluidKeyMixin {

    @Shadow(remap = false)
    @Final
    private Fluid fluid;

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public static AEFluidKey of(Fluid fluid, @Nullable CompoundTag tag) {
        var ae = ((IAEFluid) fluid);
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
    public static AEFluidKey of(Fluid fluid) {
        var ae = ((IAEFluid) fluid);
        return ae.lo$getAEKey();
    }

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public AEFluidKey dropSecondary() {
        var ae = ((IAEFluid) this.fluid);
        return ae.lo$getAEKey();
    }

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public static AEFluidKey fromTag(CompoundTag tag) {
        try {
            var ae = (IAEFluid) BuiltInRegistries.FLUID.getOptional(new ResourceLocation(tag.getString("id"))).orElseThrow(() -> new IllegalArgumentException("Unknown fluid id."));
            var extraTag = tag.contains("tag") ? tag.getCompound("tag") : null;
            if (extraTag == null || extraTag.isEmpty()) {
                return ae.lo$getAEKey();
            } else {
                var cache = ae.lo$getTagAEKeyCache();
                return cache.getCache(extraTag, cache.createFunction());
            }
        } catch (Exception e) {
            AELog.debug("Tried to load an invalid fluid key from NBT: %s", tag, e);
            return null;
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public static AEFluidKey fromPacket(FriendlyByteBuf data) {
        var ae = (IAEFluid) BuiltInRegistries.FLUID.byId(data.readVarInt());
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
