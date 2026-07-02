package io.github.smokahs.gtsf.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import io.github.smokahs.gtsf.StarFoundry;
import io.github.smokahs.gtsf.client.shader.PrimordialWindowShaders;

@Mod.EventBusSubscriber(modid = StarFoundry.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class GTSFClientEvents {

    private GTSFClientEvents() {}

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        PrimordialWindowShaders.onRegisterShaders(event);
    }
}
