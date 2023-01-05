package mods.thecomputerizer.musictriggers.mixin;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;

public interface CustomTickEvent {

    Event<CustomTickEvent> EVENT = EventFactory.createArrayBacked(CustomTickEvent.class,
            (listeners) -> () -> {
                for (CustomTickEvent listener : listeners) {
                    InteractionResult result = listener.interact();
                    if(result != InteractionResult.PASS) return result;
                }
                return InteractionResult.PASS;
            });

    InteractionResult interact();
}
