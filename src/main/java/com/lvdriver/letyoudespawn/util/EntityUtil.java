package com.lvdriver.letyoudespawn.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class EntityUtil {
   private EntityUtil() {
   }

   /** Drops every piece of equipment that the mob picked up itself, unless enchanted to never drop. */
   public static void dropEquipmentOnDiscard(LivingEntity entity) {
      if (!(entity.level() instanceof ServerLevel) || !(entity instanceof Mob mob)) {
         return;
      }
      if (!EntityEquipmentTracker.hasAnyPickedEquipment(mob)) {
         return;
      }

      for (EquipmentSlot slot : EntityEquipmentTracker.SLOTS) {
         if (!EntityEquipmentTracker.isSlotPicked(mob, slot)) {
            continue;
         }
         ItemStack stack = entity.getItemBySlot(slot);
         if (!stack.isEmpty() && !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
            entity.spawnAtLocation(stack);
            entity.setItemSlot(slot, ItemStack.EMPTY);
         }
         EntityEquipmentTracker.clearSlot(mob, slot);
      }
   }
}
