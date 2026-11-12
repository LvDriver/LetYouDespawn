package com.lvdriver.letyoudespawn.mixin;

import com.lvdriver.letyoudespawn.util.DespawnUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArmorItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ArmorItem.class)
public class ArmorItemMixin {
   @WrapOperation(
      method = "dispenseArmor",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;setPersistenceRequired()V")
   )
   private static void letyoudespawn$dispensedItem(Mob mob, Operation<Void> original, @Local EquipmentSlot slot) {
      DespawnUtil.onItemEquipped(mob, slot, mob.isPersistenceRequired());
   }
}
