package mods.thecomputerizer.musictriggers.api.data.global;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.log.MTLogger;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
public class GlobalData implements LoggableAPI {

    private final Set<Toggle> toggles;
    private Debug debug;
    private Registration registration;

    public GlobalData() {
        this.toggles = new HashSet<>();
    }

    public void parse() {
        readDebug(ChannelHelper.openToml(MTRef.CONFIG_PATH+"/debug",this));
        readRegistration(ChannelHelper.openToml(MTRef.CONFIG_PATH+"/registration",this));
        readToggles(ChannelHelper.openToml(MTRef.CONFIG_PATH+"/toggles",this));
    }

    public void readDebug(@Nullable Holder debugHolder) {
        if(Objects.isNull(debugHolder)) return;
        Debug debug = new Debug();
        if(debug.parse(debugHolder)) this.debug = debug;
    }

    public void readRegistration(@Nullable Holder regHolder) {
        if(Objects.isNull(regHolder)) return;
        Registration registration = new Registration();
        if(registration.parse(regHolder)) this.registration = registration;
    }

    public void readToggles(@Nullable Holder togglesHolder) {
        if(Objects.isNull(togglesHolder)) return;
        for(Table table : togglesHolder.getTablesByName("toggle")) {
            Toggle toggle = new Toggle();
            if(toggle.parse(table)) this.toggles.add(toggle);
        }
    }

    @Override
    public void logAll(String msg, Object ... args) {
        MTLogger.log("Global","Data",Level.ALL,msg,args);
    }

    @Override
    public void logDebug(String msg, Object ... args) {
        MTLogger.log("Global","Data",Level.DEBUG,msg,args);
    }

    @Override
    public void logError(String msg, Object ... args) {
        MTLogger.log("Global","Data",Level.ERROR,msg,args);
    }

    @Override
    public void logFatal(String msg, Object ... args) {
        MTLogger.log("Global","Data",Level.FATAL,msg,args);
    }

    @Override
    public void logInfo(String msg, Object ... args) {
        MTLogger.log("Global","Data",Level.INFO,msg,args);
    }

    @Override
    public void logTrace(String msg, Object ... args) {
        MTLogger.log("Global","Data",Level.TRACE,msg,args);
    }

    @Override
    public void logWarn(String msg, Object ... args) {
        MTLogger.log("Global","Data",Level.WARN,msg,args);
    }
}