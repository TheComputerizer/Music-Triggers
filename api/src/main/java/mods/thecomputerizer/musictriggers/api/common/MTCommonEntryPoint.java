package mods.thecomputerizer.musictriggers.api.common;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.client.MTClientEntryPoint;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.network.MTNetwork;
import mods.thecomputerizer.musictriggers.api.registry.MTRegistryHandler;
import mods.thecomputerizer.musictriggers.api.server.MTServerEvents;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.common.CommonEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.common.DelegatingCommonEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.core.ClassHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.core.CoreAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.core.CoreAPI.GameVersion;
import mods.thecomputerizer.theimpossiblelibrary.api.core.annotation.MultiVersionMod;

import javax.annotation.Nullable;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static mods.thecomputerizer.musictriggers.api.MTRef.*;
import static mods.thecomputerizer.theimpossiblelibrary.api.core.CoreAPI.ModLoader.LEGACY;
import static mods.thecomputerizer.theimpossiblelibrary.api.core.TILDev.DEV;
import static org.burningwave.core.assembler.StaticComponentContainer.ClassLoaders;
import static org.burningwave.core.assembler.StaticComponentContainer.Methods;

@MultiVersionMod(modDescription = DESCRIPTION, modid = MODID, modName = NAME, modVersion = VERSION)
public class MTCommonEntryPoint extends DelegatingCommonEntryPoint {
    
    public MTCommonEntryPoint() {
        MTRef.logDebug("Constructing MTCommonEntryPoint on ClassLoader {}",getClass().getClassLoader());
    }
    
    private String getLoader(CoreAPI instance) {
        String loaderName = instance.getModLoader().toString();
        return LEGACY.toString().equals(loaderName) ? loaderName.toLowerCase() : "shared";
    }
    
    private Class<?> findVersionEntryClass(CoreAPI instance) {
        MTRef.logDebug("Finding version entrypoint on ClassLoader {}",instance.getClass().getClassLoader());
        GameVersion version = instance.getVersion();
        String loader = getLoader(instance);
        String className = BASE_PACKAGE+"."+loader+"."+version.getPkg()+".common.MTCommonEntryPoint";
        className+=version.getName().replace('.','_');
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        if(DEV && version.isV12()) {
            String versionStr = "/legacy/1.12/";
            Set<String> contains = new HashSet<>(Arrays.asList("/api/","/"+loader+"/",versionStr));
            ClassHelper.checkBurningWaveInit();
            for(URL url : ClassLoaders.getURLs(ClassLoader.getSystemClassLoader())) {
                for(String contain : contains) {
                    if(!url.toString().contains(contain)) continue;
                    try {
                        instance.addURLToClassLoader(contextLoader,url);
                    } catch(Exception ex) {
                        MTRef.logError("ok",ex);
                    }
                    break;
                }
            }
        }
        try {
            Class<?> clazz = ClassHelper.findClass(className,true,contextLoader);
            if(Objects.nonNull(clazz))
                MTRef.logInfo("Successfully located versioned entrypoint {} to {}",clazz,clazz.getClassLoader());
            else MTRef.logError("Why is the class null {}",className);
            return clazz;
        } catch(Exception ex) {
            MTRef.logError("Caught exception getting version entrypoint class {}",className,ex);
            return null;
        }
    }

    @Override protected String getModID() {
        return MODID;
    }

    @Override protected String getModName() {
        return NAME;
    }

    @Override public void onConstructed() {
        File configDir = new File(CONFIG_PATH);
        if(!configDir.exists() && !configDir.mkdirs())
            throw new RuntimeException("Unable to create file directory at "+CONFIG_PATH+"! Music Triggers "+
                    "is unable to load any further.");
        MTNetwork.initCommon();
        super.onConstructed();
    }
    
    /**
     * Generate default config files on dedicated servers
     */
    @Override public void onDedicatedServerSetup() {
        ChannelHelper.generateDedicatedServerFiles();
    }

    @Override public void onPreRegistration() {
        MTRegistryHandler.init();
        MTCommonEvents.init();
        MTServerEvents.init();
        super.onPreRegistration();
    }
    
    @Override public @Nullable ClientEntryPoint setDelegatedClientHandle() {
        return MTClientEntryPoint.getInstance();
    }
    
    @Override public CommonEntryPoint setDelegatedCustomHandle() {
        if(!DEV || !CoreAPI.isV12()) return null;
        CommonEntryPoint instance = null;
        Class<?> versionClass = findVersionEntryClass(CoreAPI.getInstance());
        if(Objects.nonNull(versionClass)) {
            try {
                ClassHelper.checkBurningWaveInit();
                instance = Methods.invokeStatic(versionClass,"getInstance");
            } catch(Throwable t) {
                MTRef.logFatal("Unable to instantiate versioned instance!",t);
            }
        } else MTRef.logError("Versioned entrypoint not found! Things might not work properly");
        return instance;
    }
}