package io.github.nutant.leanobject;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.File;
import java.util.*;

public final class Config implements IMixinConfigPlugin {

    public static final String MODID = "leanobject";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static final int CONFIG_VERSION = 3;

    /** Mixin sub-package holding one directory per feature; the directory name IS the feature key. */
    private static final String MIXIN_SUBPACKAGE = "io.github.nutant.leanobject.mixin.";

    private static final File configFile;

    private static final Map<String, FeaturePackage> FEATURE_PACKAGES = new LinkedHashMap<>();

    /** Maps a mixin sub-package directory name to its feature key (they differ only in case). */
    private static final Map<String, String> DIR_FEATURE = new HashMap<>();

    public static boolean enabled;
    public static boolean loggingInGC;

    private static final Map<String, FeatureConfig> featureConfigs = new HashMap<>();

    static {
        registerFeaturePackage(
                "resourceKey", true,
                "ResourceKey Deduplication",
                "Replaces vanilla ResourceKey interning to cut allocations and speed up creation"
        );

        registerFeaturePackage(
                "tagKey", true,
                "TagKey Deduplication & Identity Mode",
                "Faster TagKey creation via custom interning; equals/hashCode use identity semantics " +
                        "for cheaper HashMap lookups (items, fluids, entities, worlds, etc.)"
        );

        registerFeaturePackage(
                "resourceLocation", true,
                "ResourceLocation Deduplication & Identity Mode",
                "One instance per namespace:path, with equals/hashCode reduced to identity. Saves both " +
                        "memory and the string comparisons every registry lookup would otherwise pay, at " +
                        "the cost of a cache probe on each creation."
        );

        registerFeaturePackage(
                "nbt", false,
                "Fast NBT Collection",
                "Replaces the map and list backing CompoundTag and ListTag with leaner containers, " +
                        "rewritten at the allocation site so the vanilla ones are never created."
        );

        registerFeaturePackage(
                "ingredient", false,
                "Ingredient Deduplication",
                "Shares one Ingredient per single-item or per-tag use, so its lazily built itemStacks " +
                        "array and stackingIds list exist once instead of once per recipe, and the " +
                        "construction itself skips re-building the value list."
        );

        registerFeaturePackage(
                "model", false,
                "Simplify Model Objects",
                "Moves the model bakery's lookup maps and the baked models' face and selector lists " +
                        "onto more compact containers. Client side."
        );

        registerFeaturePackage(
                "aeKey", false, "ae2",
                "AEKey Deduplication & Identity Mode",
                "One AE2 storage key per logical item or fluid value, with equals/hashCode reduced to " +
                        "identity, so a large ME network stops allocating and hashing a key per lookup."
        );

        configFile = new File(FMLPaths.GAMEDIR.get().toFile(), "config/leanobject.toml");

        for (var feature : FEATURE_PACKAGES.values()) {
            featureConfigs.put(feature.name, new FeatureConfig(feature.name, true, feature.dependency == null || ModList.get().isLoaded(feature.dependency), false));
        }

        loadAllConfigs();
        writeConfig();
        logConfigSummary();
    }

    private static void registerFeaturePackage(String name, String displayName, String description) {
        registerFeaturePackage(name, false, null, displayName, description);
    }

    private static void registerFeaturePackage(String name, boolean hasSafeMode, String displayName, String description) {
        registerFeaturePackage(name, hasSafeMode, null, displayName, description);
    }

    private static void registerFeaturePackage(String name, boolean hasSafeMode, String dependency, String displayName, String description) {
        FEATURE_PACKAGES.put(name, new FeaturePackage(name, hasSafeMode, dependency, displayName, description));
        DIR_FEATURE.put(name.toLowerCase(Locale.ROOT), name);
    }

    private static void loadAllConfigs() {
        if (!configFile.exists()) {
            enabled = true;
            loggingInGC = true;
            return;
        }

        try (var config = CommentedFileConfig.builder(configFile).build()) {
            config.load();

            enabled = config.getOrElse("enabled", true);
            loggingInGC = config.getOrElse("loggingInGC", true);

            int fileVersion = config.getOrElse("configVersion", 1);

            var features = config.get("features");
            if (features == null) return;
            for (var featureName : FEATURE_PACKAGES.keySet()) {
                CommentedConfig section = config.get("features." + featureName);
                if (section == null) {
                    continue;
                }
                var defaultConfigs = featureConfigs.get(featureName);
                boolean featureEnabled = section.getOrElse("enabled", defaultConfigs.enabled);
                boolean safeMode = section.getOrElse("safeMode", defaultConfigs.safeMode);
                featureConfigs.put(featureName, new FeatureConfig(featureName, featureEnabled, defaultConfigs.dependencyEnabled, safeMode));
            }

            LOGGER.info("Loaded config from {} (version {})", configFile, fileVersion);
        } catch (Exception e) {
            LOGGER.warn("Failed to read leanobject config, using defaults", e);
            enabled = true;
            loggingInGC = true;
        }
    }

