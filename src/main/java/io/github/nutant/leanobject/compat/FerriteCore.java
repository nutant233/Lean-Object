package io.github.nutant.leanobject.compat;

import malte0811.ferritecore.mixin.config.FerriteConfig;

public final class FerriteCore {

    public static void setMrl() {
        FerriteConfig.MRL_CACHE.set(a -> false);
    }
}
