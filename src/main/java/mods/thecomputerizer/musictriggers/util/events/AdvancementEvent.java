package mods.thecomputerizer.musictriggers.util.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.advancement.Advancement;
import net.minecraft.util.ActionResult;

public interface AdvancementEvent {
    Event<AdvancementEvent> EVENT = EventFactory.createArrayBacked(AdvancementEvent.class,
            (listeners) -> (advancement) -> {
                for (AdvancementEvent listener : listeners) {
                    ActionResult result = listener.interact(advancement);
                    if(result != ActionResult.PASS) {
                        return result;
                    }
                }
                return ActionResult.PASS;
            });

    ActionResult interact(Advancement advancement);
}
