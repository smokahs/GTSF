package io.github.smokahs.gtsf.mixin;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.sound.SoundEntry;

import net.minecraft.server.level.ServerPlayer;

import io.github.smokahs.gtsf.api.IGTSFUseSound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ToolHelper.class, remap = false)
public class ToolHelperMixin {

    @Inject(method = "playToolSound", at = @At("HEAD"), cancellable = true)
    private static void gtsf$playUseSound(GTToolType toolType, ServerPlayer player, CallbackInfo ci) {
        if (toolType == null) {
            return;
        }
        SoundEntry useSound = ((IGTSFUseSound) (Object) toolType).gtsf$getUseSound();
        if (useSound != null) {
            useSound.playOnServer(player.level(), player.blockPosition());
            ci.cancel();
        }
    }
}
