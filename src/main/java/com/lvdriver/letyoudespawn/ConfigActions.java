package com.lvdriver.letyoudespawn;

import com.lvdriver.letyoudespawn.client.ClientHooks;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * Entry point for the config-screen button. Kept free of client classes so the dedicated server
 * never tries to load a Screen.
 */
public final class ConfigActions {
   private ConfigActions() {
   }

   public static void openPicker() {
      if (FMLEnvironment.dist.isClient()) {
         ClientHooks.openPicker();
      }
   }
}
