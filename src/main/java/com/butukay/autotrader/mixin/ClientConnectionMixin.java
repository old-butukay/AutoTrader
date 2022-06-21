package com.butukay.autotrader.mixin;

import com.butukay.autotrader.AutoTrader;
import com.butukay.autotrader.events.BreakPhase;
import com.butukay.autotrader.events.CheckPhase;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

    @Inject(method = "channelRead0*", at = @At("HEAD"))
    private void onChannelRead(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callbackInfo) {

        if (MinecraftClient.getInstance().player == null) return;

        if (packet.toString().contains("SetTradeOffersS2CPacket")) {
            if (AutoTrader.status == AutoTrader.Status.CHECK) {

                SetTradeOffersS2CPacket setTradeOffersS2CPacket = (SetTradeOffersS2CPacket) packet;
                TradeOfferList offerList = setTradeOffersS2CPacket.getOffers();

                AutoTrader.checkVillager(offerList);

                BreakPhase.EVENT.invoker().invoke();
            }
        }

        if (packet.toString().contains("EntityStatusS2CPacket")) {
            if (AutoTrader.status == AutoTrader.Status.PLACE) {
                CheckPhase.EVENT.invoker().invoke();
            }
        }
    }
}

//            String[] blacklist = {"WorldTimeUpdateS2CPacket", "EntityS2CPacket$MoveRelative", "EntitySetHeadYawS2CPacket", "EntityAttributesS2CPacket", "PlaySoundS2CPacket", "EntityPositionS2CPacket", "EntityS2CPacket$Rotate", "EntityTrackerUpdateS2CPacket", "Chunk"};
//
//            for (String p : blacklist) {
//                if (packet.toString().contains(p)) {
//                    return;
//                }
//            }
//            String text = packet.toString();
//            System.out.println(text);
//            MinecraftClient.getInstance().player.sendMessage(new LiteralText(text), false);