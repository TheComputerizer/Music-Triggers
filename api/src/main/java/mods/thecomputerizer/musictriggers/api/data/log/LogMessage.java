package mods.thecomputerizer.musictriggers.api.data.log;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorCache;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.io.LogHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.io.LogHelper.ModLogger;
import org.apache.logging.log4j.Level;

public class LogMessage {

    private final Level level;
    private final String msg;
    private final Object[] args;

    public LogMessage(Level level, String msg, Object ... args) {
        this.level = level;
        this.msg = msg;
        this.args = args;
    }

    public ColorCache getColor() {
        switch(this.level.name()) {
            case "DEBUG": return ColorHelper.GRAY;
            case "ERROR": return ColorHelper.RED;
            case "FATAL": return ColorHelper.DARK_RED;
            case "TRACE": return ColorHelper.DARK_GRAY;
            case "WARN": return ColorHelper.GOLD;
            default: return ColorHelper.WHITE;
        }
    }

    public String getDisplay() {
        return LogHelper.injectParameters("["+getLevelName()+"] "+this.msg,args);
    }

    public String getLevelName() {
        return String.format("%-5s",level.name());
    }

    public LogMessage log(ModLogger logger) {
        MTRef.log(this.level,this.msg,this.args);
        logger.log(this.level,this.msg,this.args);
        return this;
    }
}