package com.lvdriver.letyoudespawn.command;

import com.lvdriver.letyoudespawn.Constants;
import com.lvdriver.letyoudespawn.LetYouDespawn;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class LetYouDespawnCommands {
   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("letyoudespawn")
         .requires(source -> source.hasPermission(2));

      root.then(
         Commands.literal("add")
            .then(
               Commands.argument("mobName", StringArgumentType.greedyString())
                  .suggests((context, builder) -> suggestIds(BuiltInRegistries.ENTITY_TYPE, builder))
                  .executes(LetYouDespawnCommands::addMob)
            )
      );
      root.then(
         Commands.literal("remove")
            .then(
               Commands.argument("mobName", StringArgumentType.greedyString())
                  .suggests((context, builder) -> suggest(Constants.config.getMobNames(), builder))
                  .executes(LetYouDespawnCommands::removeMob)
            )
      );
      root.then(
         Commands.literal("addPersistenceEnabler")
            .then(
               Commands.argument("itemName", StringArgumentType.greedyString())
                  .suggests((context, builder) -> suggestIds(BuiltInRegistries.ITEM, builder))
                  .executes(LetYouDespawnCommands::addItem)
            )
      );
      root.then(
         Commands.literal("removePersistenceEnabler")
            .then(
               Commands.argument("itemName", StringArgumentType.greedyString())
                  .suggests((context, builder) -> suggest(Constants.config.getPersistenceEnablers(), builder))
                  .executes(LetYouDespawnCommands::removeItem)
            )
      );
      root.then(
         Commands.literal("standardSpawnerDespawn")
            .then(Commands.argument("value", BoolArgumentType.bool()).executes(LetYouDespawnCommands::setStandardSpawnerDespawn))
      );
      root.then(
         Commands.literal("trialSpawnerDespawn")
            .then(Commands.argument("value", BoolArgumentType.bool()).executes(LetYouDespawnCommands::setTrialSpawnerDespawn))
      );
      root.then(Commands.literal("reload").executes(LetYouDespawnCommands::reload));

      dispatcher.register(root);
   }

   private static CompletableFuture<Suggestions> suggestIds(Registry<?> registry, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
      for (ResourceLocation id : registry.keySet()) {
         String key = id.toString();
         if (key.startsWith(remaining) || id.getPath().startsWith(remaining)) {
            builder.suggest(key);
         }
      }
      return builder.buildFuture();
   }

   private static CompletableFuture<Suggestions> suggest(Collection<String> values, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
      values.stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(remaining)).forEach(builder::suggest);
      return builder.buildFuture();
   }

   private static boolean isKnown(CommandContext<CommandSourceStack> context, Registry<?> registry, String id, String kind) {
      ResourceLocation parsed = ResourceLocation.tryParse(id);
      if (parsed != null && registry.containsKey(parsed)) {
         return true;
      }
      context.getSource().sendFailure(Component.literal("Unknown " + kind + " '" + id + "'."));
      return false;
   }

   private static int reply(CommandContext<CommandSourceStack> context, boolean changed, String done, String unchanged, ChatFormatting doneColor) {
      if (changed) {
         context.getSource().sendSuccess(() -> Component.literal(done).withStyle(doneColor), true);
         return 1;
      }
      context.getSource().sendFailure(Component.literal(unchanged));
      return 0;
   }

   private static int addMob(CommandContext<CommandSourceStack> context) {
      String mobName = StringArgumentType.getString(context, "mobName").trim();
      if (!isKnown(context, BuiltInRegistries.ENTITY_TYPE, mobName, "mob")) {
         return 0;
      }
      boolean changed = Constants.config.addMobName(mobName);
      if (changed) {
         LetYouDespawn.syncToFzzy();
      }
      return reply(
         context,
         changed,
         "Added '" + mobName + "' to LetYouDespawn configuration.",
         "Mob '" + mobName + "' is already in the configuration.",
         ChatFormatting.AQUA
      );
   }

   private static int removeMob(CommandContext<CommandSourceStack> context) {
      String mobName = StringArgumentType.getString(context, "mobName").trim();
      boolean changed = Constants.config.removeMobName(mobName);
      if (changed) {
         LetYouDespawn.syncToFzzy();
      }
      return reply(
         context,
         changed,
         "Removed '" + mobName + "' from LetYouDespawn configuration.",
         "Mob '" + mobName + "' is not in the configuration.",
         ChatFormatting.GOLD
      );
   }

   private static int addItem(CommandContext<CommandSourceStack> context) {
      String itemName = StringArgumentType.getString(context, "itemName").trim();
      if (!isKnown(context, BuiltInRegistries.ITEM, itemName, "item")) {
         return 0;
      }
      boolean changed = Constants.config.addPersistenceEnabler(itemName);
      if (changed) {
         LetYouDespawn.syncToFzzy();
      }
      return reply(
         context,
         changed,
         "Added '" + itemName + "' to persistence enablers.",
         "Item '" + itemName + "' is already a persistence enabler.",
         ChatFormatting.AQUA
      );
   }

   private static int removeItem(CommandContext<CommandSourceStack> context) {
      String itemName = StringArgumentType.getString(context, "itemName").trim();
      boolean changed = Constants.config.removePersistenceEnabler(itemName);
      if (changed) {
         LetYouDespawn.syncToFzzy();
      }
      return reply(
         context,
         changed,
         "Removed '" + itemName + "' from persistence enablers.",
         "Item '" + itemName + "' is not a persistence enabler.",
         ChatFormatting.GOLD
      );
   }

   private static int setStandardSpawnerDespawn(CommandContext<CommandSourceStack> context) {
      boolean value = BoolArgumentType.getBool(context, "value");
      Constants.config.setStandardSpawnerDespawnEnabled(value);
      LetYouDespawn.syncToFzzy();
      context.getSource().sendSuccess(() -> Component.literal("Standard spawner despawn set to: " + value).withStyle(ChatFormatting.AQUA), true);
      return 1;
   }

   private static int setTrialSpawnerDespawn(CommandContext<CommandSourceStack> context) {
      boolean value = BoolArgumentType.getBool(context, "value");
      Constants.config.setTrialSpawnerDespawnEnabled(value);
      LetYouDespawn.syncToFzzy();
      context.getSource().sendSuccess(() -> Component.literal("Trial spawner despawn set to: " + value).withStyle(ChatFormatting.AQUA), true);
      return 1;
   }

   private static int reload(CommandContext<CommandSourceStack> context) {
      if (LetYouDespawn.reloadConfig()) {
         context.getSource().sendSuccess(() -> Component.literal("Reloaded LetYouDespawn configuration.").withStyle(ChatFormatting.AQUA), true);
         return 1;
      }
      context.getSource().sendFailure(Component.literal("Failed to reload LetYouDespawn configuration."));
      return 0;
   }
}
