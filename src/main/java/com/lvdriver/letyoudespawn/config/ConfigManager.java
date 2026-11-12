package com.lvdriver.letyoudespawn.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.slf4j.Logger;

/**
 * Minimal replacement for Almanac's JSON config plumbing.
 * Produces the exact same file format (Gson, pretty printing) as Almanac 1.5.2 did.
 */
public final class ConfigManager {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Map<String, Entry<?>> ENTRIES = new ConcurrentHashMap<>();

   private ConfigManager() {
   }

   public static <T> T load(File file, Class<T> type) {
      T config = loadFromFile(file, type);
      if (config != null) {
         ENTRIES.put(file.getName(), new Entry<>(config, type, file));
      }

      return config;
   }

   public static <T> T loadFromFile(File file, Class<T> type) {
      if (file.exists()) {
         try (FileReader reader = new FileReader(file)) {
            T config = GSON.fromJson(reader, type);
            if (config != null) {
               return config;
            }
         } catch (IOException exception) {
            LOGGER.error("Failed to load config {}", file, exception);
         }
      }

      return createDefault(type);
   }

   private static <T> T createDefault(Class<T> type) {
      try {
         return type.getDeclaredConstructor().newInstance();
      } catch (Exception exception) {
         LOGGER.error("Failed to create default config", exception);
         return null;
      }
   }

   public static void save(File file, Object config) {
      File parent = file.getParentFile();
      if (parent != null) {
         parent.mkdirs();
      }

      try (FileWriter writer = new FileWriter(file)) {
         GSON.toJson(config, writer);
      } catch (IOException exception) {
         LOGGER.error("Failed to save config {}", file, exception);
      }
   }

   @SuppressWarnings("unchecked")
   public static <T> void addListener(File file, Consumer<T> listener) {
      Entry<T> entry = (Entry<T>)ENTRIES.get(file.getName());
      if (entry != null) {
         entry.listeners.add(listener);
      }
   }

   /** Re-reads {@code configName} from disk and notifies listeners. */
   public static boolean reload(String configName) {
      return reloadEntry(ENTRIES.get(configName));
   }

   private static <T> boolean reloadEntry(Entry<T> entry) {
      if (entry == null) {
         return false;
      }

      T config = loadFromFile(entry.file, entry.type);
      if (config == null) {
         return false;
      }

      entry.config = config;
      for (Consumer<T> listener : entry.listeners) {
         listener.accept(config);
      }

      return true;
   }

   private static final class Entry<T> {
      private T config;
      private final Class<T> type;
      private final File file;
      private final List<Consumer<T>> listeners = new ArrayList<>();

      private Entry(T config, Class<T> type, File file) {
         this.config = config;
         this.type = type;
         this.file = file;
      }
   }
}
