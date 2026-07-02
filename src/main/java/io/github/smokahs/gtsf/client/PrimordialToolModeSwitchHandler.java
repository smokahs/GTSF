package io.github.smokahs.gtsf.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import io.github.smokahs.gtsf.StarFoundry;
import io.github.smokahs.gtsf.common.item.tool.PrimordialToolItem;
import io.github.smokahs.gtsf.common.network.GTSFNetwork;
import io.github.smokahs.gtsf.common.network.packets.CPacketTogglePrimordialToolMode;

@Mod.EventBusSubscriber(modid = StarFoundry.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PrimordialToolModeSwitchHandler {

    private PrimordialToolModeSwitchHandler() {}

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (Minecraft.getInstance().screen != null) {
            return;
        }

        var minecraft = Minecraft.getInstance();
        if (!minecraft.options.keyShift.isDown()) {
            return;
        }

        var player = minecraft.player;
        if (player == null) {
            return;
        }

        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof PrimordialToolItem)) {
            return;
        }

        int toggles = Math.max(1, (int) Math.round(Math.abs(event.getScrollDelta())));
        GTSFNetwork.sendToServer(new CPacketTogglePrimordialToolMode(toggles));
        event.setCanceled(true);
    }
}
