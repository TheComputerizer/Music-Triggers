package mods.thecomputerizer.musictriggers.util.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.ActionResult;

public interface LivingDamageEvent {
    Event<LivingDamageEvent> EVENT = EventFactory.createArrayBacked(LivingDamageEvent.class,
            (listeners) -> (entity, damageSource) -> {
                for (LivingDamageEvent listener : listeners) {
                    ActionResult result = listener.interact(entity, damageSource);
                    if(result != ActionResult.PASS) {
                        return result;
                    }
                }
                return ActionResult.PASS;
            });

    ActionResult interact(LivingEntity entity, DamageSource damageSource);
}
