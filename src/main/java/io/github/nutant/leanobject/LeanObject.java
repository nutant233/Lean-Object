package io.github.nutant.leanobject;

import com.gto.fastcollection.cache.WeakHashInterner;
import com.gto.fastcollection.cache.WeakValueHashCache;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.Locale;
import java.util.function.UnaryOperator;

@Mod(Config.MODID)
public final class LeanObject {

    public static final UnaryOperator<CompoundTag> NBT_COPY = CompoundTag::copy;
    public static final WeakHashInterner<String> NAMESPACE_INTERNER = new WeakHashInterner<>();
    public static final WeakHashInterner<String> PATH_INTERNER = new WeakHashInterner<>();
    public static final WeakValueHashCache<String, String> VARIANT_CACHE = new WeakValueHashCache<>(s -> s.toLowerCase(Locale.ROOT));

    public LeanObject() {
        DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> Client::new);
    }
}
