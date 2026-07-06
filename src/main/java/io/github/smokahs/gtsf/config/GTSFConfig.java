package io.github.smokahs.gtsf.config;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.format.ConfigFormats;
import io.github.smokahs.gtsf.StarFoundry;

@Config(id = StarFoundry.MOD_ID)
public class GTSFConfig {

    public static GTSFConfig INSTANCE;
    private static final Object LOCK = new Object();

    public static GTSFConfig get() {
        init();
        return INSTANCE;
    }

    public static void init() {
        synchronized (LOCK) {
            if (INSTANCE == null) {
                INSTANCE = Configuration.registerConfig(GTSFConfig.class, ConfigFormats.yaml()).getConfigInstance();
            }
        }
    }

    @Configurable
    @Configurable.Comment("Star Foundry tools")
    public ToolConfigs tools = new ToolConfigs();

    public static class ToolConfigs {

        @Configurable
        @Configurable.Comment("Tool behavior")
        public General general = new General();

        @Configurable
        @Configurable.Comment("Enable tools. Restart to apply.")
        public Items items = new Items();
    }

    public static class General {

        @Configurable
        @Configurable.Comment({ "Mined blocks go to inventory.", "Default: true" })
        public boolean relocateMinedBlocks = true;

        @Configurable
        @Configurable.Comment({ "Precision mode mining speed.", "Default: 8" })
        @Configurable.DecimalRange(min = 1.0, max = 100.0)
        public double precisionMiningSpeed = 8.0;

        @Configurable
        @Configurable.Comment({ "Animated tool shader. Reload resources to apply.", "Default: true" })
        public boolean primordialShader = true;
    }

    public static class Items {

        @Configurable
        @Configurable.Comment("Default: true")
        public boolean primordialHardHammer = true;
        @Configurable
        @Configurable.Comment("Default: true")
        public boolean primordialSoftMallet = true;
        @Configurable
        @Configurable.Comment("Default: true")
        public boolean primordialWrench = true;
        @Configurable
        @Configurable.Comment("Default: true")
        public boolean primordialCrowbar = true;
        @Configurable
        @Configurable.Comment("Default: true")
        public boolean primordialScrewdriver = true;
        @Configurable
        @Configurable.Comment("Default: true")
        public boolean primordialMortar = true;
        @Configurable
        @Configurable.Comment("Default: true")
        public boolean primordialWireCutter = true;
        @Configurable
        @Configurable.Comment("Default: true")
        public boolean primordialPlunger = true;
        @Configurable
        @Configurable.Comment("Default: true")
        public boolean primordialSaw = true;
        @Configurable
        @Configurable.Comment("Default: true")
        public boolean primordialFile = true;
        @Configurable
        @Configurable.Comment("Default: true")
        public boolean primordialKnife = true;
        @Configurable
        @Configurable.Comment("Default: true")
        public boolean primordialHealingAxe = true;
    }
}
