package io.github.smokahs.gtsf.common.data;

import com.gregtechceu.gtceu.api.sound.SoundEntry;

import io.github.smokahs.gtsf.StarFoundry;

public final class GTSFSoundEntries {

    public static final SoundEntry WRENCH_USE_TOOL = StarFoundry.REGISTRATE.sound(StarFoundry.id("wrench_use")).build();

    private GTSFSoundEntries() {}

    public static void init() {
        StarFoundry.REGISTRATE.addRawLang("gtsf.subtitle.wrench_use", "Wrench used");
    }
}
