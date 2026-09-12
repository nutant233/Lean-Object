# Lean Object

Performance optimizations for **Minecraft 1.20.1 / Forge**.

LeanObject deduplicates the immutable objects that Minecraft and its mods recreate over and over, so
the same logical value ends up as one shared instance instead of thousands of copies. That is worth
more than the memory it saves:

- **Hash lookups get cheaper.** Once a value exists exactly once, `equals` can be reduced to `o == this`
  and `hashCode` to `System.identityHashCode`. Keys that were compared by content — `ResourceLocation`
  strings, `TagKey`/`ResourceKey` component fields — are then matched by reference, on every lookup in
  every registry, tag check and ingredient match.
- **Fewer allocations.** Reusing an interned instance also means not building temporary objects on
  lookup paths, which cuts GC pressure and improves locality.
- **Memory footprint drops** as a side effect: the duplicate copies simply never exist.

The gain is largest where an object is used as a **key in hot maps** — which is exactly why
`ResourceLocation`, `TagKey`, `ResourceKey` and AE2 storage keys are the headline features, and why
[`aeKey`](#the-aekey-feature-matters-most-in-late-game-tech-packs) pays off in large tech packs.

> This is a **mixin-based optimization mod**. It rewrites vanilla methods. See
> [Known limitations](#known-limitations) before putting it in a large pack.

## Relationship to Fast Tag

**LeanObject supersedes [Fast Tag](https://github.com/nutant233/Fast-Tag) and is intended to be used
instead of it.** The `tagKey` and `resourceKey` features are a direct port: the same `codec` /
`hashedCodec` / `create` overloads are overwritten, backed by the same interning-map layout, and
interning keys are compared by reference in both.

The reasoning is Fast Tag's, unchanged. Vanilla already deduplicates `TagKey` on creation through
`Interners.newWeakInterner()`, so only one instance per logical tag exists. But `TagKey` is a `record`,
so its generated `hashCode`/`equals` compare contents — and every lookup elsewhere in the game pays
for that, while the interner relies on them. Swapping contents for `System.identityHashCode` and `==`
is therefore safe *because* of the interning, and makes those calls much cheaper. `ResourceKey` gets
the same treatment.

What changed is the cache backend:

| | Fast Tag | LeanObject |
|---|---|---|
| Cache | Guava `MapMaker` (`weakValues()`) | own `O2OOpenCacheHashMap` / weak caches |
| Identity-keyed maps | reflection into Guava `MapMaker.keyEquivalence` | plain identity maps, no reflection |
| Scope | only `TagKey` + `ResourceKey` | those plus 6 more features |

Running both together would put two `@Overwrite` sets on the same targets. Use one or the other.

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

`hashCode` is the smaller win. `AEItemKey` caches its hash in a field; `ResourceLocation` does not —
its `hashCode()` is `31 * namespace.hashCode() + path.hashCode()` with no cached result, so every call
redoes two `String.hashCode` lookups and the combine (`String.hashCode` itself is cached, so string
length is not the recurring cost). Identity hashing removes that per-call work and also spreads buckets
better than path strings do, which in turn means fewer `equals` calls.

So the benefit scales with how often these objects are used as map keys, which is why the headline
features are the key types. Memory is the side effect, not the goal.

Every feature is independently switchable, and injection is decided at runtime by the mixin config
plugin (`Config`, which doubles as `IMixinConfigPlugin`).

## Features

Enabled state shown is the default written to `config/leanobject.toml` on first launch.

| Feature | Default | `.safe.` variant | Requires | What it does |
|---|---|---|---|---|
| `resourceKey` | on | yes | — | Deduplicates `ResourceKey` creation; `equals` becomes identity |
| `tagKey` | on | yes | — | Deduplicates `TagKey` creation; `equals`/`hashCode` become identity |
| `resourceLocation` | on | no | — | Interns `ResourceLocation` namespace/path/variant strings |
| `ingredient` | on | no | — | Single-entry `Ingredient` instances are shared instead of rebuilt |
| `model` | on | no | — | Leaner containers for baked block/item model objects |
| `nbt` | on | no | — | Replaces the maps/lists inside `CompoundTag`, `ListTag` and `NbtOps` record building |
| `aeKey` | on | no | AE2 | Deduplicates AE2 `AEItemKey`/`AEFluidKey`; `equals` becomes identity |
| `farmersDelight` | on | no | Farmer's Delight | Caches `ToolActionIngredient` parsed from JSON and packets |

`resourceKey` and `tagKey` are the only features with a `.safe.` variant. Every other feature only
ships the `@Overwrite` form.

### The aeKey feature matters most in late-game tech packs

Official AE2 has **no interning for storage keys**. `AEItemKey`'s constructor and its three-argument
factory are both `private`, so every lookup that goes through `AEItemKey.of(...)` builds a new key
object — and a big AE2 network is nothing but keys.

### Why the construction rate is so high in a tech pack

In a modded pack, AE2 rarely works on its own storage. Storage buses, import and export buses and
interfaces connect the ME network to *other* mods' inventories — chests, barrels, machines, drawers,
pipes — and every item moved across that boundary is converted between an `ItemStack` and an AE key.
Those transfers run continuously in bulk, so key construction happens constantly, not once at setup:
a late-game base with large banks of interfaces, storage buses and crafting CPUs drives this path
per item, per bus, per tick.

### What vanilla pays per key

Allocation is not the whole cost — the constructor computes and stores the key's hash every time:

```
41: invokestatic  java/util/Objects.hash:([Ljava/lang/Object;)I
44: putfield      AEItemKey.hashCode:I
```

So each conversion costs *allocate an object + hash its components + compare by value*. This feature
collapses all three:

- Keys with no NBT become a **field read** — the shared instance is fetched from the item, with no
  allocation and no hashing at all.
- Tagged keys cost **one hash lookup keyed by the NBT copy**, and that is the whole cost. Only the
  first lookup for a given tag allocates; repeats just return the existing key.
- Because keys then exist once, `equals` can be identity, and the resulting fast reference/hash
  lookups are what a large ME network spends its time on.

That is why this is the feature worth enabling for a tech pack: it speeds up key *creation*, not just
key lookup.

### What "interning" means for you

`TagKey` and `ResourceKey` are compared by reference, so **a value that was never routed through the
cache will not compare equal to its interned twin.** This is inherited design, not something this mod
introduces — see [Relationship to Fast Tag](#relationship-to-fast-tag) for why it is safe, and
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
  `.safe.` variant** (`resourceKey`, `tagKey`):
  - `false` → `@Overwrite`: faster, fully replaces the target method.
  - `true` → `@Inject`: more compatible, wraps the original method.
  - Enable only when another mod conflicts with the overwrite path.
- **`loggingInGC`** — on world join, runs `System.gc()` and logs the heap in use. Useful for
  before/after measurement.

The file is rewritten from scratch each launch, so comments you add by hand are lost and obsolete
sections are dropped. Feature keys are camelCase (`resourceKey`, `tagKey`, `resourceLocation`,
`aeKey`, `farmersDelight`).

## Requirements

- Minecraft **1.20.1**
- Forge **47.x** (developed against 47.4.9)
- Java **17**

Optional integrations are auto-detected: **AE2** (`aeKey`) and **Farmer's Delight**
(`farmersDelight`). Features whose hard dependency is absent are skipped at mixin-application time.

## Building

```bash
./gradlew build          # jar lands in build/libs/
./gradlew compileJava    # compile only
```

## Development

The project uses the NeoForge `moddev` legacy-Forge plugin. Dev runs share the `run/` directory:

```bash
./gradlew runClient      # dev client
./gradlew runServer      # dev server (--nogui); needs run/eula.txt
./gradlew runData        # data generators -> src/generated/resources
./gradlew runGameTestServer
```

Verifying that injection is healthy: each feature logs `ApplyMixin: <class>` when it is applied. On a
clean run with the default config you should see **25** applied mixins on the client and **20** on a
dedicated server (the difference is the client-only `model` and `resourcelocation` mixins). Anything
reported as `was not found` or `maps to unregistered feature` means a mixin is not being applied.

## Project layout

```
src/main/java/io/github/nutant/leanobject/
├── Config.java              # feature registry + IMixinConfigPlugin (the injection gate)
├── LeanObject.java          # mod entrypoint; string interning caches
├── ResourceKeys.java        # ResourceKey interning caches
├── TagKeys.java             # TagKey interning caches
├── FastStream.java          # array-backed Stream used to avoid stream setup cost
├── Client.java              # optional on-join GC logging
├── compat/                  # FerriteCore hook, AE2 interfaces
├── ingredient/              # Ingredient caching interface
└── mixin/<feature>/         # one package per feature; package name == feature key
```

**The mixin package name is load-bearing.** `Config.shouldApplyMixin` derives the feature key from the
directory directly below `io.github.nutant.leanobject.mixin.`, so `mixin/tagkey/` maps to the `tagKey`
feature. A mixin placed in a directory that has no registered feature is applied **ungated**
(unconditionally).

## Known limitations

- **`TagKey`/`ResourceKey` compare by reference.** Vanilla deduplicates both on creation, then compares
  their contents (`TagKey` is a `record`, so that is compiler-generated). This mod keeps the
  deduplication but switches `equals`/`hashCode` to identity — the change Fast Tag already made, and
  safe precisely because of the deduplication. Values built by bypassing the interning entry points
  will no longer compare equal to interned ones.
- **`@Overwrite` conflicts.** Every feature except `resourceKey`/`tagKey` has no non-destructive
  fallback. If another mod mixes into the same target (`TagKey`, `ResourceKey`, `ResourceLocation`,
  `Ingredient`, `CompoundTag`, `ListTag`, `NbtOps$NbtRecordBuilder`, baked model classes, AE2 keys),
  the two overwrites fight and the last one applied wins. The `model` feature in particular is known to
  conflict on large packs.

## License

GNU Lesser General Public License v3.0 or later — see [LICENSE.txt](LICENSE.txt).
