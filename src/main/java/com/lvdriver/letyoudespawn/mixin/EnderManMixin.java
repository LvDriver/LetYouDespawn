package com.lvdriver.letyoudespawn.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnderMan.class)
public abstract class EnderManMixin {
   @WrapOperation(
      method = "requiresCustomPersistence",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/EnderMan;getCarriedBlock()Lnet/minecraft/world/level/block/state/BlockState;")
   )
   private BlockState letyoudespawn$ignoreCarriedBlockForPersistence(EnderMan instance, Operation<BlockState> original) {
      return null;
   }
}
