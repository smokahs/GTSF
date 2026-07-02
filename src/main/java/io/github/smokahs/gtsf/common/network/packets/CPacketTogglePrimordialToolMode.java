package io.github.smokahs.gtsf.common.network.packets;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import io.github.smokahs.gtsf.common.item.tool.PrimordialToolItem;
import io.github.smokahs.gtsf.common.network.GTSFNetwork;

public class CPacketTogglePrimordialToolMode implements GTSFNetwork.INetPacket {

    private final int toggles;

    public CPacketTogglePrimordialToolMode(int toggles) {
        this.toggles = toggles;
    }

    public CPacketTogglePrimordialToolMode(FriendlyByteBuf buf) {
        this.toggles = buf.readVarInt();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(toggles);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        var player = context.getSender();
        if (player == null) {
            return;
        }

        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof PrimordialToolItem toolItem)) {
            return;
        }

        var mode = toolItem.cycleMiningMode(held, toggles);
        player.getInventory().setChanged();
        player.displayClientMessage(
                Component.translatable("item.gtsf.tool.tooltip.mining_mode", mode.getDisplayName())
                        .withStyle(ChatFormatting.GOLD),
                true);
    }
}
