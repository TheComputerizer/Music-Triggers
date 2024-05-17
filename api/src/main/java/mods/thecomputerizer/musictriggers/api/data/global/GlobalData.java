package mods.thecomputerizer.musictriggers.api.data.global;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.log.MTLogger;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.network.MessageInitChannels;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Objects;

import static mods.thecomputerizer.musictriggers.api.data.MTDataRef.GLOBAL;
import static org.apache.logging.log4j.Level.*;

@Getter
public class GlobalData extends ParameterWrapper {
    
    private final Debug debug;
    private Toml global;
    private String togglesPath;
    
    public GlobalData() {
        super("Data");
        this.debug = new Debug();
    }
    
    public void close() {}
    
    public ChannelHelper loadFromInit(MessageInitChannels<?> init) {
        ChannelHelper helper = new ChannelHelper(init.getUuid(),init.isClient());
        this.global = init.getGlobal();
        helper.loadFromInit(init);
        return helper;
    }
    
    @Override protected String getLogPrefix() {
        return "Global";
    }
    
    @Override protected TableRef getReferenceData() {
        return GLOBAL;
    }
    
    @Override public Class<? extends ParameterWrapper> getTypeClass() {
        return null;
    }
    
    @Override public void logDebug(String msg, Object ... args) {
        MTLogger.log("Global","Data",DEBUG,msg,args);
    }

    @Override public void logError(String msg, Object ... args) {
        MTLogger.log("Global","Data",ERROR,msg,args);
    }

    @Override public void logFatal(String msg, Object ... args) {
        MTLogger.log("Global","Data",FATAL,msg,args);
    }

    @Override public void logInfo(String msg, Object ... args) {
        MTLogger.log("Global","Data",INFO,msg,args);
    }

    @Override public void logTrace(String msg, Object ... args) {
        MTLogger.log("Global","Data",TRACE,msg,args);
    }

    @Override public void logWarn(String msg, Object ... args) {
        MTLogger.log("Global","Data",WARN,msg,args);
    }

    public @Nullable Toml openToggles() {
        return StringUtils.isNotBlank(this.togglesPath) ?
                ChannelHelper.openToml(this.togglesPath,true,this) : null;
    }

    public boolean parse(@Nullable Toml global) {
        this.global = global;
        if(Objects.nonNull(global)) {
            logInfo("Parsing global data");
            if(!global.hasTable("debug")) logError("Missing debug table!");
            else if(!this.debug.parse(global.getTable("debug"))) logError("Failed to parse debug parameters");
            this.togglesPath = MTRef.CONFIG_PATH+"/"+(
                    super.parse(global) ? getParameterAsString("toggles_path") : "toggles");
            return true;
        }
        logError("Tried to parse missing globals file");
        return false;
    }
    
    public void parseToggles(ChannelHelper helper) {
        parseToggles(helper,openToggles());
    }
    
    public void parseToggles(ChannelHelper helper, @Nullable Toml toggles) {
        if(Objects.isNull(toggles)) {
            logError("Tried to parse missing toggles file");
            return;
        }
        if(toggles.hasTable("toggle"))  {
            for(Toml table : toggles.getTableArray("toggle")) {
                Toggle toggle = new Toggle(helper,table);
                if(toggle.parse()) helper.getToggles().add(toggle);
            }
        }
    }
}