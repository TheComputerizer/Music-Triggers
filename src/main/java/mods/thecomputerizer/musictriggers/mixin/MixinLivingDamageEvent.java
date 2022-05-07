package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.util.events.LivingDamageEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class MixinLivingDamageEvent {
    @Inject(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At(value = "HEAD", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"))
    private void applyDamage(DamageSource source, float amount, CallbackInfo info) {
        LivingDamageEvent.EVENT.invoker().interact((LivingEntity)(Object)this,source);
    }
}
