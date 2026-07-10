package com.panda.noisedamage.mixin.client;

import com.panda.noisedamage.client.ClientNoiseManager;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public class SoundManagerMixin {
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"))
    private void onPlaySound(SoundInstance sound, CallbackInfo ci) {
        try {
            ClientNoiseManager.onSoundPlayed(sound);
        } catch (Exception ignored) {
            // 完全不理会任何异常，避免影响游戏
        }
    }
}
