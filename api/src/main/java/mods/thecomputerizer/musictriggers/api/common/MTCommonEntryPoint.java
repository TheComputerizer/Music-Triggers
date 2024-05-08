package mods.thecomputerizer.musictriggers.api.common;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.client.MTClientEntryPoint;
import mods.thecomputerizer.musictriggers.api.network.MTNetwork;
import mods.thecomputerizer.musictriggers.api.server.MTServerEvents;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.common.CommonEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.core.CoreAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.core.loader.MultiVersionMod;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings("unused")
@MultiVersionMod(modid = MTRef.MODID, modName = MTRef.NAME, modVersion = MTRef.VERSION)
public class MTCommonEntryPoint extends CommonEntryPoint {
    
    private static final Class<CommonEntryPoint> versionClass = findVersionEntryClass(CoreAPI.INSTANCE);
    
    @SuppressWarnings("unchecked")
    private static Class<CommonEntryPoint> findVersionEntryClass(CoreAPI instance) {
        String pkg = "mods.thecomputerizer.musictriggers.";
        switch(instance.getModLoader()) {
            case FABRIC: {
                pkg+="fabric.";
                break;
            }
            case FORGE: {
                pkg+="forge.";
                break;
            }
            case LEGACY: {
                pkg+="legacy.";
                break;
            }
            case NEOFORGE: {
                pkg+="neoforge.";
                break;
            }
        }
        String cls = "MTCommonEntryPoint1_";
        switch(instance.getVersion()) {
            case V12: {
                pkg+="v12.m2";
                cls+="12_2";
                break;
            }
            case V16: {
                pkg+="v16.m5";
                cls+="16_5";
                break;
            }
            case V18: {
                pkg+="v18.m2";
                cls+="18_2";
                break;
            }
            case V19: { //TODO Support multiple minor versions?
                pkg+="v19";
                cls+="19";
                break;
            }
            case V20: {
                pkg+="v20";
                cls+="20";
                break;
            }
            case V21: {
                pkg+="v21";
                cls+="21";
                break;
            }
        }
        String classpath = pkg+".common."+cls;
        try {
            Class<CommonEntryPoint> clazz = (Class<CommonEntryPoint>)Class.forName(classpath);
            MTRef.logInfo("Successfully located versioned entrypoint {}",clazz);
            return clazz;
        } catch(ClassNotFoundException | ClassCastException ex) {
            if(ex instanceof ClassCastException) MTRef.logError("Classpath `{}` is not an entrypoint!",classpath,ex);
            else MTRef.logError("Unable to locate version specific entrypoint classpath {}",classpath,ex);
            return null;
        }
    }
    
    final CommonEntryPoint versionInstance;
    
    public MTCommonEntryPoint() {
        CommonEntryPoint instance = null;
        if(Objects.nonNull(versionClass)) {
            try {
                instance = versionClass.newInstance();
            } catch(ReflectiveOperationException ex) {
                MTRef.logFatal("Unable to instantiate versioned instance!",ex);
            }
        }
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

    @Override
    protected String getModID() {
        return MTRef.MODID;
    }

    @Override
    protected String getModName() {
        return MTRef.NAME;
    }

    @Override
    public void onConstructed() {
        File configDir = new File(MTRef.CONFIG_PATH);
        if(!configDir.exists() && !configDir.mkdirs())
            throw new RuntimeException("Unable to create file directory at "+MTRef.CONFIG_PATH+"! Music Triggers "+
                    "is unable to load any further.");
        MTNetwork.initCommon();
        distributeHook(CommonEntryPoint::onConstructed);
    }

    @Override
    public void onPreRegistration() {
        MTServerEvents.init();
        distributeHook(CommonEntryPoint::onPreRegistration);
    }
    
    @Override
    public void onLoadComplete() {
        distributeHook(CommonEntryPoint::onLoadComplete);
    }
}
