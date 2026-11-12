package com.lvdriver.letyoudespawn.mixin;

import com.lvdriver.letyoudespawn.Constants;
import com.lvdriver.letyoudespawn.util.DespawnTracked;
import com.lvdriver.letyoudespawn.util.DespawnUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ported from Let You Despawn 1.26.11.1 {@code MobMixin}, with Almanac's picked-slot holder folded in
 * and the {@code @Redirect} on {@code discard()} replaced by cancellable injections.
 */
@Mixin(value = Mob.class, priority = 1010)
public abstract class MobMixin extends LivingEntity implements DespawnTracked {
   @Unique
   private static final String LYD_SPAWNER_KEY = "letyoudespawn_spawner";
   @Unique
   private static final String LYD_TRIAL_KEY = "letyoudespawn_trial_spawner";
   @Unique
   private static final String LYD_TRIAL_POS_KEY = "letyoudespawn_trial_spawner_pos";
   @Unique
   private static final String LYD_TRIAL_RELEASED_KEY = "letyoudespawn_trial_released";
   @Unique
   private static final String LYD_PICKED_SLOTS_KEY = "letyoudespawn_picked_slots";

   @Unique
   private boolean letyoudespawn$spawnedBySpawner;
   @Unique
   private boolean letyoudespawn$spawnedByTrialSpawner;
   @Unique
   private BlockPos letyoudespawn$trialSpawnerPos;
   @Unique
   private boolean letyoudespawn$releasedByTrialSpawner;
   @Unique
   private boolean letyoudespawn$persistentBeforeSlot;
   @Unique
   private boolean letyoudespawn$persistentBeforeEquip;
   @Unique
   private int letyoudespawn$pickedSlots;

   protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
      super(entityType, level);
   }

   @Shadow
   public abstract boolean isPersistenceRequired();

   @Override
   public boolean letyoudespawn$isFromSpawner() {
      return this.letyoudespawn$spawnedBySpawner;
   }

   @Override
   public void letyoudespawn$markFromSpawner() {
      this.letyoudespawn$spawnedBySpawner = true;
   }

   @Override
   public boolean letyoudespawn$isFromTrialSpawner() {
      return this.letyoudespawn$spawnedByTrialSpawner;
   }

   @Override
   public void letyoudespawn$markFromTrialSpawner() {
      this.letyoudespawn$spawnedByTrialSpawner = true;
   }

   @Override
   public BlockPos letyoudespawn$getTrialSpawnerPos() {
      return this.letyoudespawn$trialSpawnerPos;
   }

   @Override
   public void letyoudespawn$setTrialSpawnerPos(BlockPos pos) {
      this.letyoudespawn$trialSpawnerPos = pos;
   }

   @Override
   public boolean letyoudespawn$isReleasedByTrialSpawner() {
      return this.letyoudespawn$releasedByTrialSpawner;
   }

   @Override
   public void letyoudespawn$releaseFromTrialSpawner() {
      this.letyoudespawn$releasedByTrialSpawner = true;
      this.letyoudespawn$trialSpawnerPos = null;
   }

   @Override
   public int letyoudespawn$getPickedSlots() {
      return this.letyoudespawn$pickedSlots;
   }

   @Override
   public void letyoudespawn$setPickedSlots(int slots) {
      this.letyoudespawn$pickedSlots = slots;
   }

   @Inject(method = "finalizeSpawn", at = @At("HEAD"))
   private void letyoudespawn$trackSpawnReason(
      ServerLevelAccessor level,
      DifficultyInstance difficulty,
      MobSpawnType spawnReason,
      SpawnGroupData groupData,
      CallbackInfoReturnable<SpawnGroupData> cir
   ) {
      if (spawnReason == MobSpawnType.SPAWNER) {
         this.letyoudespawn$spawnedBySpawner = true;
      } else if (spawnReason == MobSpawnType.TRIAL_SPAWNER) {
         this.letyoudespawn$spawnedByTrialSpawner = true;
      }
   }

   @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
   private void letyoudespawn$saveState(CompoundTag output, CallbackInfo ci) {
      if (this.letyoudespawn$spawnedBySpawner) {
         output.putBoolean(LYD_SPAWNER_KEY, true);
      }
      if (this.letyoudespawn$spawnedByTrialSpawner) {
         output.putBoolean(LYD_TRIAL_KEY, true);
      }
      if (this.letyoudespawn$trialSpawnerPos != null) {
         output.putLong(LYD_TRIAL_POS_KEY, this.letyoudespawn$trialSpawnerPos.asLong());
      }
      if (this.letyoudespawn$releasedByTrialSpawner) {
         output.putBoolean(LYD_TRIAL_RELEASED_KEY, true);
      }
      if (this.letyoudespawn$pickedSlots != 0) {
         output.putInt(LYD_PICKED_SLOTS_KEY, this.letyoudespawn$pickedSlots);
      }
   }

   @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
   private void letyoudespawn$loadState(CompoundTag input, CallbackInfo ci) {
      this.letyoudespawn$spawnedBySpawner = input.getBoolean(LYD_SPAWNER_KEY) || this.letyoudespawn$spawnedBySpawner;
      this.letyoudespawn$spawnedByTrialSpawner = input.getBoolean(LYD_TRIAL_KEY) || this.letyoudespawn$spawnedByTrialSpawner;
      if (input.contains(LYD_TRIAL_POS_KEY)) {
         this.letyoudespawn$trialSpawnerPos = BlockPos.of(input.getLong(LYD_TRIAL_POS_KEY));
      }
      this.letyoudespawn$releasedByTrialSpawner = input.getBoolean(LYD_TRIAL_RELEASED_KEY) || this.letyoudespawn$releasedByTrialSpawner;
      this.letyoudespawn$pickedSlots = input.getInt(LYD_PICKED_SLOTS_KEY);
   }

   @Inject(method = "setPersistenceRequired", at = @At("HEAD"), cancellable = true)
   private void letyoudespawn$preventSpawnerPersistence(CallbackInfo ci) {
      boolean despawnable = this.letyoudespawn$spawnedByTrialSpawner && Constants.config.isTrialSpawnerDespawnEnabled()
         || this.letyoudespawn$spawnedBySpawner && Constants.config.isStandardSpawnerDespawnEnabled();
      if (despawnable && !this.hasCustomName()) {
         ci.cancel();
      }
   }

   @Inject(method = "setItemSlotAndDropWhenKilled", at = @At("HEAD"))
   private void letyoudespawn$beforeSetItemSlot(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
      this.letyoudespawn$persistentBeforeSlot = this.isPersistenceRequired();
   }

   @Inject(method = "setItemSlotAndDropWhenKilled", at = @At("TAIL"))
   private void letyoudespawn$afterSetItemSlot(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
      DespawnUtil.onItemEquipped((Mob)(Object)this, slot, this.letyoudespawn$persistentBeforeSlot);
   }

   @Inject(method = "equipItemIfPossible", at = @At("HEAD"))
   private void letyoudespawn$beforeEquip(ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
      this.letyoudespawn$persistentBeforeEquip = this.isPersistenceRequired();
   }

   @Inject(method = "equipItemIfPossible", at = @At("RETURN"))
   private void letyoudespawn$afterEquip(ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
      if (!cir.getReturnValue().isEmpty()) {
         Mob mob = (Mob)(Object)this;
         ((MobAccessor)mob).letyoudespawn$setPersistenceRequiredField(this.letyoudespawn$persistentBeforeEquip || DespawnUtil.keepsPickupPersistence(mob));
      }
   }

   @Inject(
      method = "checkDespawn",
      at = {
         @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;discard()V", ordinal = 1),
         @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;discard()V", ordinal = 2)
      },
      cancellable = true
   )
   private void letyoudespawn$beforeDespawn(CallbackInfo ci) {
      Mob mob = (Mob)(Object)this;
      if (DespawnUtil.isCountedByTrialSpawner(mob) || DespawnUtil.holdsPersistenceEnabler(mob)) {
         ci.cancel();
      } else {
         DespawnUtil.dropOnDespawn(mob);
      }
   }
}
