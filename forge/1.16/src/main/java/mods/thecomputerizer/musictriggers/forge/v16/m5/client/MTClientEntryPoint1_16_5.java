package mods.thecomputerizer.musictriggers.forge.v16.m5.client;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.core.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.function.Function;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.musictriggers.api.MTRef.NAME;

public class MTClientEntryPoint1_16_5 extends ClientEntryPoint {
    
    @Nullable @Override public ClientEntryPoint delegatedClientEntry() {
        return this;
    }
    
    @Override protected String getModID() {
        return MODID;
    }
    
    @Override protected String getModName() {
        return NAME;
    }
    
    @Override public void onClientSetup() {
    }
    
    @Override public void onLoadComplete() {
        Minecraft mc = Minecraft.getInstance();
        setMusicTicker(mc);
        setSoundHandler(mc);
    }
    
    private <T> void setFinalField(Minecraft mc, T instance, String fieldName, String className,
            Function<Minecraft,T> getter) {
        MTRef.logInfo("Fixing vanilla {}",className);
        try {
            Field field = ObfuscationReflectionHelper.findField(Minecraft.class,fieldName);
            ReflectionHelper.modifyFinalField(mc,field,instance);
            MTRef.logInfo("{} class is now {}",className,getter.apply(mc).getClass());
        } catch(Exception ex) {
            MTRef.logError("Failed to replace {}",className,ex);
        }
    }
    
    private void setMusicTicker(Minecraft mc) {
        MusicTicker ticker = new MTMusicTicker1_16_5(mc);
        setFinalField(mc,ticker,"field_147126_aw","MusicTicker",Minecraft::getMusicManager);
    }
    
    private void setSoundHandler(Minecraft mc) {
        SoundHandler handler = new MTSoundHandler1_16_5(mc.getSoundManager(),mc.getResourceManager(),mc.options);
        setFinalField(mc,handler,"field_147127_av","SoundHandler",Minecraft::getMusicManager);
    }
}
