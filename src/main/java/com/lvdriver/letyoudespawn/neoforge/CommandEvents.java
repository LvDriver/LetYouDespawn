package com.lvdriver.letyoudespawn.neoforge;

import com.lvdriver.letyoudespawn.LetYouDespawn;
import com.lvdriver.letyoudespawn.command.LetYouDespawnCommands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Replaces Almanac's {@code addCommandRegistration} hook with the plain NeoForge game bus. */
@EventBusSubscriber(modid = LetYouDespawn.MOD_ID)
public final class CommandEvents {
   private CommandEvents() {
   }

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      LetYouDespawnCommands.register(event.getDispatcher());
   }
}
