package com.panda.noisedamage.mixin;

import com.panda.noisedamage.manager.NoiseManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void onReadNbt(NbtCompound nbt, CallbackInfo ci) {
        NoiseManager.readPlayerDataFromNbt((ServerPlayerEntity) (Object) this, nbt);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void onWriteNbt(NbtCompound nbt, CallbackInfo ci) {
        NoiseManager.writePlayerDataToNbt((ServerPlayerEntity) (Object) this, nbt);
    }
}
