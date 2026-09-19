# Lean Object

Performance optimizations for **Minecraft 1.21.1 / NeoForge**.

LeanObject deduplicates the immutable objects that Minecraft and its mods recreate over and over, so
the same logical value ends up as one shared instance instead of thousands of copies. That is worth
more than the memory it saves:

- **Hash lookups get cheaper.** Once a value exists exactly once, `equals` can be reduced to `o == this`
  and `hashCode` to `System.identityHashCode`. Keys that were compared by content — `ResourceLocation`
  strings, `TagKey` component fields, AE2 key stacks — are then matched by reference, on every lookup in
  every registry, tag check and ingredient match.
- **Fewer allocations.** Reusing an interned instance also means not building temporary objects on
  lookup paths, which cuts GC pressure and improves locality.
- **Memory footprint drops** as a side effect: the duplicate copies simply never exist.

The gain is largest where an object is used as a **key in hot maps** — which is exactly why
`ResourceLocation`, `TagKey` and AE2 storage keys are the headline features, and why
[`aeKey`](#the-aekey-feature-matters-most-in-late-game-tech-packs) pays off in large tech packs.

> This is a **mixin-based optimization mod**. It rewrites vanilla methods. See
> [Known limitations](#known-limitations) before putting it in a large pack.

## Relationship to Fast Tag

**LeanObject supersedes [Fast Tag](https://github.com/nutant233/Fast-Tag) and is intended to be used
instead of it.** The `tagKey` and `resourceKey` features are a direct port: the same `codec` /
`hashedCodec` / `create` overloads are replaced, backed by the same interning-map layout, and interning
keys are compared by reference in both.

The reasoning is Fast Tag's, unchanged. Vanilla already deduplicates `TagKey` on creation through
`Interners.newWeakInterner()`, so only one instance per logical tag exists. But `TagKey` is a `record`,
so its generated `hashCode`/`equals` compare contents — and every lookup elsewhere in the game pays for
that, while the interner relies on them. Swapping contents for `System.identityHashCode` and `==` is
therefore safe *because* of the interning.

What changed is the cache backend:

| | Fast Tag | LeanObject |
|---|---|---|
| Cache | Guava `MapMaker` (`weakValues()`) | own `O2OOpenCacheHashMap` / weak caches |
| Identity-keyed maps | reflection into Guava `MapMaker.keyEquivalence` | plain identity maps, no reflection |
| Scope | only `TagKey` + `ResourceKey` | those plus 5 more features |

Running both together would put two replacement sets on the same targets. Use one or the other.

---

## How it works

Two ideas, applied per feature:

1. **Interning** — when a value object is created, route it through a weak cache so that an equal value
   already in memory is reused instead of allocating a new instance.
2. **Identity semantics** — having kept only one instance per logical value, `equals` can be reduced to
   `o == this` and `hashCode` to `System.identityHashCode`, making both constant-time.

The order matters: interning is what makes the identity comparison *correct*, and the identity
comparison is where the speed comes from.

### Why identity comparison is the bigger win

Content comparison is not free, and it is paid on every single map operation — not once at creation.

The heavy part is **`equals`**. A hash code only picks the bucket; every collision inside that bucket
is then resolved by `equals`, and `equals` is where the string work happens. For vanilla `TagKey` (a
`record`) that means comparing a `ResourceKey` **and** a `ResourceLocation`. For the registries a modded
pack builds — thousands of entries whose identifiers share prefixes like `minecraft:` or `mymod:` —
content hash codes cluster tightly, so those buckets are exactly where the comparisons pile up. Reduce
`equals` to `o == this` and each of those becomes a pointer compare.

`hashCode` is the smaller win, but in 1.21.1 it is not nothing: `ResourceLocation` stores only
`namespace` and `path` and recomputes `31 * namespace.hashCode() + path.hashCode()` on every call, so
identity hashing removes that per-call work and spreads buckets better than path strings do — which in
turn means fewer `equals` calls. `AEItemKey` caches its hash in a field; see
[below](#the-aekey-feature-matters-most-in-late-game-tech-packs) for what that field now holds.

So the benefit scales with how often these objects are used as map keys, which is why the headline
features are the key types. Memory is the side effect, not the goal.

Every feature is independently switchable, and injection is decided at runtime by the mixin config
plugin (`Config`, which doubles as `IMixinConfigPlugin`).

## Features

Enabled state shown is the default written to `config/leanobject.toml` on first launch.

| Feature | Default | `.safe.` variant | Requires | What it does |
|---|---|---|---|---|
| `resourceKey` | on | yes | — | Replaces vanilla's `ResourceKey` interning map, so keys are built with one cache probe. `ResourceKey` already compares by identity in 1.21.1 — that is inherited, not changed here |
| `tagKey` | on | yes | — | Deduplicates `TagKey` creation and switches `equals`/`hashCode` to identity |
| `resourceLocation` | on | yes | — | One instance per `namespace:path`, with `equals`/`hashCode` reduced to identity — see below |
| `ingredient` | on | no | — | Single-entry `Ingredient` instances are shared instead of rebuilt |
| `model` | on | no | — | Leaner containers for baked block/item model objects (client side) |
| `nbt` | on | no | — | Replaces the maps/lists inside `CompoundTag`, `ListTag` and `NbtOps` record building |
| `aeKey` | on | no | AE2 | Deduplicates AE2 `AEItemKey`/`AEFluidKey`; `equals` becomes identity and the hash AE2 caches in its constructor becomes the identity hash |

`resourceKey`, `tagKey` and `resourceLocation` are the features with a `.safe.` variant. Every other
feature only ships the replacing form.

### A note on `resourceLocation`

`ResourceLocation` is constructed extremely often, and this feature adds a cache probe to every
construction: the namespace selects a sub-cache and the path is looked up in it. It does not add string
hashing of its own — the strings are hashed by the cache as any map key would be.

Three things pay for that probe:

- The duplicate instances disappear, along with the `namespace`/`path` strings they hold.
- `equals` becomes a pointer compare, which is the bigger win, because location comparisons happen on
  every registry and model lookup.
- `hashCode` stops recomputing `31 * namespace.hashCode() + path.hashCode()` per call.

`safeMode = true` is worth knowing about here: the safe variant only wraps the factory methods and
leaves `equals`/`hashCode` **as value comparisons**. That makes it the conservative choice — you keep
the shared instances and lose the identity comparisons.

### The aeKey feature matters most in late-game tech packs

Official AE2 has **no interning for storage keys**. `AEItemKey`'s constructor is `private` and every
public factory builds a fresh key, so every lookup that goes through `AEItemKey.of(...)` allocates — and
a big AE2 network is nothing but keys.

### Why the construction rate is so high in a tech pack

In a modded pack, AE2 rarely works on its own storage. Storage buses, import and export buses and
interfaces connect the ME network to *other* mods' inventories — chests, barrels, machines, drawers,
pipes — and every item moved across that boundary is converted between an `ItemStack` and an AE key.
Those transfers run continuously in bulk, so key construction happens constantly, not once at setup:
a late-game base with large banks of interfaces, storage buses and crafting CPUs drives this path
per item, per bus, per tick.

### What vanilla pays per key

Allocation is not the whole cost — the constructor computes and stores the key's hash every time
(1.21.1, `AEItemKey`):

```
22: putfield      stack:Lnet/minecraft/world/item/ItemStack;
27: invokestatic  ItemStack.hashItemAndComponents:(Lnet/minecraft/world/item/ItemStack;)I
31: putfield      hashCode:I
```

So each conversion costs *allocate an object + hash its components + compare by value*. This feature
collapses all three:

- Keys with no components become a **field read** — the shared instance is fetched from the item, with
  no allocation and no hashing at all.
- Keys carrying components cost **one cache probe keyed by the component patch**, and that is the whole
  cost. Only the first probe for a given patch allocates; repeats just return the existing key.
- Because keys then exist once, `equals` can be identity, and the hash the constructor stores is
  replaced by the identity hash at the point it is computed — so `hashCode()` keeps returning that one
  field, it just returns an identity hash now.
- Every other factory is routed through the same cache as well: `fromPacket`, and the codec factory
  that `fromTag` and the JSON codec both go through.

That is why this is the feature worth enabling for a tech pack: it speeds up key *creation*, not just
key lookup.

### What "interning" means for you

Interned keys are compared by reference, so **a value that was never routed through the cache will not
compare equal to its interned twin.** For `TagKey` and `ResourceLocation` that is a change this mod
makes; `ResourceKey` already behaves that way in 1.21.1. See
[Known limitations](#known-limitations) for the practical consequence.

## Configuration

`config/leanobject.toml` is (re)generated on every launch:

```toml
enabled = true                      # master switch for every feature
loggingInGC = true                  # force a GC + log heap usage on world join

[features.tagKey]
    safeMode = false
    enabled = true
```

- **`enabled` (global)** — kills all features at once.
- **`features.<name>.enabled`** — per-feature switch.
- **`features.<name>.safeMode`** — injection strategy, **only meaningful for features that ship a
  `.safe.` variant** (`resourceKey`, `tagKey`, `resourceLocation`):
  - `false` → replace the target methods: faster, and it is what gives identity comparison.
  - `true` → wrap the original methods: more compatible, and for `resourceLocation` it keeps value
    comparison. Enable when another mod conflicts with the replacing path.
- **`loggingInGC`** — on world join, runs `System.gc()` and logs the heap in use. Useful for
  before/after measurement.

The file is rewritten from scratch each launch, so comments you add by hand are lost and obsolete
sections are dropped. Feature keys are camelCase (`resourceKey`, `tagKey`, `resourceLocation`,
`ingredient`, `model`, `nbt`, `aeKey`).

## Requirements

- Minecraft **1.21.1**
- NeoForge **21.1.169** or newer (developed against 21.1.234)
- Java **21**

Optional integrations are auto-detected, and features whose mod is absent are skipped at
mixin-application time:

- **AE2** (`aeKey`) — 19.2.17 or newer
- **FerriteCore** — 7.0.2 or newer. FerriteCore's own `ModelResourceLocation` cache is switched off
  while `resourceLocation` is enabled, because both would replace the same call and two replacements
  cannot both inject.

`farmersDelight` is **not part of the 1.21.1 build** — see [Known limitations](#known-limitations).

## Building

```bash
./gradlew build          # jar lands in build/libs/
./gradlew compileJava    # compile only
```

## Development

The project uses the NeoForge `moddev` plugin. Dev runs share the `run/` directory:

```bash
./gradlew runClient      # dev client
./gradlew runServer      # dev server (--nogui); needs run/eula.txt
./gradlew runData        # data generators -> src/generated/resources
./gradlew runGameTestServer
```

Verifying that injection is healthy: each applied mixin logs `ApplyMixin: <class>`, and the plugin
prints the whole configuration before that. A clean run with the default config should show **22**
applied mixins on the client and **17** on a dedicated server — the difference is the client-only
`model` and `ModelResourceLocation` mixins.

Two lines in a run log mean the gate did **not** run, and the mixins were applied without it — check for
them after any change to the plugin:

- `Error loading companion plugin class` — `Config` failed to initialize.
- `Method overwrite conflict` / `@Redirect conflict` — both the normal and the `.safe.` variant were
  applied at once.

## Project layout

```
src/main/java/io/github/nutant/leanobject/
├── Config.java              # feature registry + IMixinConfigPlugin (the injection gate)
├── LeanObject.java          # mod entrypoint
├── ResourceLocations.java   # ResourceLocation interning caches + model variant strings
├── ResourceKeys.java        # ResourceKey interning caches
├── TagKeys.java             # TagKey interning caches
├── FastStream.java          # array-backed Stream used to avoid stream setup cost
├── Client.java              # optional on-join GC logging
├── compat/                  # FerriteCore hook, AE2 interfaces
├── ingredient/              # Ingredient sharing interface
└── mixin/<feature>/         # one package per feature; package name == feature key
```

**The mixin package name is load-bearing.** `Config.shouldApplyMixin` derives the feature key from the
directory directly below `io.github.nutant.leanobject.mixin.`, so `mixin/tagkey/` maps to the `tagKey`
feature. A mixin placed in a directory that has no registered feature is applied **ungated**
(unconditionally).

## Known limitations

- **`TagKey` and `ResourceLocation` compare by reference.** Both are interned, so this is safe for
  anything created through the game's own entry points — and in 1.21.1 nothing in Minecraft or NeoForge
  calls `new ResourceLocation(...)` directly, so every location the game builds goes through the
  interned paths. A third-party mod that accesses that constructor (via its own access transformer) and
  builds a location by hand will produce an instance that does not compare equal to the interned one.
  `safeMode = true` avoids this for `resourceLocation` by keeping value comparison.
- **`@Overwrite` / replacement conflicts.** Every feature except `resourceKey`/`tagKey` has no
  non-destructive fallback. If another mod mixes into the same target (`TagKey`, `ResourceLocation`,
  `Ingredient`, `CompoundTag`, `ListTag`, `NbtOps$NbtRecordBuilder`, baked model classes, AE2 keys),
  the two fight and the last one applied wins. The `model` feature in particular is known to conflict on
  large packs. FerriteCore is handled automatically as described above.
- **No `farmersDelight` feature on 1.21.1.** The 1.20.1 line caches `ToolActionIngredient` parsed from
  recipe JSON and packets. Farmer's Delight has no 1.21.1 build that this port could compile against, so
  that feature is simply absent here rather than broken — there is no config entry for it, and recipes
  are parsed exactly as they are without the mod.
- **AE2 support is version specific.** It targets official AE2 19.2.17 and hooks private members of
  `AEItemKey`/`AEFluidKey`. Forks or other builds can change those internals, in which case the mixins
  fail to apply loudly rather than silently doing the wrong thing.

## License

GNU Lesser General Public License v3.0 or later — see [LICENSE.txt](LICENSE.txt).
