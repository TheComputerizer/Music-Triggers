package mods.thecomputerizer.musictriggers.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.advancements.Advancement;
import net.minecraft.world.InteractionResult;

public interface AdvancementEvent {
    Event<AdvancementEvent> EVENT = EventFactory.createArrayBacked(AdvancementEvent.class,
            (listeners) -> (advancement) -> {
                for (AdvancementEvent listener : listeners) {
                    InteractionResult result = listener.interact(advancement);
                    if(result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            });

    InteractionResult interact(Advancement advancement);
}