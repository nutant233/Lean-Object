package io.github.nutant.leanobject.mixin.ingredient;

import io.github.nutant.leanobject.FastStream;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.stream.Stream;

@Mixin(value = StrictNBTIngredient.class, priority = 100000)
public class StrictNBTIngredientMixin {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;of(Ljava/lang/Object;)Ljava/util/stream/Stream;"), remap = false)
    private static Stream<? extends Ingredient.Value> of(Object o) {
        return FastStream.create((Ingredient.Value) o);
    }
}
