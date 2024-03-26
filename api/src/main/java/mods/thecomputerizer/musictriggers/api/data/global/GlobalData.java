package mods.thecomputerizer.musictriggers.api.data.global;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.log.MTLogger;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.Objects;

public class GlobalData implements LoggableAPI {

    @Getter private Holder holder;
    @Getter private Debug debug;
    @Getter private Registration registration;
    @Getter private String toggles = "";
    //TODO These should probably be encoded or something
    private String email = null;
    private String password = null;

    private @Nullable String getStringOrNull(Holder holder, String name) {
        String val = holder.getValOrDefault(name,"");
        return StringUtils.isNotBlank(val) ? val : null;
    }

    public ChannelHelper initHelper(String playerID, boolean isClient) {
        ChannelHelper helper = new ChannelHelper(playerID,isClient,this.email,this.password);
        helper.init(this.holder);
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

    public @Nullable Holder openToggles(String path) {
        return StringUtils.isNotBlank(this.toggles) ? ChannelHelper.openToml(path+this.toggles,this) : null;
    }

    public void parse(@Nullable Holder holder) { //TODO Fix bad plaintext email & password
        if(Objects.nonNull(holder)) {
            readDebug(holder.getTableByName("debug"));
            readRegistration(holder.getTableByName("registration"));
            this.toggles = holder.getValOrDefault("toggles_path","");
            this.email = getStringOrNull(holder,"youtube_email");
            this.password = getStringOrNull(holder,"youtube_password");
        }
        this.holder = holder;
    }

    public void readDebug(@Nullable Table table) {
        if(Objects.isNull(table)) return;
        Debug debug = new Debug();
        if(debug.parse(table)) this.debug = debug;
    }

    public void readRegistration(@Nullable Table table) {
        if(Objects.isNull(table)) return;
        Registration registration = new Registration();
        if(registration.parse(table)) this.registration = registration;
    }
}