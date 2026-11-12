package com.lvdriver.letyoudespawn.mixin;

import com.lvdriver.letyoudespawn.Constants;
import com.lvdriver.letyoudespawn.util.DespawnTracked;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TrialSpawner.class)
public class TrialSpawnerMixin {
   @WrapOperation(
      method = "spawnMob",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;setPersistenceRequired()V")
   )
   private void letyoudespawn$trialSpawned(Mob mob, Operation<Void> original, @Local(argsOnly = true) BlockPos spawnerPos) {
      ((DespawnTracked)mob).letyoudespawn$markFromTrialSpawner();
      ((DespawnTracked)mob).letyoudespawn$setTrialSpawnerPos(spawnerPos.immutable());
      if (!Constants.config.isTrialSpawnerDespawnEnabled()) {
         original.call(mob);
      }
   }
}
