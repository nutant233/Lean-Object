package io.github.nutant.leanobject.mixin.ingredient;


import io.github.nutant.leanobject.FastStream;
import io.github.nutant.leanobject.ingredient.IIngredientObject;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TagKey.class)
public class TagKeyMixin implements IIngredientObject {

    @Unique
    private Ingredient leanObject$ingredient;

    @Override
    public @NotNull Ingredient leanObject$getIngredient() {
        if (leanObject$ingredient == null) {
            leanObject$ingredient = new Ingredient(FastStream.create((new Ingredient.TagValue((TagKey) (Object) this))));
        }
        return leanObject$ingredient;
    }
}
