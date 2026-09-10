package com.frnc.misc.mechanics.elytraflight.network;

import com.frnc.misc.mechanics.elytraflight.ElytraFlightHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/** 客户端 -> 服务端: 切换鞘翅飞行开关, 服务端回发聊天消息并把新状态同步给客户端 */
public class ToggleElytraFlightPacket
{
    public ToggleElytraFlightPacket() {}

    public static void encode(ToggleElytraFlightPacket msg, FriendlyByteBuf buf) {}

    public static ToggleElytraFlightPacket decode(FriendlyByteBuf buf)
    {
        return new ToggleElytraFlightPacket();
    }

    public static void handle(ToggleElytraFlightPacket msg, Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() ->
        {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            boolean newState = ElytraFlightHandler.toggleFlight(player);

            Component message = Component.literal(newState ? "鞘翅飞行已开启" : "鞘翅飞行已关闭")
                    .withStyle(newState ? ChatFormatting.GREEN : ChatFormatting.RED);
            player.sendSystemMessage(message);

            // 同步状态给客户端, 使其据此决定是否触发鞘翅飞行
            ElytraFlightNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new ElytraFlightStatePacket(newState));
        });
        ctx.get().setPacketHandled(true);
    }
}
