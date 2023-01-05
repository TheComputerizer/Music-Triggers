package mods.thecomputerizer.musictriggers.util.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public interface LivingDamageEvent {
    Event<LivingDamageEvent> EVENT = EventFactory.createArrayBacked(LivingDamageEvent.class,
            (listeners) -> (entity, damageSource) -> {
                for (LivingDamageEvent listener : listeners) {
                    InteractionResult result = listener.interact(entity, damageSource);
                    if(result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            });

    InteractionResult interact(LivingEntity entity, DamageSource damageSource);
}
