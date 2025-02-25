package mods.thecomputerizer.musictriggers.shared.v18.m2.client;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.shared.v18.m2.common.MTMappingsHelper1_18_2;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.core.ClassHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.core.CoreAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.io.FileHelper;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static mods.thecomputerizer.musictriggers.api.MTRef.BASE_PACKAGE;
import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.musictriggers.api.MTRef.NAME;
import static mods.thecomputerizer.theimpossiblelibrary.api.core.TILDev.DEV;

public class MTClientEntryPoint1_18_2 extends ClientEntryPoint {
    
    MTMappingsHelper1_18_2 reflector;
    
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
    
    @SuppressWarnings("deprecation") @Override public void onConstructed() {
        if(!DEV) return;
        CoreAPI core = CoreAPI.getInstance();
        String loader = core.getModLoader().toString();
        String reflectorPath = core.getVersion().getPackageName(BASE_PACKAGE+"."+loader.toLowerCase());
        String reflectorName = String.format("MTMappingsHelper%s1_18_2",loader);
        try {
            String reflectorClass = reflectorPath+".common."+reflectorName;
            ClassLoader currentLoader = getClass().getClassLoader();
            Class<?> cls = ClassHelper.findClass(reflectorClass,currentLoader);
            if(Objects.nonNull(cls)) this.reflector = (MTMappingsHelper1_18_2)cls.newInstance();
            else MTRef.logError("Failed to find 1.18.2 reflector (null class)");
        } catch(InstantiationException | IllegalAccessException ex) {
            MTRef.logError("Failed to find 1.18.2 reflector",ex);
        }
        if(Objects.nonNull(this.reflector)) {
            MTRef.logInfo("Attmpting to manually define dev resources");
            File resourceDir = new File("MTResources");
            if(resourceDir.exists() && resourceDir.isDirectory()) {
                FileHelper.writeLines(new File(resourceDir,"pack.mcmeta"),getMCMetaLines(),false);
                this.reflector.addDevResources(Minecraft.getInstance(),resourceDir);
            }
        }
    }
    
    @Override public void onLoadComplete() {
        ChannelHelper.getClientHelper().queryCategoryVolume();
    }
}