package com.lvdriver.letyoudespawn.config;

import com.lvdriver.letyoudespawn.ConfigActions;
import com.lvdriver.letyoudespawn.Constants;
import java.util.ArrayList;
import java.util.List;
import me.fzzyhmstrs.fzzy_config.annotations.ClientModifiable;
import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigAction;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.event.api.ServerUpdateContext;
import me.fzzyhmstrs.fzzy_config.validation.collection.ValidatedList;
import me.fzzyhmstrs.fzzy_config.validation.minecraft.ValidatedIdentifier;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Fzzy Config view of the settings, stored at {@code config/letyoudespawn/config.toml}.
 * Only loaded when {@code fzzy_config} is installed.
 * <p>The two lists stay editable by hand, and the single button below them opens a searchable
 * registry browser so ids never have to be typed.
 */
@ClientModifiable
public class DespawnFzzyConfig extends Config {
   public Despawning despawning = new Despawning();

   public DespawnFzzyConfig() {
      super(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "config"));
   }

   public static class Despawning extends ConfigSection {
      @Comment("Mobs that still become persistent when they pick up an item, as before LetYouDespawn (entity ids, e.g. corpse:corpse)")
      public ValidatedList<ResourceLocation> mobNames = new ValidatedList<>(
         List.of(ResourceLocation.fromNamespaceAndPath("corpse", "corpse")),
         ValidatedIdentifier.Companion.ofRegistry(BuiltInRegistries.ENTITY_TYPE)
      );

      @Comment("Items that keep a mob from despawning while it holds or wears one it picked up (item ids)")
      public ValidatedList<ResourceLocation> persistenceEnablers = new ValidatedList<>(
         new ArrayList<>(),
         ValidatedIdentifier.Companion.ofRegistry(BuiltInRegistries.ITEM)
      );

      public ConfigAction pick = new ConfigAction.Builder()
         .title(Component.translatable("letyoudespawn.config.despawning.pickOpen"))
         .desc(Component.translatable("letyoudespawn.config.despawning.pickOpen.desc"))
         .build(ConfigActions::openPicker);

      @Comment("Let mobs from monster spawners despawn even when something tries to make them persistent")
      public ValidatedBoolean enableStandardSpawnerDespawn = new ValidatedBoolean(true);

      @Comment("Let mobs from trial spawners despawn even when something tries to make them persistent")
      public ValidatedBoolean enableTrialSpawnerDespawn = new ValidatedBoolean(true);
   }

   /** Called by Fzzy after the GUI saves, so the core config follows the TOML. */
   @Override
   public void onUpdateClient() {
      FzzyIntegration.pull();
   }

   @Override
   public void onUpdateServer(ServerUpdateContext context) {
      FzzyIntegration.pull();
   }
}
