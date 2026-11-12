package com.lvdriver.letyoudespawn.mixin;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Replaces the old access transformer on {@code Mob#persistenceRequired}. */
@Mixin(Mob.class)
public interface MobAccessor {
   @Accessor("persistenceRequired")
   void letyoudespawn$setPersistenceRequiredField(boolean value);

   @Accessor("persistenceRequired")
   boolean letyoudespawn$getPersistenceRequiredField();
}
