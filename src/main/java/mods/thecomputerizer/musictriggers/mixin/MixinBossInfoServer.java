package mods.thecomputerizer.musictriggers.mixin;


import mods.thecomputerizer.musictriggers.server.channels.ServerTriggerStatus;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.end.DragonFightManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Mixin(BossInfoServer.class)
public class MixinBossInfoServer {

    @Unique
    private static final HashMap<Class<?>,Class<? extends EntityLivingBase>> WHITELISTED_BOSS_CLASSES = musicTriggers$makeBossWhitelist();

    @Unique
    private static HashMap<Class<?>,Class<? extends EntityLivingBase>> musicTriggers$makeBossWhitelist() {
        HashMap<Class<?>,Class<? extends EntityLivingBase>> ret = new HashMap<>();
        ret.put(DragonFightManager.class,EntityDragon.class);
        return ret;
    }

    @Unique
    private BossInfoServer musictriggers$cast() {
        return (BossInfoServer)(Object)this;
    }

    @SuppressWarnings("unchecked")
    @Inject(at = @At(value = "RETURN"), method = "<init>")
    private void musictriggers$init(ITextComponent display, BossInfo.Color color, BossInfo.Overlay overlay, CallbackInfo ci) {
        int stackLimit = 10;
        for(StackTraceElement element : Thread.currentThread().getStackTrace()) {
            try {
                Class<?> elementClass = Class.forName(element.getClassName());
                if(EntityLivingBase.class.isAssignableFrom(elementClass)) {
                    ServerTriggerStatus.bossBarInstantiated(musictriggers$cast(), (Class<? extends EntityLivingBase>) elementClass);
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