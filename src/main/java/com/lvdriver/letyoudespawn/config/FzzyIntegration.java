package com.lvdriver.letyoudespawn.config;

import com.lvdriver.letyoudespawn.Constants;
import java.util.ArrayList;
import java.util.List;
import me.fzzyhmstrs.fzzy_config.api.ConfigApi;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import net.minecraft.resources.ResourceLocation;

/**
 * The only class that touches Fzzy Config types.
 * <p>Loaded exclusively when {@code fzzy_config} is present; every entry point is guarded by the
 * caller. When Fzzy is missing the whole class stays unloaded and {@code config/letyoudespawn.json}
 * is authoritative.
 */
public final class FzzyIntegration {
   private static DespawnFzzyConfig config;
   private static boolean syncing;

   private FzzyIntegration() {
   }

   public static void init(boolean fzzyFileExisted) {
      config = ConfigApi.registerAndLoadConfig(DespawnFzzyConfig::new, RegisterType.BOTH);
      if (config == null) {
         Constants.LOG.warn("Fzzy Config did not return a config instance; using config/letyoudespawn.json");
         return;
      }

      if (fzzyFileExisted) {
         pull();
      } else {
         // First run with Fzzy: keep whatever the fallback JSON already had.
         push();
         pull();
      }
      Constants.LOG.info("Let You Despawn is using Fzzy Config (config/letyoudespawn/config.toml)");
   }

   /** TOML -> core. Runs after the GUI saved. */
   public static void pull() {
      DespawnFzzyConfig cfg = config;
      if (cfg == null || syncing) {
         return;
      }
      syncing = true;
      try {
         Constants.config.replaceWith(
            toIds(cfg.despawning.mobNames.get()),
            toIds(cfg.despawning.persistenceEnablers.get()),
            cfg.despawning.enableStandardSpawnerDespawn.get(),
            cfg.despawning.enableTrialSpawnerDespawn.get()
         );
         Constants.config.save();
      } finally {
         syncing = false;
      }
   }

   /** Core -> TOML. Called after a command changed the settings. */
   public static void push() {
      DespawnFzzyConfig cfg = config;
      if (cfg == null || syncing) {
         return;
      }
      syncing = true;
      try {
         cfg.despawning.mobNames.accept(toLocations(Constants.config.getMobNames()));
         cfg.despawning.persistenceEnablers.accept(toLocations(Constants.config.getPersistenceEnablers()));
         cfg.despawning.enableStandardSpawnerDespawn.accept(Constants.config.isStandardSpawnerDespawnEnabled());
         cfg.despawning.enableTrialSpawnerDespawn.accept(Constants.config.isTrialSpawnerDespawnEnabled());
         ConfigApi.save(cfg);
      } finally {
         syncing = false;
      }
   }

   public static void pushIfPresent() {
      if (config != null) {
         push();
      }
   }

   public static boolean isActive() {
      return config != null;
   }

   private static List<String> toIds(List<? extends ResourceLocation> values) {
      List<String> out = new ArrayList<>();
      for (ResourceLocation id : values) {
         if (id != null) {
            out.add(id.toString());
         }
      }
      return out;
   }

   private static List<ResourceLocation> toLocations(List<String> ids) {
      List<ResourceLocation> out = new ArrayList<>();
      for (String id : ids) {
         ResourceLocation parsed = ResourceLocation.tryParse(id);
         if (parsed != null) {
            out.add(parsed);
         }
      }
      return out;
   }
}
