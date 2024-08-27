package mods.thecomputerizer.musictriggers.forge.v16.m5.client;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.core.ReflectionHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.io.FileHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.musictriggers.api.MTRef.NAME;
import static mods.thecomputerizer.theimpossiblelibrary.api.core.TILDev.DEV;

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
    
    List<String> getMCMetaLines() {
        return Arrays.asList(
                "{","\t\"pack\": {","\t\t\"pack_format\": 4,",
                "\t\t\"description\": \"Relocated Music Triggers resources\"", "\t}", "}");
    }
    
    @Override public void onClientSetup() {
    }
    
    @Override public void onConstructed() {
        if(DEV) {
            MTRef.logInfo("Attmpting to manually define dev resources");
            File resourceDir = new File("MTResources");
            if(resourceDir.exists() && resourceDir.isDirectory()) {
                FileHelper.writeLines(new File(resourceDir,"pack.mcmeta"),getMCMetaLines(),false);
                Minecraft.getInstance().getResourcePackRepository().addPackFinder(new MTDevResourceFinder1_16_5(resourceDir));
            }
        }
    }
    
    @Override public void onLoadComplete() {
        Minecraft.getInstance().submit(() -> {
            Minecraft mc = Minecraft.getInstance();
            setMusicTicker(mc);
            //setSoundHandler(mc);
        });
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
