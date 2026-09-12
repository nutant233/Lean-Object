package io.github.nutant.leanobject.mixin.ingredient;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import io.github.nutant.leanobject.ingredient.IIngredientObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.VanillaIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = VanillaIngredientSerializer.class, priority = 100000)
public class VanillaIngredientSerializerMixin {

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public Ingredient parse(JsonObject json) {
        var item = json.get("item");
        if (item != null) {
            Object o = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(item.getAsString()));
            if (o == null) throw new JsonSyntaxException("Unknown item '" + item + "'");
            return ((IIngredientObject) o).leanObject$getIngredient();
        }
        var tag = json.get("tag");
        if (tag != null) {
            var resourcelocation = ResourceLocation.parse(tag.getAsString());
            Object tagkey = TagKey.create(Registries.ITEM, resourcelocation);
            return ((IIngredientObject) tagkey).leanObject$getIngredient();
        } else {
            throw new JsonParseException("An ingredient entry needs either a tag or an item");
        }
    }
}
