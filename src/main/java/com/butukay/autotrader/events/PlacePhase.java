package com.butukay.autotrader.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;

public interface PlacePhase {

    Event<PlacePhase> EVENT = EventFactory.createArrayBacked(PlacePhase.class,
        (listeners) -> () -> {
            for (PlacePhase listener : listeners) {
                ActionResult result = listener.invoke();

                if (result != ActionResult.PASS) {
                    return result;
                }
            }

            return ActionResult.PASS;
        });

    ActionResult invoke();
}