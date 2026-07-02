package io.github.smokahs.gtsf.mixin;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.sound.SoundEntry;

import io.github.smokahs.gtsf.api.IGTSFUseSound;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = GTToolType.class, remap = false)
public class GTToolTypeMixin implements IGTSFUseSound {

    @Unique
    private SoundEntry gtsf$useSound;

    @Override
    public void gtsf$setUseSound(@Nullable SoundEntry sound) {
        this.gtsf$useSound = sound;
    }

    @Override
    public @Nullable SoundEntry gtsf$getUseSound() {
        return this.gtsf$useSound;
    }
}
