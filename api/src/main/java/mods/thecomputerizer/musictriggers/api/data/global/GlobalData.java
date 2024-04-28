package mods.thecomputerizer.musictriggers.api.data.global;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.log.MTLogger;
import mods.thecomputerizer.theimpossiblelibrary.api.io.FileHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.IndexFinder;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Variable;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.Objects;

public class GlobalData implements LoggableAPI {

    @Getter private Holder holder;
    @Getter private Debug debug;
    @Getter private Registration registration;
    @Getter private String toggles = "";
    private boolean writable;
    //TODO These should probably be encoded or something
    private String email = null;
    private String password = null;

    private @Nullable String getStringOrNull(Holder holder, String name) {
        String val = holder.getValOrDefault(name,"");
        return StringUtils.isNotBlank(val) ? val : null;
    }

    public ChannelHelper initHelper(String playerID, boolean isClient) {
        ChannelHelper helper = new ChannelHelper(playerID,isClient,this.email,this.password);
        helper.load(this.holder);
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
        return StringUtils.isNotBlank(this.toggles) ? ChannelHelper.openToml(path+"/"+this.toggles,this) : null;
    }

    public void parse(@Nullable Holder holder) { //TODO Fix bad plaintext email & password
        if(Objects.nonNull(holder)) {
            readDebug(holder);
            readRegistration(holder);
            Variable var;
            if(!holder.hasVar("toggles_path")) {
                var = holder.addVariable(null,"toggles_path","toggles",new IndexFinder(null));
                holder.andBlank(1,new IndexFinder(null,var,1));
                markWritable();
            } else var = holder.getOrCreateVar(null,"toggles_path","toggles");
            this.toggles = var.get().toString();
            this.email = getStringOrNull(holder,"youtube_email");
            this.password = getStringOrNull(holder,"youtube_password");
        }
        this.holder = holder;
    }

    public void readDebug(Holder holder) {
        Debug debug = new Debug();
        if(!holder.hasTable("debug")) {
            debug.writeDefault(holder);
            markWritable();
        }
        if(debug.parse(holder.getTableByName("debug"))) this.debug = debug;
    }

    public void readRegistration(Holder holder) {
        Registration registration = new Registration();
        if(!holder.hasTable("registration")) {
            registration.writeDefault(holder);
            markWritable();
        }
        if(registration.parse(holder.getTableByName("registration"))) this.registration = registration;
    }

    public void markWritable() {
        this.writable = true;
    }

    public void write() {
        //if(this.writable && Objects.nonNull(this.holder))
            //FileHelper.writeLines(MTRef.GLOBAL_CONFIG+".toml",this.holder.toLines(),false);
    }
}