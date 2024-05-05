package mods.thecomputerizer.musictriggers.api.data.global;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.log.MTLogger;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml.TomlEntry;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.TomlWritingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.Objects;

public class GlobalData implements LoggableAPI {

    @Getter private Toml toml;
    @Getter private Debug debug;
    @Getter private String toggles = "";
    private boolean writable; //TODO Replaced by config remapping

    private @Nullable String getStringOrNull(Toml toml, String name) {
        String val = toml.getValueString(name);
        return StringUtils.isNotBlank(val) ? val : null;
    }

    public ChannelHelper initHelper(String playerID, boolean isClient) throws TomlWritingException {
        ChannelHelper helper = new ChannelHelper(playerID,isClient);
        helper.load(this.toml);
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
        return StringUtils.isNotBlank(this.toggles) ? ChannelHelper.openToml(path+"/"+this.toggles,this) : null;
    }

    public void parse(@Nullable Toml holder) throws TomlWritingException {
        if(Objects.nonNull(holder)) {
            readDebug(holder);
            TomlEntry<?> entry;
            if(!holder.hasEntry("toggles_path")) {
                entry = holder.addEntry("toggles_path","toggles");
                markWritable();
            } else entry = holder.getEntry("toggles_path");
            this.toggles = entry.getValue().toString();
        }
        this.toml = holder;
    }

    public void readDebug(Toml holder) throws TomlWritingException {
        Debug debug = new Debug();
        if(!holder.hasTable("debug")) {
            debug.writeDefault(holder);
            markWritable();
        }
        if(debug.parse(holder.getTable("debug"))) this.debug = debug;
    }

    public void markWritable() {
        this.writable = true;
    }

    public void write() {
        //if(this.writable && Objects.nonNull(this.holder))
            //FileHelper.writeLines(MTRef.GLOBAL_CONFIG+".toml",this.holder.toLines(),false);
    }
}