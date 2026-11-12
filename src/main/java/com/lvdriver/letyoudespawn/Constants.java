package com.lvdriver.letyoudespawn;

import com.lvdriver.letyoudespawn.config.LetYouDespawnConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Constants {
   public static final String MOD_ID = "letyoudespawn";
   public static final String MOD_NAME = "LetYouDespawn";
   public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
   /** Replaced at init; never null so mixins are safe during early class loading. */
   public static LetYouDespawnConfig config = new LetYouDespawnConfig();

   private Constants() {
   }
}
