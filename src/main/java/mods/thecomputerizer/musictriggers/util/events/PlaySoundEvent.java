package mods.thecomputerizer.musictriggers.util.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.sound.SoundInstance;

public interface PlaySoundEvent {

    Event<PlaySoundEvent> EVENT = EventFactory.createArrayBacked(PlaySoundEvent.class,
            (listeners) -> (sound) -> {
                for (PlaySoundEvent listener : listeners) {
                    sound = listener.interact(sound);
                }
                return sound;
            });

    SoundInstance interact(SoundInstance sound);
}
