package com.butukay.autotrader.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;

public interface CheckPhase {

    Event<CheckPhase> EVENT = EventFactory.createArrayBacked(CheckPhase.class,
        (listeners) -> () -> {
            for (CheckPhase listener : listeners) {
                ActionResult result = listener.invoke();

                if (result != ActionResult.PASS) {
                    return result;
                }
            }

            return ActionResult.PASS;
        });

    ActionResult invoke();
}