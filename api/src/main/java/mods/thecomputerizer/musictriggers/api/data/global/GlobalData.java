package mods.thecomputerizer.musictriggers.api.data.global;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.log.MTLogger;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.TomlWritingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.Objects;

@Getter
public class GlobalData implements LoggableAPI {

    private Toml global;
    private Debug debug;
    private String toggles = "";

    private @Nullable String getStringOrNull(Toml toml, String name) {
        String val = toml.getValueString(name);
        return StringUtils.isNotBlank(val) ? val : null;
    }

    public ChannelHelper initHelper(boolean isClient) throws TomlWritingException {
        ChannelHelper helper = new ChannelHelper(isClient);
        helper.load(this.global);
        return helper;
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

    public @Nullable Toml openToggles(String path) {
        return StringUtils.isNotBlank(this.toggles) ?
                ChannelHelper.openToml(path+"/"+this.toggles,true,this) : null;
    }

    public void parse(@Nullable Toml global) throws TomlWritingException {
        if(Objects.nonNull(global)) {
            logInfo("Parsing global data");
            readDebug(global);
            this.toggles = global.getEntry("toggles_path").getValue().toString();
        }
        this.global = global;
    }

    public void readDebug(Toml global) {
        Debug debug = new Debug();
        if(debug.parse(global.getTable("debug"))) this.debug = debug;
    }
}