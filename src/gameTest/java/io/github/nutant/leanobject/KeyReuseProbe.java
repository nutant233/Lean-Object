package io.github.nutant.leanobject;

import appeng.api.stacks.AEItemKey;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Regression harness for the {@code aeKey} feature: the same semantic probe the Thunderbolt
 * object-reuse experiment uses, so the two implementations can be compared row by row.
 *
 * <p>It lives in the {@code gameTest} source set, so it is compiled and run by
 * {@code ./gradlew runGameTestServer} but never packaged into the released jar.
 */
@GameTestHolder(Config.MODID)
@PrefixGameTestTemplate(false)
@EventBusSubscriber(modid = Config.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class KeyReuseProbe {

    private static volatile CyclicBarrier barrier;
    private static Item racing;

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.ITEM, helper -> {
            // Deliberately sizes itself from a mutable field that is *not* a component, and blocks both
            // threads on a barrier so the first construction of its key happens concurrently.
            racing = new Item(new Item.Properties()) {
                @Override
                public int getMaxStackSize(ItemStack stack) {
                    var b = barrier;
                    if (b != null) {
                        try {
                            b.await(10, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return 64;
                }
            };
            helper.register(ResourceLocation.parse("leanobject:probe_racing"), racing);
        });
    }

    private static void check(String name, boolean ok) {
        System.out.println("KEY_PROBE name=" + name + " result=" + ok);
        if (!ok) throw new AssertionError("aeKey probe failed: " + name);
    }

    @GameTest(template = "empty", timeoutTicks = 2000)
    public static void compare(GameTestHelper h) throws Exception {
        check("air_itemlike_returns_null", AEItemKey.of(Items.AIR) == null);

        // An empty component patch is not the same value as the item's default stack: a potion's
        // default instance carries potion_contents while a freshly built stack carries nothing.
        var raw = new ItemStack(Items.POTION);
        var rawKey = AEItemKey.of(raw);
        var itemKey = AEItemKey.of(Items.POTION);
        var stackKey = AEItemKey.of(Items.POTION.getDefaultInstance());
        check("potion_raw_matches_input", rawKey.matches(raw));
        check("potion_itemlike_equals_default_stack", itemKey.equals(stackKey));
        check("potion_raw_distinct_from_water", !rawKey.equals(stackKey));
        check("potion_disk_roundtrip",
                itemKey.equals(AEItemKey.fromTag(h.getLevel().registryAccess(), itemKey.toTag(h.getLevel().registryAccess()))));
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), h.getLevel().registryAccess());
        try {
            itemKey.writeToPacket(buffer);
            var decoded = AEItemKey.fromPacket(buffer);
            check("potion_packet_roundtrip", itemKey.equals(decoded));
            check("potion_storage_lookup_after_packet", Integer.valueOf(42).equals(java.util.Map.of(itemKey, 42).get(decoded)));
        } finally {
            buffer.release();
        }

        barrier = new CyclicBarrier(2);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var a = pool.submit(() -> AEItemKey.of(new ItemStack(racing)));
            var b = pool.submit(() -> AEItemKey.of(new ItemStack(racing)));
            var x = a.get(15, TimeUnit.SECONDS);
            var y = b.get(15, TimeUnit.SECONDS);
            check("concurrent_first_keys_equal", x.equals(y));
            var map = new java.util.HashMap<AEItemKey, Integer>();
            map.put(x, 1);
            map.merge(y, 1, Integer::sum);
            check("concurrent_storage_type_count", map.size() == 1);
        } finally {
            barrier = null;
            pool.shutdownNow();
        }

        // Every registered item must still produce a key that matches itself and a factory result that
        // agrees with the default-stack factory.
        var wrongInputs = new ArrayList<String>();
        var unequalFactories = new ArrayList<String>();
        for (var item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;
            var stack = new ItemStack(item);
            if (!AEItemKey.of(stack).matches(stack)) {
                wrongInputs.add(BuiltInRegistries.ITEM.getKey(item).toString());
            }
            if (!AEItemKey.of(item).equals(AEItemKey.of(item.getDefaultInstance()))) {
                unequalFactories.add(BuiltInRegistries.ITEM.getKey(item).toString());
            }
        }
        check("registry_input_mismatches", wrongInputs.isEmpty());
        check("registry_factory_inequality", unequalFactories.isEmpty());

        // The per-item fast path must still return one shared instance.
        check("plain_key_reused", AEItemKey.of(new ItemStack(Items.DIAMOND)) == AEItemKey.of(new ItemStack(Items.DIAMOND)));

        h.succeed();
    }
}
