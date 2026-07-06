package io.github.smokahs.gtsf;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import io.github.smokahs.gtsf.api.IGTSFUseSound;
import io.github.smokahs.gtsf.common.data.GTSFSoundEntries;
import io.github.smokahs.gtsf.common.data.GTSFToolTiers;
import io.github.smokahs.gtsf.common.network.GTSFNetwork;
import io.github.smokahs.gtsf.config.GTSFConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(StarFoundry.MOD_ID)
public class StarFoundry {

    public static final String MOD_ID = "gtsf";
    public static final Logger LOGGER = LogManager.getLogger("GregTech: Star Foundry");
    public static final GTRegistrate REGISTRATE = GTRegistrate.create(MOD_ID);

    public StarFoundry() {
        GTSFConfig.init();

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        try {
            GTRegistrate.class.getMethod("registerRegistrate").invoke(REGISTRATE);
        } catch (NoSuchMethodException ignored) {} catch (ReflectiveOperationException e) {
            throw new IllegalStateException("failed to init star foundry registrate", e);
        }

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        GTSFToolTiers.init();
        GTSFNetwork.init();
        event.enqueueWork(StarFoundry::applyWrenchUseSounds);
    }

    private void clientSetup(FMLClientSetupEvent event) {}

    private static void applyWrenchUseSounds() {
        GTToolType[] wrenches = { GTToolType.WRENCH, GTToolType.WRENCH_LV, GTToolType.WRENCH_HV, GTToolType.WRENCH_IV };
        for (GTToolType wrench : wrenches) {
            ((IGTSFUseSound) (Object) wrench).gtsf$setUseSound(GTSFSoundEntries.WRENCH_USE_TOOL);
        }
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
