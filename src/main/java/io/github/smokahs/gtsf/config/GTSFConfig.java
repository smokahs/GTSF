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
    @Configurable.Comment("Config options for Star Foundry tools")
    public ToolConfigs tools = new ToolConfigs();

    public static class ToolConfigs {

        @Configurable
        @Configurable.Comment({ "Teleport blocks mined by Primordial tools directly into your inventory.",
                "Applies on the next block broken, no restart needed.", "Default: true" })
        public boolean relocateMinedBlocks = true;

        @Configurable
        @Configurable.Comment({ "Enable the Primordial Hard Hammer.",
                "Disabling removes the item from the game; requires restart.", "Default: true" })
        public boolean primordialHardHammer = true;
        @Configurable
        @Configurable.Comment({ "Enable the Primordial Soft Mallet.",
                "Disabling removes the item from the game; requires restart.", "Default: true" })
        public boolean primordialSoftMallet = true;
        @Configurable
        @Configurable.Comment({ "Enable the Primordial Wrench.",
                "Disabling removes the item from the game; requires restart.", "Default: true" })
        public boolean primordialWrench = true;
        @Configurable
        @Configurable.Comment({ "Enable the Primordial Crowbar.",
                "Disabling removes the item from the game; requires restart.", "Default: true" })
        public boolean primordialCrowbar = true;
        @Configurable
        @Configurable.Comment({ "Enable the Primordial Screwdriver.",
                "Disabling removes the item from the game; requires restart.", "Default: true" })
        public boolean primordialScrewdriver = true;
        @Configurable
        @Configurable.Comment({ "Enable the Primordial Mortar.",
                "Disabling removes the item from the game; requires restart.", "Default: true" })
        public boolean primordialMortar = true;
        @Configurable
        @Configurable.Comment({ "Enable the Primordial Wire Cutters.",
                "Disabling removes the item from the game; requires restart.", "Default: true" })
        public boolean primordialWireCutter = true;
        @Configurable
        @Configurable.Comment({ "Enable the Primordial Plunger.",
                "Disabling removes the item from the game; requires restart.", "Default: true" })
        public boolean primordialPlunger = true;
        @Configurable
        @Configurable.Comment({ "Enable the Primordial Saw.",
                "Disabling removes the item from the game; requires restart.", "Default: true" })
        public boolean primordialSaw = true;
        @Configurable
        @Configurable.Comment({ "Enable the Primordial File.",
                "Disabling removes the item from the game; requires restart.", "Default: true" })
        public boolean primordialFile = true;
        @Configurable
        @Configurable.Comment({ "Enable the Primordial Knife.",
                "Disabling removes the item from the game; requires restart.", "Default: true" })
        public boolean primordialKnife = true;
        @Configurable
        @Configurable.Comment({ "Enable the Primordial Healing Axe.",
                "Disabling removes the item from the game; requires restart.", "Default: true" })
        public boolean primordialHealingAxe = true;
    }
}
