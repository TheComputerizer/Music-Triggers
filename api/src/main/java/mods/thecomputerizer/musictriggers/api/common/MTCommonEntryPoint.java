package mods.thecomputerizer.musictriggers.api.common;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.client.MTClientEntryPoint;
import mods.thecomputerizer.musictriggers.api.network.MTNetwork;
import mods.thecomputerizer.musictriggers.api.registry.MTRegistryHandler;
import mods.thecomputerizer.musictriggers.api.server.MTServerEvents;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.common.CommonEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.core.ClassHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.core.CoreAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.core.loader.MultiVersionMod;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Objects;
import java.util.function.Consumer;

import static mods.thecomputerizer.musictriggers.api.MTRef.CONFIG_PATH;
import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.musictriggers.api.MTRef.NAME;

@SuppressWarnings("unused")
@MultiVersionMod(modid = MODID, modName = NAME, modVersion = MTRef.VERSION)
public class MTCommonEntryPoint extends CommonEntryPoint {
    
    final CommonEntryPoint versionInstance;
    
    public MTCommonEntryPoint() {
        MTRef.logDebug("Constructing MTCommonEntryPoint on ClassLoader {}",getClass().getClassLoader());
        CommonEntryPoint instance = null;
        Class<? extends CommonEntryPoint> versionClass = findVersionEntryClass(CoreAPI.getInstance());
        if(Objects.nonNull(versionClass)) {
            try {
                instance = versionClass.newInstance();
            } catch(ReflectiveOperationException ex) {
                MTRef.logFatal("Unable to instantiate versioned instance!",ex);
            }
        } else MTRef.logError("Versioned entrypoint not found! Things might not work properly");
        this.versionInstance = instance;
    }

    @Override
    public @Nullable ClientEntryPoint delegatedClientEntry() {
        return new MTClientEntryPoint();
    }
    
    private void distributeHook(Consumer<CommonEntryPoint> hook) {
        if(Objects.nonNull(this.versionInstance)) hook.accept(this.versionInstance);
        if(Objects.nonNull(this.delegatedClient)) hook.accept(this.delegatedClient);
    }
    
    @SuppressWarnings("unchecked")
    private Class<? extends CommonEntryPoint> findVersionEntryClass(CoreAPI instance) {
        MTRef.logDebug("Finding version entrypoint on ClassLoader {}",instance.getClass().getClassLoader());
        String pkg = CoreAPI.getInstance().getPackageName("mods.thecomputerizer.musictriggers");
        String version = instance.getVersion().getName().replace('.','_');
        String classpath = pkg+".common.MTCommonEntryPoint"+version;
        //Sync the version entry class to the context class loader
        ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        ClassHelper.syncSourcesAndLoadClass(systemLoader,contextLoader,classpath);
        try {
            Class<CommonEntryPoint> clazz = (Class<CommonEntryPoint>)ClassHelper.findClass(classpath,contextLoader);
            MTRef.logInfo("Successfully located versioned entrypoint {} using loader {}",clazz,contextLoader);
            return clazz;
        } catch(ClassCastException ex) {
            MTRef.logError("Classpath `{}` is not an entrypoint!",classpath,ex);
            return null;
        }
    }

    @Override
    protected String getModID() {
        return MODID;
    }

    @Override
    protected String getModName() {
        return NAME;
    }

    @Override
    public void onConstructed() {
        File configDir = new File(CONFIG_PATH);
        if(!configDir.exists() && !configDir.mkdirs())
            throw new RuntimeException("Unable to create file directory at "+CONFIG_PATH+"! Music Triggers "+
                    "is unable to load any further.");
        MTNetwork.initCommon();
        distributeHook(CommonEntryPoint::onConstructed);
    }

    @Override
    public void onPreRegistration() {
        MTRegistryHandler.init();
        MTServerEvents.init();
        distributeHook(CommonEntryPoint::onPreRegistration);
    }
    
    @Override
    public void onLoadComplete() {
        distributeHook(CommonEntryPoint::onLoadComplete);
    }
}
