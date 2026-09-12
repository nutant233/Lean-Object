package io.github.nutant.leanobject.mixin.ingredient;


import io.github.nutant.leanobject.FastStream;
import io.github.nutant.leanobject.ingredient.IIngredientObject;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;

@Mixin(Item.class)
public abstract class ItemMixin implements IIngredientObject {


    @Unique
    private Ingredient leanObject$ingredient;


    @Override
    public @NotNull Ingredient leanObject$getIngredient() {
        if (leanObject$ingredient == null) {
            leanObject$ingredient = new Ingredient(FastStream.create(new Ingredient.ItemValue(new ItemStack((Item) (Object) this))));
        }
        return leanObject$ingredient;
    }


}
