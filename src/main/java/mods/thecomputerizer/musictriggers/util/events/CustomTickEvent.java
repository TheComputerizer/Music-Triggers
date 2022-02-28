package mods.thecomputerizer.musictriggers.util.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;

public interface CustomTickEvent {

    Event<CustomTickEvent> EVENT = EventFactory.createArrayBacked(CustomTickEvent.class,
            (listeners) -> (client) -> {
            });

    void interact(MinecraftClient client);
}
