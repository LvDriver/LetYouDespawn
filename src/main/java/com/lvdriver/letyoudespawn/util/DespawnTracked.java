package com.lvdriver.letyoudespawn.util;

import net.minecraft.core.BlockPos;

/**
 * Per-mob state implemented by {@code MobMixin}: where the mob came from and which
 * equipment slots came from a pickup (as a bitmask, like upstream did via Almanac).
 */
public interface DespawnTracked {
   boolean letyoudespawn$isFromSpawner();

   void letyoudespawn$markFromSpawner();

   boolean letyoudespawn$isFromTrialSpawner();

   void letyoudespawn$markFromTrialSpawner();

   BlockPos letyoudespawn$getTrialSpawnerPos();

   void letyoudespawn$setTrialSpawnerPos(BlockPos pos);

   boolean letyoudespawn$isReleasedByTrialSpawner();

   void letyoudespawn$releaseFromTrialSpawner();

   int letyoudespawn$getPickedSlots();

   void letyoudespawn$setPickedSlots(int slots);
}
