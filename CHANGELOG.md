# Changelog

## 1.21.1-26.9.1-neoforge — first NeoForge release

**Summary (goes in the mod listing's short-summary field — under Modrinth's 256 character limit):**

> Lean Object deduplicates Minecraft's hottest lookup objects — ResourceLocation, TagKey, ResourceKey,
> Ingredients and AE2 storage keys — so comparing them becomes a pointer compare instead of a string
> compare.

### What it is

Minecraft and its mods rebuild the same immutable value objects over and over: a `ResourceLocation` for
an id that already exists, a `TagKey` for a tag that is already loaded, an AE2 storage key for an item
the network has seen a thousand times. Lean Object routes those constructions through weak caches so one
instance serves every copy, and then reduces `equals` to `o == this` and `hashCode` to
`System.identityHashCode` — which is what makes the lookups that dominate registries, tag checks and
recipe matching cheaper. Less memory is a side effect, not the goal.

This is a mixin-based optimization mod. It replaces vanilla methods, so read the compatibility notes
before dropping it into a large pack.

### Features

All are on by default and can be switched off individually in `config/leanobject.toml`, which is written
on first launch.

| Feature | What it does | Requires |
|---|---|---|
| `resourceKey` | Replaces vanilla's `ResourceKey` interning map, so keys are built with one cache probe | — |
| `tagKey` | Deduplicates `TagKey` creation and switches `equals`/`hashCode` to identity | — |
| `resourceLocation` | One instance per `namespace:path`, with `equals`/`hashCode` reduced to identity | — |
| `ingredient` | Single-entry `Ingredient` instances are shared instead of rebuilt per recipe | — |
| `model` | Leaner containers for the model bakery and baked block/item models (client side) | — |
| `nbt` | Replaces the maps and lists inside `CompoundTag`, `ListTag` and `NbtOps` record building | — |
| `aeKey` | Deduplicates AE2 `AEItemKey`/`AEFluidKey`; the hash AE2 caches in its constructor becomes the identity hash | AE2 |

`resourceKey`, `tagKey` and `resourceLocation` also ship a non-destructive `.safe.` variant that wraps
the original methods instead of replacing them (`safeMode = true`). For `resourceLocation` that variant
also keeps value comparison, which is the conservative combination.

### Optional integrations

- **Applied Energistics 2** — 19.2.17 or newer, for `aeKey`.
- **FerriteCore** — 7.0.2 or newer. FerriteCore's `ModelResourceLocation` cache is switched off while
  `resourceLocation` is enabled, because both replace the same call and two replacements cannot both
  inject.

Both are declared as optional dependencies, and every feature whose mod is absent is skipped at
mixin-application time.

### Requirements

- Minecraft **1.21.1**
- NeoForge **21.1.169** or newer (developed against 21.1.234)
- Java **21**

### Differences from the 1.20.1 line

- **No `farmersDelight` feature on 1.21.1.** The 1.20.1 line caches `ToolActionIngredient` parsed from
  recipe JSON and packets. Farmer's Delight has no 1.21.1 build this port could compile against, so the
  feature is absent rather than broken: there is no config entry for it, and recipes are parsed exactly
  as they are without the mod.
- **`resourceLocation` works differently.** 1.20.1 interned the namespace/path strings and kept value
  comparison; 1.21.1 keeps one instance per `namespace:path` and reduces `equals`/`hashCode` to identity,
  which is where the speed comes from. Nothing in Minecraft or NeoForge builds a `ResourceLocation`
  through its constructor, so this only concerns mods that reach that constructor through their own
  access transformer — and the `.safe.` variant avoids it entirely.
- **`resourceKey` no longer changes comparison.** `ResourceKey` already compares by identity in 1.21.1,
  so the feature only replaces the interning map and speeds up creation.
- **`aeKey` follows the component system.** Item NBT is gone, so the caches are keyed by
  `DataComponentPatch`, and every factory — `of`, the packet reader, and the codec factory behind
  `fromTag` and the JSON codec — goes through them.

### Verified

Client: 22 applied mixins, no injection conflicts. Dedicated server: 17 applied mixins (the difference is
the client-only `model` and `ModelResourceLocation` mixins), reaching `Done` cleanly.
