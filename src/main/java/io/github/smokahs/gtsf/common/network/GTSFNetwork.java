package io.github.smokahs.gtsf.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import io.github.smokahs.gtsf.StarFoundry;
import io.github.smokahs.gtsf.common.network.packets.CPacketTogglePrimordialToolMode;

import java.util.Optional;
import java.util.function.Function;

public class GTSFNetwork {

    private static final String PROTOCOL_VERSION = "1.0.0";
    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(StarFoundry.id("network"),
            () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    private static int nextPacketId = 0;

    public static void sendToServer(INetPacket packet) {
        try {
            INSTANCE.sendToServer(packet);
        } catch (Exception e) {
            StarFoundry.LOGGER.warn("failed to send packet, {}", e.getLocalizedMessage());
        }
    }

    public interface INetPacket {

        void encode(FriendlyByteBuf buffer);

        void execute(NetworkEvent.Context context);
    }

    public static <T extends INetPacket> void register(Class<T> cls, Function<FriendlyByteBuf, T> decode,
                                                       NetworkDirection direction) {
        INSTANCE.registerMessage(nextPacketId++, cls, INetPacket::encode, decode, (msg, ctx) -> {
            ctx.get().enqueueWork(() -> msg.execute(ctx.get()));
            ctx.get().setPacketHandled(true);
        }, Optional.ofNullable(direction));
    }

    public static void init() {
        register(CPacketTogglePrimordialToolMode.class, CPacketTogglePrimordialToolMode::new,
                NetworkDirection.PLAY_TO_SERVER);
    }
}
