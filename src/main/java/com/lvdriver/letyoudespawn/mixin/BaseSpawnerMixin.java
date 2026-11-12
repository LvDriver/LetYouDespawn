package com.lvdriver.letyoudespawn.mixin;

import com.lvdriver.letyoudespawn.util.DespawnTracked;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BaseSpawner.class)
public class BaseSpawnerMixin {
   @ModifyArg(
      method = "serverTick",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/server/level/ServerLevel;tryAddFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)Z"
      )
   )
   private Entity letyoudespawn$markSpawned(Entity entity) {
      if (entity instanceof DespawnTracked tracked) {
         tracked.letyoudespawn$markFromSpawner();
      }
      return entity;
   }
}
