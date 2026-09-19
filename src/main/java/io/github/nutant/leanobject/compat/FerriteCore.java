package io.github.nutant.leanobject.compat;

import malte0811.ferritecore.mixin.config.FerriteConfig;

/**
 * FerriteCore's {@code modelResourceLocations} option caches the very same strings that
 * {@code ModelResourceLocation} builds, at the very same call site this mod redirects. Two redirects on
 * one instruction cannot both inject, so whenever this mod owns that path FerriteCore's own handling is
 * switched off.
 *
 * <p>Like AE2, FerriteCore is not a declared dependency: this class is only referenced when the mod list
 * says FerriteCore is present, so it is never loaded otherwise.
 */
public final class FerriteCore {

    public static void setMrl() {
        FerriteConfig.MRL_CACHE.set(a -> false);
    }

    private FerriteCore() {
    }
}
