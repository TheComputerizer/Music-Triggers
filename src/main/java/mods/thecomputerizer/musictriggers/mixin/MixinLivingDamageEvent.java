package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.util.events.LivingDamageEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinLivingDamageEvent {
    @Inject(method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At(value = "HEAD", target = "Lnet/minecraft/world/entity/LivingEntity;applyDamage(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
    private void hurt(DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        LivingDamageEvent.EVENT.invoker().interact((LivingEntity)(Object)this,damageSource);
    }
}
