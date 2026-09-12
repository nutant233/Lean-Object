package io.github.nutant.leanobject.mixin.ingredient;

import io.github.nutant.leanobject.FastStream;
import io.github.nutant.leanobject.ingredient.IIngredientObject;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

@Mixin(value = Ingredient.class, priority = 100000)
public abstract class IngredientMixin {

    @Inject(method = "of(Lnet/minecraft/tags/TagKey;)Lnet/minecraft/world/item/crafting/Ingredient;", at = @At("HEAD"), cancellable = true)
    private static void of(TagKey<Item> tag, CallbackInfoReturnable<Ingredient> cir) {
        cir.setReturnValue(((IIngredientObject) (Object) tag).leanObject$getIngredient());
    }

    @Inject(method = "of([Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/item/crafting/Ingredient;", at = @At("HEAD"), cancellable = true)
    private static void of(ItemLike[] items, CallbackInfoReturnable<Ingredient> cir) {
        if (items.length == 0) {
            cir.setReturnValue(Ingredient.EMPTY);
        } else if (items.length == 1) {
            cir.setReturnValue(((IIngredientObject) items[0].asItem()).leanObject$getIngredient());
        }
    }

    @Inject(method = "of([Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/crafting/Ingredient;", at = @At("HEAD"), cancellable = true)
    private static void of(ItemStack[] stacks, CallbackInfoReturnable<Ingredient> cir) {
        if (stacks.length == 0) {
            cir.setReturnValue(Ingredient.EMPTY);
        } else if (stacks.length == 1 && !stacks[0].hasTag() && stacks[0].getCount() == 1) {
            cir.setReturnValue(((IIngredientObject) stacks[0].getItem()).leanObject$getIngredient());
        }
    }

    @Inject(method = "fromValues", at = @At("HEAD"), cancellable = true)
    private static void fromValues(Stream<? extends Ingredient.Value> stream, CallbackInfoReturnable<Ingredient> cir) {
        var values = stream.toArray(Ingredient.Value[]::new);
        if (values.length == 0) {
            cir.setReturnValue(Ingredient.EMPTY);
            return;
        } else if (values.length == 1) {
            if (values[0] instanceof Ingredient.ItemValue itemValue && !itemValue.item.hasTag() && itemValue.item.getCount() == 1) {
                cir.setReturnValue(((IIngredientObject) itemValue.item.getItem()).leanObject$getIngredient());
                return;
            } else if (values[0] instanceof Ingredient.TagValue tagValue) {
                cir.setReturnValue(((IIngredientObject) (Object) tagValue.tag).leanObject$getIngredient());
                return;
            }
        }
        cir.setReturnValue(new Ingredient(FastStream.create(values)));
    }
}
