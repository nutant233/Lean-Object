package io.github.nutant.leanobject.mixin.ingredient;

import io.github.nutant.leanobject.FastStream;
import io.github.nutant.leanobject.ingredient.IIngredientObject;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

/**
 * Deduplicates {@link Ingredient} construction for the single-entry cases.
 *
 * <p>{@code of(TagKey)}, {@code of(ItemLike...)} and {@code of(ItemStack...)} each build their
 * ingredient themselves, so all three are hooked. The fourth, {@code fromValues}, is the one that
 * matters most: the item codec and the network stream codec both end up there, so recipe JSON and
 * packets are covered as well. A one-entry ingredient becomes the shared instance cached on its
 * {@code Item} or {@code TagKey}; anything else keeps the original behaviour.
 *
 * <p>The payoff is not only the avoided allocation: because one instance is shared, the lazily built
 * {@code itemStacks} array and {@code stackingIds} list that every {@code Ingredient} carries are
 * materialised once instead of once per recipe.
 */
@Mixin(value = Ingredient.class, priority = 100000)
public abstract class IngredientMixin {

    @Inject(method = "of(Lnet/minecraft/tags/TagKey;)Lnet/minecraft/world/item/crafting/Ingredient;", at = @At("HEAD"), cancellable = true)
    private static void leanObject$ofTag(TagKey<Item> tag, CallbackInfoReturnable<Ingredient> cir) {
        cir.setReturnValue(((IIngredientObject) (Object) tag).leanObject$getIngredient());
    }

    @Inject(method = "of([Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/item/crafting/Ingredient;", at = @At("HEAD"), cancellable = true)
    private static void leanObject$ofItems(ItemLike[] items, CallbackInfoReturnable<Ingredient> cir) {
        if (items.length == 0) {
            cir.setReturnValue(Ingredient.EMPTY);
        } else if (items.length == 1) {
            cir.setReturnValue(((IIngredientObject) items[0].asItem()).leanObject$getIngredient());
        }
    }

    @Inject(method = "of([Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/crafting/Ingredient;", at = @At("HEAD"), cancellable = true)
    private static void leanObject$ofStacks(ItemStack[] stacks, CallbackInfoReturnable<Ingredient> cir) {
        if (stacks.length == 0) {
            cir.setReturnValue(Ingredient.EMPTY);
        } else if (stacks.length == 1 && leanObject$isBareStack(stacks[0])) {
            cir.setReturnValue(((IIngredientObject) stacks[0].getItem()).leanObject$getIngredient());
        }
    }

    @Inject(method = "fromValues", at = @At("HEAD"), cancellable = true)
    private static void leanObject$fromValues(Stream<? extends Ingredient.Value> stream, CallbackInfoReturnable<Ingredient> cir) {
        // Materialise once, then always set a return value - the incoming stream is never handed on,
        // so it cannot be operated upon twice.
        var values = stream.toArray(Ingredient.Value[]::new);
        if (values.length == 0) {
            cir.setReturnValue(Ingredient.EMPTY);
            return;
        } else if (values.length == 1) {
            if (values[0] instanceof Ingredient.ItemValue itemValue && leanObject$isBareStack(itemValue.item())) {
                cir.setReturnValue(((IIngredientObject) itemValue.item().getItem()).leanObject$getIngredient());
                return;
            } else if (values[0] instanceof Ingredient.TagValue tagValue) {
                cir.setReturnValue(((IIngredientObject) (Object) tagValue.tag()).leanObject$getIngredient());
                return;
            }
        }
        cir.setReturnValue(new Ingredient(FastStream.create(values)));
    }

    /**
     * A stack that carries no component patch and holds a single item, i.e. the plain
     * {@code new ItemStack(item)} form that the item's shared ingredient represents.
     */
    @Unique
    private static boolean leanObject$isBareStack(ItemStack stack) {
        return !stack.isEmpty() && stack.getCount() == 1 && stack.isComponentsPatchEmpty();
    }
}
