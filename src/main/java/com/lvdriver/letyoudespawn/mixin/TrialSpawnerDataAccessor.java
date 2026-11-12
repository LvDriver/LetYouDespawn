package com.lvdriver.letyoudespawn.mixin;

import java.util.Set;
import java.util.UUID;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TrialSpawnerData.class)
public interface TrialSpawnerDataAccessor {
   @Accessor("currentMobs")
   Set<UUID> letyoudespawn$getCurrentMobs();
}
