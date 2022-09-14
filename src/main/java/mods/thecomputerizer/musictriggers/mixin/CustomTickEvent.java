package mods.thecomputerizer.musictriggers.mixin;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;

public interface CustomTickEvent {

    Event<CustomTickEvent> EVENT = EventFactory.createArrayBacked(CustomTickEvent.class,
            (listeners) -> () -> {
                for (CustomTickEvent listener : listeners) {
                    ActionResult result = listener.interact();
                    if(result != ActionResult.PASS) return result;
                }
                return ActionResult.PASS;
            });

    ActionResult interact();
}
