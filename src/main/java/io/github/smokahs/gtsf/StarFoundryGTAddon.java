package io.github.smokahs.gtsf;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.data.recipes.FinishedRecipe;

import io.github.smokahs.gtsf.common.data.GTSFItems;
import io.github.smokahs.gtsf.common.data.GTSFPrimordialTools;
import io.github.smokahs.gtsf.common.data.GTSFSoundEntries;

import java.util.function.Consumer;

@SuppressWarnings("unused")
@GTAddon
public class StarFoundryGTAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return StarFoundry.REGISTRATE;
    }

    @Override
    public void initializeAddon() {
        GTSFPrimordialTools.generatePrimordialTools();
        GTSFItems.init();
    }

    @Override
    public String addonModId() {
        return StarFoundry.MOD_ID;
    }

    @Override
    public void registerTagPrefixes() {}

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {}

    @Override
    public void registerElements() {}

    @Override
    public void registerSounds() {
        GTSFSoundEntries.init();
    }
}
