package io.github.smokahs.gtsf.client;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.IItemDecorator;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import io.github.smokahs.gtsf.StarFoundry;
import io.github.smokahs.gtsf.client.shader.PrimordialWindowShaders;
import io.github.smokahs.gtsf.common.data.GTSFPrimordialTools;

@Mod.EventBusSubscriber(modid = StarFoundry.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class GTSFClientEvents {

    private GTSFClientEvents() {}

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        PrimordialWindowShaders.onRegisterShaders(event);
    }

    @SubscribeEvent
    public static void onRegisterItemDecorations(RegisterItemDecorationsEvent event) {
        // stamp unbreakable
        IItemDecorator suppressor = (guiGraphics, font, stack, x, y) -> {
            CompoundTag tag = stack.getOrCreateTag();
            if (!tag.getBoolean(ToolHelper.UNBREAKABLE_KEY)) {
                tag.putBoolean(ToolHelper.UNBREAKABLE_KEY, true);
            }
            return false;
        };
        for (var entry : GTSFPrimordialTools.getEntries()) {
            event.register(entry.get(), suppressor);
        }
    }
}
