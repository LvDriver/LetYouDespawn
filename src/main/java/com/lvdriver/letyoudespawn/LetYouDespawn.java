package com.lvdriver.letyoudespawn;

import com.lvdriver.letyoudespawn.config.ConfigManager;
import com.lvdriver.letyoudespawn.config.LetYouDespawnConfig;
import java.io.File;
import net.neoforged.fml.ModList;

public final class LetYouDespawn {
   public static final String MOD_ID = Constants.MOD_ID;
   /** Fallback / mirror file. Always written, so the settings survive a missing Fzzy Config. */
   public static final File CONFIG_FILE = new File("config/letyoudespawn.json");
   /** Where Fzzy Config keeps its TOML when it is installed. */
   public static final File FZZY_FILE = new File("config/letyoudespawn/config.toml");
   public static final boolean FZZY_PRESENT = ModList.get().isLoaded("fzzy_config");

   private LetYouDespawn() {
   }

   public static void init() {
      boolean fzzyFileExisted = FZZY_FILE.isFile();
      Constants.config = LetYouDespawnConfig.load();
      // Always produce the editable fallback file.
      Constants.config.save();

      if (FZZY_PRESENT) {
         try {
            FzzyBridge.init(fzzyFileExisted);
         } catch (Throwable throwable) {
            Constants.LOG.error("Fzzy Config integration failed, falling back to config/letyoudespawn.json", throwable);
         }
      }
   }

   /** Pushes the current settings into Fzzy Config, if it is installed. */
   public static void syncToFzzy() {
      if (FZZY_PRESENT) {
         try {
            FzzyBridge.pushIfPresent();
         } catch (Throwable throwable) {
            Constants.LOG.error("Failed to sync settings to Fzzy Config", throwable);
         }
      }
   }

   /** Re-reads {@code config/letyoudespawn.json} (or the Fzzy TOML) and applies it live. */
   public static boolean reloadConfig() {
      LetYouDespawnConfig fresh = ConfigManager.loadFromFile(CONFIG_FILE, LetYouDespawnConfig.class);
      if (fresh == null) {
         return false;
      }
      fresh.refresh();
      Constants.config.replaceWith(
         fresh.getMobNames(),
         fresh.getPersistenceEnablers(),
         fresh.isStandardSpawnerDespawnEnabled(),
         fresh.isTrialSpawnerDespawnEnabled()
      );
      Constants.config.save();
      syncToFzzy();
      return true;
   }
}
