package mods.thecomputerizer.musictriggers.legacy.v12.m2.client;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.io.FileHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.musictriggers.api.MTRef.NAME;
import static mods.thecomputerizer.theimpossiblelibrary.api.core.TILDev.DEV;

public class MTClientEntryPoint1_12_2 extends ClientEntryPoint {
    
    private static final List<String> MCMETA_LINES = Arrays.asList(
            "{","\t\"pack\": {","\t\t\"pack_format\": 3,",
            "\t\t\"description\": \"Relocated Music Triggers resources\"", "\t}", "}");
    
    @Nullable @Override public ClientEntryPoint delegatedClientEntry() {
        return this;
    }
    
    @Override protected String getModID() {
        return MODID;
    }
    
    @Override protected String getModName() {
        return NAME;
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
        if(DEV) {
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
    }
}