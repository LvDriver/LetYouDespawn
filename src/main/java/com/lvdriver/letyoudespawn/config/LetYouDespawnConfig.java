package com.lvdriver.letyoudespawn.config;

import com.lvdriver.letyoudespawn.Constants;
import com.lvdriver.letyoudespawn.LetYouDespawn;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

/**
 * Source of truth for the despawn rules.
 * <p>Stored as {@code config/letyoudespawn.json}; when Fzzy Config is installed its TOML file is
 * authoritative and this JSON is kept as a mirror (see {@code FzzyBridge}).
 */
public class LetYouDespawnConfig {
   private List<String> mobNames = new ArrayList<>(List.of("corpse:corpse"));
   private List<String> persistenceEnablers = new ArrayList<>();
   private boolean enableStandardSpawnerDespawn = true;
   private boolean enableTrialSpawnerDespawn = true;

   // Not persisted: rebuilt from the id lists on every load.
   private transient volatile Set<ResourceLocation> mobIds = Set.of();
   private transient volatile Set<ResourceLocation> enablerIds = Set.of();

   public static LetYouDespawnConfig load() {
      LetYouDespawnConfig config = ConfigManager.load(LetYouDespawn.CONFIG_FILE, LetYouDespawnConfig.class);
      if (config == null) {
         config = new LetYouDespawnConfig();
      }
      config.refresh();
      return config;
   }

   public void refresh() {
      this.mobIds = parse(this.mobNames);
      this.enablerIds = parse(this.persistenceEnablers);
   }

   private static Set<ResourceLocation> parse(List<String> ids) {
      Set<ResourceLocation> out = new HashSet<>();
      if (ids == null) {
         return Set.of();
      }
      for (String id : ids) {
         ResourceLocation parsed = id == null ? null : ResourceLocation.tryParse(id.trim());
         if (parsed != null) {
            out.add(parsed);
         } else {
            Constants.LOG.warn("Ignoring invalid id '{}' in the LetYouDespawn config", id);
         }
      }
      return Set.copyOf(out);
   }

   public void save() {
      ConfigManager.save(LetYouDespawn.CONFIG_FILE, this);
   }

   /** Replaces the whole content in place (used when Fzzy Config hands us new values). */
   public void replaceWith(List<String> mobs, List<String> enablers, boolean standardSpawner, boolean trialSpawner) {
      this.mobNames = new ArrayList<>(mobs);
      this.persistenceEnablers = new ArrayList<>(enablers);
      this.enableStandardSpawnerDespawn = standardSpawner;
      this.enableTrialSpawnerDespawn = trialSpawner;
      this.refresh();
   }

   public boolean isListedMob(ResourceLocation entityId) {
      return this.mobIds.contains(entityId);
   }

   public Set<ResourceLocation> getPersistenceEnablerIds() {
      return this.enablerIds;
   }

   public List<String> getMobNames() {
      return this.mobNames;
   }

   public List<String> getPersistenceEnablers() {
      return this.persistenceEnablers;
   }

   public boolean addMobName(String mobName) {
      return this.addTo(this.mobNames, mobName);
   }

   public boolean removeMobName(String mobName) {
      return this.removeFrom(this.mobNames, mobName);
   }

   public boolean addPersistenceEnabler(String id) {
      return this.addTo(this.persistenceEnablers, id);
   }

   public boolean removePersistenceEnabler(String id) {
      return this.removeFrom(this.persistenceEnablers, id);
   }

   public boolean isStandardSpawnerDespawnEnabled() {
      return this.enableStandardSpawnerDespawn;
   }

   public boolean isTrialSpawnerDespawnEnabled() {
      return this.enableTrialSpawnerDespawn;
   }

   public void setStandardSpawnerDespawnEnabled(boolean value) {
      this.enableStandardSpawnerDespawn = value;
      this.save();
   }

   public void setTrialSpawnerDespawnEnabled(boolean value) {
      this.enableTrialSpawnerDespawn = value;
      this.save();
   }

   private boolean addTo(List<String> target, String value) {
      Set<String> values = new LinkedHashSet<>(target);
      if (!values.add(value)) {
         return false;
      }
      target.clear();
      target.addAll(values);
      this.refresh();
      this.save();
      return true;
   }

   private boolean removeFrom(List<String> target, String value) {
      if (!target.remove(value)) {
         return false;
      }
      this.refresh();
      this.save();
      return true;
   }
}
