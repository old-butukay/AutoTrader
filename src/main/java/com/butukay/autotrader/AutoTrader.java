package com.butukay.autotrader;

import com.butukay.autotrader.events.CheckPhase;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import com.butukay.autotrader.events.BreakPhase;
import com.butukay.autotrader.events.PlacePhase;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.village.TradeOfferList;

@Environment(EnvType.CLIENT)
public class AutoTrader implements ModInitializer {

    public static Status status = Status.PAUSE;

    public enum Status {
        CHECK,
        BREAK,
        PLACE,
        PAUSE,
        FINISH
    }

    public static int villagerId;
    public static BlockPos blockPos;
    public static String enchantment;

    public static void start(int villagerId, String enchantment, BlockPos blockPos) {
        AutoTrader.status = Status.CHECK;

        AutoTrader.villagerId = villagerId;
        AutoTrader.enchantment = enchantment;
        AutoTrader.blockPos = blockPos;

        CheckPhase.EVENT.invoker().invoke();
    }

    public static void checkVillager(TradeOfferList tradeOfferList) {
        if (tradeOfferList.toNbt().toString().contains(enchantment)) {
            status = Status.FINISH;

            MinecraftClient.getInstance().player.sendMessage(new LiteralText("FINISH"), false);
        }
    }

    @Override
    public void onInitialize() {
        CheckPhase.EVENT.register(() -> {
            if (AutoTrader.status == AutoTrader.Status.FINISH || AutoTrader.status == AutoTrader.Status.PAUSE)
                return ActionResult.FAIL;
            if (MinecraftClient.getInstance().world == null) return ActionResult.FAIL;
            if (MinecraftClient.getInstance().interactionManager == null) return ActionResult.FAIL;

            AutoTrader.status = AutoTrader.Status.CHECK;

            Entity entity = MinecraftClient.getInstance().world.getEntityById(AutoTrader.villagerId);
            MinecraftClient.getInstance().interactionManager.interactEntity(MinecraftClient.getInstance().player, entity, Hand.MAIN_HAND);

            return ActionResult.PASS;
        });

        BreakPhase.EVENT.register(() -> {
            if (AutoTrader.status == AutoTrader.Status.FINISH || AutoTrader.status == AutoTrader.Status.PAUSE)
                return ActionResult.FAIL;
            if (MinecraftClient.getInstance().player == null) return ActionResult.FAIL;
            if (MinecraftClient.getInstance().interactionManager == null) return ActionResult.FAIL;
            if (MinecraftClient.getInstance().getNetworkHandler() == null) return ActionResult.FAIL;

            MinecraftClient.getInstance().player.currentScreenHandler.close(MinecraftClient.getInstance().player);

            AutoTrader.status = AutoTrader.Status.BREAK;

            MinecraftClient.getInstance().getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, AutoTrader.blockPos, Direction.NORTH));
            MinecraftClient.getInstance().getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, AutoTrader.blockPos, Direction.NORTH));

            return ActionResult.PASS;
        });

        PlacePhase.EVENT.register(() -> {
            if (AutoTrader.status == AutoTrader.Status.FINISH || AutoTrader.status == AutoTrader.Status.PAUSE)
                return ActionResult.FAIL;
            if (MinecraftClient.getInstance().getNetworkHandler() == null) return ActionResult.FAIL;

            AutoTrader.status = AutoTrader.Status.PLACE;

            BlockHitResult blockHitResult = (BlockHitResult) MinecraftClient.getInstance().crosshairTarget;
            MinecraftClient.getInstance().getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.OFF_HAND, blockHitResult));

            return ActionResult.PASS;
        });

        ClientCommandManager.DISPATCHER.register(
            ClientCommandManager.literal("checkevent")
                .executes(context -> {
                    CheckPhase.EVENT.invoker().invoke();
                    return 0;
                })
        );

        ClientCommandManager.DISPATCHER.register(
            ClientCommandManager.literal("breakevent")
                .executes(context -> {
                        BreakPhase.EVENT.invoker().invoke();
                        return 0;
                    }
                ));

        ClientCommandManager.DISPATCHER.register(
            ClientCommandManager.literal("placeevent")
                .executes(context -> {
                        PlacePhase.EVENT.invoker().invoke();
                        return 0;
                    }
                ));

        ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("start")
            .then(ClientCommandManager.argument("villager", IntegerArgumentType.integer())
                .then(ClientCommandManager.argument("enchantment", StringArgumentType.string())
                    .executes(context -> {
                        if (MinecraftClient.getInstance().world == null) return 0;
                        if (MinecraftClient.getInstance().player == null) return 0;

                        int id = IntegerArgumentType.getInteger(context, "villager");

                        if (MinecraftClient.getInstance().crosshairTarget == null) {
                            context.getSource().sendFeedback(new LiteralText("Please look at the profession block and try again"));
                        }

                        BlockPos blockPos = new BlockPos(MinecraftClient.getInstance().crosshairTarget.getPos());

                        String enchantment = StringArgumentType.getString(context, "enchantment");

                        assert enchantment != null;
                        context.getSource().sendFeedback(new LiteralText("Searching for: " + enchantment));

                        AutoTrader.start(id, enchantment, blockPos);
                        return 0;
                    })
                ))
        );

        ClientCommandManager.DISPATCHER.register(
            ClientCommandManager.literal("stop")
                .executes(context -> {
                        AutoTrader.status = AutoTrader.Status.PAUSE;
                        return 0;
                    }
                )
        );
    }
}

