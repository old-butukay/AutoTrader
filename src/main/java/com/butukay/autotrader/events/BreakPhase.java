package com.butukay.autotrader.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;

public interface BreakPhase {

    Event<BreakPhase> EVENT = EventFactory.createArrayBacked(BreakPhase.class,
        (listeners) -> () -> {
            for (BreakPhase listener : listeners) {
                ActionResult result = listener.invoke();

                if (result != ActionResult.PASS) {
                    return result;
                }
            }

            return ActionResult.PASS;
        });

    ActionResult invoke();
}