package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.util.events.AdvancementEvent;
import net.minecraft.advancements.Advancement;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerAdvancements.class)
public class MixinAdvancementEvent {

    @Inject(method = "award(Lnet/minecraft/advancements/Advancement;Ljava/lang/String;)Z", at = @At(value = "HEAD", target = "Lnet/minecraft/advancement/PlayerAdvancementTracker;grantCriterion(Lnet/minecraft/advancement/Advancement;Ljava/lang/String;)Z"))
    private void award(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        if(((PlayerAdvancements)(Object)this).getOrStartProgress(advancement).isDone()) {
            AdvancementEvent.EVENT.invoker().interact(advancement);
        }
    }
}
