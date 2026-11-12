package com.lvdriver.letyoudespawn.util;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;

/** Bitmask of equipment slots that were filled by a mob picking something up. */
public final class EntityEquipmentTracker {
   public static final EquipmentSlot[] SLOTS = EquipmentSlot.values();

   private EntityEquipmentTracker() {
   }

   private static int bit(EquipmentSlot slot) {
      return 1 << slot.ordinal();
   }

   private static DespawnTracked tracked(Mob mob) {
      return (DespawnTracked)mob;
   }

   public static void markSlotAsPicked(Mob mob, EquipmentSlot slot) {
      tracked(mob).letyoudespawn$setPickedSlots(tracked(mob).letyoudespawn$getPickedSlots() | bit(slot));
   }

   public static boolean isSlotPicked(Mob mob, EquipmentSlot slot) {
      return (tracked(mob).letyoudespawn$getPickedSlots() & bit(slot)) != 0;
   }

   public static boolean hasAnyPickedEquipment(Mob mob) {
      return tracked(mob).letyoudespawn$getPickedSlots() != 0;
   }

   public static void clearSlot(Mob mob, EquipmentSlot slot) {
      tracked(mob).letyoudespawn$setPickedSlots(tracked(mob).letyoudespawn$getPickedSlots() & ~bit(slot));
   }
}
