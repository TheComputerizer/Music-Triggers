package mods.thecomputerizer.musictriggers.legacy.v12.m2.client;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.core.TILDev;
import mods.thecomputerizer.theimpossiblelibrary.api.io.FileHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MTClientEntryPoint1_12_2 extends ClientEntryPoint {
    
    private static final List<String> MCMETA_LINES = Arrays.asList(
            "{","\t\"pack\": {","\t\t\"pack_format\": 3,",
            "\t\t\"description\": \"Relocated Music Triggers resources\"", "\t}", "}");
    
    @Nullable @Override
    public ClientEntryPoint delegatedClientEntry() {
        return this;
    }
    
    @Override protected String getModID() {
        return MTRef.MODID;
    }
    
    @Override protected String getModName() {
        return MTRef.NAME;
    }
    
    private @Nullable List<IResourcePack> getResourcePacks(Minecraft mc) {
        try {
            return ObfuscationReflectionHelper.getPrivateValue(Minecraft.class,mc,"field_110449_ao");
        } catch(Exception ex) {
            MTRef.logError("Unable to get resource pack list",ex);
            return null;
        }
    }
    
    @Override public void onConstructed() {
        if(TILDev.DEV) {
            MTRef.logInfo("Attmpting to manually define dev resources");
            File resourceDir = new File("MTResources");
            if(resourceDir.exists() && resourceDir.isDirectory()) {
                FileHelper.writeLines(new File(resourceDir,"pack.mcmeta"),MCMETA_LINES,false);
                List<IResourcePack> defaultPacks = getResourcePacks(Minecraft.getMinecraft());
                if(Objects.nonNull(defaultPacks)) defaultPacks.add(new FolderResourcePack(resourceDir));
            } else MTRef.logError("The MTResources directory doesn't seem to exist. Were the resources copied correctly?");
        }
    }
    
    @Override public void onClientSetup() {} //Undelegated
    
    @Override public void onLoadComplete() {
        Minecraft mc = Minecraft.getMinecraft();
        setMusicTicker(mc,new MTMusicTicker1_12_2(mc));
        setSoundHandler(mc,new MTSoundHandler1_12_2(mc.getResourceManager(),mc.gameSettings,mc.getSoundHandler()));
    }
    
    private void setMusicTicker(Minecraft mc, MusicTicker ticker) {
        MTRef.logInfo("Fixing vanilla MusicTicker");
        try {
            ObfuscationReflectionHelper.setPrivateValue(Minecraft.class,mc,ticker,"field_147126_aw");
        } catch(Exception ex) {
            MTRef.logError("Failed to replace MusicTicker",ex);
        }
    }
    
    private void setSoundHandler(Minecraft mc, SoundHandler handler) {
        MTRef.logInfo("Fixing vanilla SoundHandler");
        try {
            ObfuscationReflectionHelper.setPrivateValue(Minecraft.class,mc,handler,"field_147127_av");
            MTRef.logInfo("SoundHandler class is now {}",mc.getSoundHandler().getClass());
        } catch(Exception ex) {
            MTRef.logError("Failed to replace SoundHandler",ex);
        }
    }
}