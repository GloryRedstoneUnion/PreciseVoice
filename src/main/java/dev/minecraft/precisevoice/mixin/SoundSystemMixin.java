package dev.minecraft.precisevoice.mixin;

import dev.minecraft.precisevoice.PreciseVoiceClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundSystem.class)
public abstract class SoundSystemMixin {
    @Shadow
    private float getAdjustedVolume(float volume, SoundCategory category) {
        throw new AssertionError();
    }

    @Redirect(
        method = "play(Lnet/minecraft/client/sound/SoundInstance;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/sound/SoundSystem;getAdjustedVolume(FLnet/minecraft/sound/SoundCategory;)F"
        )
    )
    private float preciseVoice$adjustNewSoundVolume(
        SoundSystem soundSystem,
        float volume,
        SoundCategory category,
        SoundInstance sound
    ) {
        float vanillaVolume = getAdjustedVolume(volume, category);
        return vanillaVolume * PreciseVoiceClient.getVolumeMultiplier(sound.getId());
    }

    @Inject(
        method = "getAdjustedVolume(Lnet/minecraft/client/sound/SoundInstance;)F",
        at = @At("RETURN"),
        cancellable = true
    )
    private void preciseVoice$adjustExistingSoundVolume(
        SoundInstance sound,
        CallbackInfoReturnable<Float> callback
    ) {
        float adjustedVolume = callback.getReturnValueF()
            * PreciseVoiceClient.getVolumeMultiplier(sound.getId());
        callback.setReturnValue(adjustedVolume);
    }
}
