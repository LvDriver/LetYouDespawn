package com.lvdriver.letyoudespawn;

import com.lvdriver.letyoudespawn.config.FzzyIntegration;

/**
 * Indirection layer so {@link LetYouDespawn} never links against Fzzy Config types directly.
 * The JVM only loads this class when {@code fzzy_config} is actually installed.
 */
final class FzzyBridge {
   private FzzyBridge() {
   }

   static void init(boolean fzzyFileExisted) {
      FzzyIntegration.init(fzzyFileExisted);
   }

   static void pushIfPresent() {
      FzzyIntegration.pushIfPresent();
   }
}