    /**
     * Rewrites leanobject.toml from scratch so obsolete sections are dropped.
     */
    private static void writeConfig() {
        try (var config = CommentedFileConfig.builder(configFile)
                .writingMode(WritingMode.REPLACE)
                .build()) {

            config.set("configVersion", CONFIG_VERSION);
            config.setComment("configVersion", "Internal config schema version - do not change");

            config.set("enabled", enabled);
            config.setComment("enabled", "Master switch for every LeanObject optimization");

            config.set("loggingInGC", loggingInGC);
            config.setComment("loggingInGC", "On world join, force a GC and log heap usage (MB) for memory debugging");

            for (var entry : FEATURE_PACKAGES.entrySet()) {
                var featureName = entry.getKey();
                var featurePackage = entry.getValue();
                var featureConfig = featureConfigs.get(featureName);

                var path = "features." + featureName;

                config.set(path + ".enabled", featureConfig.enabled);
                if (featurePackage.hasSafeMode) config.set(path + ".safeMode", featureConfig.safeMode);

                config.setComment(path, featurePackage.displayName);
                config.setComment(path + ".enabled", featurePackage.description);

                if (featurePackage.hasSafeMode) config.setComment(path + ".safeMode",
                        """
                                Injection strategy:
                                  false = @Overwrite - faster, fully replaces the target method
                                  true  = @Inject    - more compatible, wraps the original method
                                Enable only when another mod conflicts with the overwrite path""");
            }

            config.save();
        } catch (Exception e) {
            LOGGER.warn("Failed to write leanobject config", e);
        }
    }

    private static void logConfigSummary() {
        LOGGER.info("=== LeanObject Configuration ===");
        LOGGER.info("Global enabled: {}", enabled);
        LOGGER.info("Logging in GC: {}", loggingInGC);

        for (var entry : featureConfigs.entrySet()) {
            var config = entry.getValue();
            LOGGER.info("  {}: enabled={}, safeMode={} ({})",
                    entry.getKey(), config.enabled, config.safeMode,
                    config.safeMode ? "@Inject" : "@Overwrite");
        }
        LOGGER.info("================================");
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!enabled) return false;

        if (!mixinClassName.startsWith(MIXIN_SUBPACKAGE)) {
            LOGGER.warn("Mixin outside '{}', skipping: {}", MIXIN_SUBPACKAGE, mixinClassName);
            return false;
        }

        // Directory name directly below the mixin package identifies the feature.
        // e.g. ...mixin.tagkey.TagKeyMixin -> "tagkey" -> feature key "tagKey"
        int start = MIXIN_SUBPACKAGE.length();
        int end = mixinClassName.indexOf('.', start);
        var dir = end < 0 ? mixinClassName.substring(start) : mixinClassName.substring(start, end);
        var featureName = DIR_FEATURE.getOrDefault(dir, dir);

        var feature = FEATURE_PACKAGES.get(featureName);
        if (feature == null) {
            // Not a configurable feature: declare-only entries apply unconditionally.
            LOGGER.info("ApplyMixin (ungated, feature '{}' not registered): {}", featureName, mixinClassName);
            return true;
        }

        var config = featureConfigs.get(featureName);
        if (config == null || !config.enabled) return false;

        // Hard dependency missing (e.g. AE2): the target classes do not exist, so applying these
        // mixins would blow up with NoClassDefFoundError instead of being skipped.
        if (!config.dependencyEnabled) return false;

        // Features without a *.safe.* variant are always the @Overwrite form: safeMode is
        // meaningless for them, so never let it silently gate the mixins away.
        if (feature.hasSafeMode && config.safeMode != mixinClassName.contains(".safe.")) {
            return false;
        }

        LOGGER.info("ApplyMixin: {}", mixinClassName);
        return true;
    }

    @Override
    public void onLoad(String mixinPackage) {
        LOGGER.info("LeanObject Mixin Plugin Loaded");
        LOGGER.info("Mixin package: {}", mixinPackage);
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    private record FeaturePackage(String name, boolean hasSafeMode, String dependency, String displayName,
                                  String description) {
    }

    private record FeatureConfig(String name, boolean enabled, boolean dependencyEnabled, boolean safeMode) {

    }
}
