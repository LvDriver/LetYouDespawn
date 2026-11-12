package com.lvdriver.letyoudespawn.client;

import net.minecraft.client.Minecraft;

/** Client-only indirection so no Screen class is touched on a dedicated server. */
public final class ClientHooks {
   private ClientHooks() {
   }

   public static void openPicker() {
      Minecraft minecraft = Minecraft.getInstance();
      minecraft.setScreen(new RegistryPickerScreen(minecraft.screen));
   }
}
