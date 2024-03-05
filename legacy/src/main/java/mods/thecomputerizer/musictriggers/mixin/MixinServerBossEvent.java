package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.server.channels.ServerTriggerStatus;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Mixin(ServerBossEvent.class)
public class MixinServerBossEvent {

    @Unique
    private static final HashMap<Class<?>,Class<? extends LivingEntity>> WHITELISTED_BOSS_CLASSES = musicTriggers$makeBossWhitelist();

    @Unique
    private static HashMap<Class<?>,Class<? extends LivingEntity>> musicTriggers$makeBossWhitelist() {
        HashMap<Class<?>,Class<? extends LivingEntity>> ret = new HashMap<>();
        ret.put(EndDragonFight.class, EnderDragon.class);
        return ret;
    }

    @Unique
    private ServerBossEvent musictriggers$cast() {
        return (ServerBossEvent)(Object)this;
    }

    @SuppressWarnings("unchecked")
    @Inject(at = @At(value = "RETURN"), method = "<init>")
    private void musictriggers$init(Component display, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay, CallbackInfo ci) {
        int stackLimit = 10;
        for(StackTraceElement element : Thread.currentThread().getStackTrace()) {
            try {
                Class<?> elementClass = Class.forName(element.getClassName());
                if(LivingEntity.class.isAssignableFrom(elementClass)) {
                    ServerTriggerStatus.bossBarInstantiated(musictriggers$cast(), (Class<? extends LivingEntity>) elementClass);
                    break;
                } else if(musicTriggers$checkWhitelistedClass(elementClass)) break;
            } catch (ClassNotFoundException ignored) {}
            stackLimit--;
            if(stackLimit<0) break;
        }
    }

    @Unique
    private boolean musicTriggers$checkWhitelistedClass(Class<?> elementClass) {
        if(WHITELISTED_BOSS_CLASSES.containsKey(elementClass)) {
            ServerTriggerStatus.bossBarInstantiated(musictriggers$cast(), WHITELISTED_BOSS_CLASSES.get(elementClass));
            return true;
        }
        return false;
    }
}
