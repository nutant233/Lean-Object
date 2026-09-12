package io.github.nutant.leanobject.mixin.farmersdelight;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.ToolAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import vectorwing.farmersdelight.common.crafting.ingredient.ToolActionIngredient;

import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = ToolActionIngredient.Serializer.class, remap = false, priority = 100000)
public class ToolActionIngredientSerializerMixin {

    @Unique
    private static final ConcurrentHashMap<String, ToolActionIngredient> leanObject$CACHE = new ConcurrentHashMap<>();

    /**
     * @author
     * @reason
     */
    @Overwrite
    public ToolActionIngredient parse(JsonObject json) {
        return leanObject$CACHE.computeIfAbsent(json.get("action").getAsString(), k -> new ToolActionIngredient(ToolAction.get(k)));
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public ToolActionIngredient parse(FriendlyByteBuf buffer) {
        return leanObject$CACHE.computeIfAbsent(buffer.readUtf(), k -> new ToolActionIngredient(ToolAction.get(k)));
    }
}
