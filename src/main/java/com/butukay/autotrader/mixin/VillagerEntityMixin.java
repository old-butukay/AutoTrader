package com.butukay.autotrader.mixin;

import com.butukay.autotrader.AutoTrader;
import com.butukay.autotrader.events.PlacePhase;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity {

    @Shadow
    public abstract VillagerData getVillagerData();

    public VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    protected void tick(CallbackInfo ci) {
        if (!(AutoTrader.status == AutoTrader.Status.BREAK)) return;
        if (MinecraftClient.getInstance().player == null) return;
        if (this.getVillagerData().getProfession().equals(VillagerProfession.NONE)) return;

//        MinecraftClient.getInstance().player.sendMessage(new LiteralText("place"), false);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        PlacePhase.EVENT.invoker().invoke();
//        MinecraftClient.getInstance().player.sendMessage(new LiteralText("placed"), false);
    }


    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    public void afterUsing(TradeOffer offer) {
    }

    public void fillRecipes() {
    }
}
