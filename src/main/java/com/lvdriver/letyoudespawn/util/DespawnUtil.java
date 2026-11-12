package com.lvdriver.letyoudespawn.util;

import com.lvdriver.letyoudespawn.Constants;
import com.lvdriver.letyoudespawn.mixin.MobAccessor;
import com.lvdriver.letyoudespawn.mixin.TrialSpawnerDataAccessor;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public final class DespawnUtil {
   private static final int TRIAL_TRACKING_DISTANCE = 47;
   private static final int TRIAL_CHUNK_RADIUS = 3;

   private DespawnUtil() {
   }

   public static boolean keepsPickupPersistence(Mob mob) {
      return mob.hasCustomName() || Constants.config.isListedMob(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()));
   }

   public static void onItemEquipped(Mob mob, EquipmentSlot slot, boolean wasPersistent) {
      if (!mob.getItemBySlot(slot).isEmpty()) {
         EntityEquipmentTracker.markSlotAsPicked(mob, slot);
         ((MobAccessor)mob).letyoudespawn$setPersistenceRequiredField(wasPersistent || keepsPickupPersistence(mob));
      }
   }

   public static boolean holdsPersistenceEnabler(Mob mob) {
      Set<?> enablers = Constants.config.getPersistenceEnablerIds();
      if (enablers.isEmpty() || !EntityEquipmentTracker.hasAnyPickedEquipment(mob)) {
         return false;
      }
      for (EquipmentSlot slot : EntityEquipmentTracker.SLOTS) {
         if (EntityEquipmentTracker.isSlotPicked(mob, slot)) {
            ItemStack stack = mob.getItemBySlot(slot);
            if (!stack.isEmpty() && enablers.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
               return true;
            }
         }
      }
      return false;
   }

   public static void dropOnDespawn(Mob mob) {
      EntityUtil.dropEquipmentOnDiscard(mob);
      if (mob instanceof EnderMan enderman && enderman.level() instanceof ServerLevel) {
         BlockState carried = enderman.getCarriedBlock();
         if (carried != null) {
            ItemStack block = new ItemStack(carried.getBlock().asItem());
            if (!block.isEmpty()) {
               enderman.spawnAtLocation(block);
            }
            enderman.setCarriedBlock(null);
         }
      }
   }

   public static boolean isCountedByTrialSpawner(Mob mob) {
      DespawnTracked tracked = (DespawnTracked)mob;
      if (!tracked.letyoudespawn$isFromTrialSpawner() || tracked.letyoudespawn$isReleasedByTrialSpawner()) {
         return false;
      }
      if (!(mob.level() instanceof ServerLevel level)) {
         return false;
      }

      BlockPos pos = tracked.letyoudespawn$getTrialSpawnerPos();
      if (pos == null) {
         if (!isTrialAreaLoaded(level, mob)) {
            return true;
         }
         pos = findCountingTrialSpawner(level, mob);
         if (pos == null) {
            tracked.letyoudespawn$releaseFromTrialSpawner();
            return false;
         }
         tracked.letyoudespawn$setTrialSpawnerPos(pos);
      }

      if (!level.isLoaded(pos)) {
         return true;
      }
      if (countsTrialMob(level, pos, mob.getUUID())) {
         return true;
      }
      tracked.letyoudespawn$releaseFromTrialSpawner();
      return false;
   }

   private static boolean countsTrialMob(ServerLevel level, BlockPos pos, UUID id) {
      return level.getBlockEntity(pos) instanceof TrialSpawnerBlockEntity spawner
         && ((TrialSpawnerDataAccessor)spawner.getTrialSpawner().getData()).letyoudespawn$getCurrentMobs().contains(id);
   }

   private static boolean isTrialAreaLoaded(ServerLevel level, Mob mob) {
      int chunkX = SectionPos.blockToSectionCoord(mob.getBlockX());
      int chunkZ = SectionPos.blockToSectionCoord(mob.getBlockZ());
      for (int dx = -TRIAL_CHUNK_RADIUS; dx <= TRIAL_CHUNK_RADIUS; dx++) {
         for (int dz = -TRIAL_CHUNK_RADIUS; dz <= TRIAL_CHUNK_RADIUS; dz++) {
            if (level.getChunkSource().getChunkNow(chunkX + dx, chunkZ + dz) == null) {
               return false;
            }
         }
      }
      return true;
   }

   private static BlockPos findCountingTrialSpawner(ServerLevel level, Mob mob) {
      int chunkX = SectionPos.blockToSectionCoord(mob.getBlockX());
      int chunkZ = SectionPos.blockToSectionCoord(mob.getBlockZ());
      UUID id = mob.getUUID();
      for (int dx = -TRIAL_CHUNK_RADIUS; dx <= TRIAL_CHUNK_RADIUS; dx++) {
         for (int dz = -TRIAL_CHUNK_RADIUS; dz <= TRIAL_CHUNK_RADIUS; dz++) {
            LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX + dx, chunkZ + dz);
            if (chunk != null) {
               for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                  if (blockEntity instanceof TrialSpawnerBlockEntity && countsTrialMob(level, blockEntity.getBlockPos(), id)) {
                     return blockEntity.getBlockPos();
                  }
               }
            }
         }
      }
      return null;
   }
}
