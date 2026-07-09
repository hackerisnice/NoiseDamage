package com.panda.noisedamage.mixin;

import com.panda.noisedamage.manager.NoiseManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Inject(method = "playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/sound/SoundCategory;FFJ)V",
            at = @At("TAIL"))
    private void onPlaySound(PlayerEntity except, double x, double y, double z,
                             RegistryEntry<SoundEvent> soundEntry, SoundCategory category,
                             float volume, float pitch, long seed, CallbackInfo ci) {
        ServerWorld world = (ServerWorld) (Object) this;
        SoundEvent sound = soundEntry.value();
        Vec3d pos = new Vec3d(x, y, z);

        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player == except) continue;
            NoiseManager.addNoise(player, sound, volume, pos);
        }
    }
}
