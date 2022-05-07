package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.util.events.AdvancementEvent;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerAdvancementTracker.class)
public class MixinAdvancementEvent {

    @Inject(method = "grantCriterion(Lnet/minecraft/advancement/Advancement;Ljava/lang/String;)Z", at = @At(value = "HEAD", target = "Lnet/minecraft/advancement/PlayerAdvancementTracker;grantCriterion(Lnet/minecraft/advancement/Advancement;Ljava/lang/String;)Z"))
    private void grantCriterion(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        if(((PlayerAdvancementTracker)(Object)this).getProgress(advancement).isDone()) {
            AdvancementEvent.EVENT.invoker().interact(advancement);
        }
    }
}
