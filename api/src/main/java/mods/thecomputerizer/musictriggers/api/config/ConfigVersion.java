package mods.thecomputerizer.musictriggers.api.config;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.log.MTLogger;
import org.apache.logging.log4j.Level;

import java.util.Map;
import java.util.Objects;

@Getter
public abstract class ConfigVersion implements LoggableAPI {
    
    protected final Version version;
    
    protected ConfigVersion(int release, int major, int minor) {
        this(new Version(release,major,minor));
    }
    
    protected ConfigVersion(int release, int major, int minor, String qualifierName, int qualifierBuild) {
        this(release,major,minor,new Qualifier(qualifierName,qualifierBuild));
    }
    
    protected ConfigVersion(int release, int major, int minor, Qualifier qualifier) {
        this(new Version(release,major,minor,qualifier));
    }
    
    protected ConfigVersion(Version version) {
        this.version = version;
    }
    
    public abstract ConfigVersion getTarget();
    
    public boolean hasCloserQualiferThan(ConfigVersion version, int closest) {
        return hasCloserQualiferThan(version.version,closest);
    }
    
    public boolean hasCloserQualiferThan(Version version, int closest) {
        return this.version.hasCloserQualiferThan(version.qualifier.name,version.qualifier.build,closest);
    }
    
    public void logAll(String msg, Object ...args) {
        MTLogger.log("ConfigMapper",toString(),Level.ALL,msg,args);
    }
    
    public void logDebug(String msg, Object ...args) {
        MTLogger.log("ConfigMapper",toString(),Level.DEBUG,msg,args);
    }
    
    public void logError(String msg, Object ...args) {
        MTLogger.log("ConfigMapper",toString(),Level.ERROR,msg,args);
    }
    
    public void logFatal(String msg, Object ...args) {
        MTLogger.log("ConfigMapper",toString(),Level.FATAL,msg,args);
    }
    
    public void logInfo(String msg, Object ...args) {
        MTLogger.log("ConfigMapper",toString(),Level.INFO,msg,args);
    }
    
    public void logTrace(String msg, Object ...args) {
        MTLogger.log("ConfigMapper",toString(),Level.TRACE,msg,args);
    }
    
    public void logWarn(String msg, Object ...args) {
        MTLogger.log("ConfigMapper",toString(),Level.WARN,msg,args);
    }
    
    public abstract MTConfigGlobal initGlobalMappers();
    
    public boolean similar(Version version) {
        return this.version.similar(version);
    }
    
    @Getter
    public static final class Version {
        
        private final int release;
        private final int major;
        private final int minor;
        private final Qualifier qualifier;
        
        public Version(int release, int major, int minor) {
            this(release,major,minor,null);
        }
        
        public Version(int release, int major, int minor, Qualifier qualifier) {
            this.release = release;
            this.major = major;
            this.minor = minor;
            this.qualifier = qualifier;
        }
        
        @Override
        public boolean equals(Object other) {
            if(other instanceof Version) {
                Version version = (Version)other;
                return this.release==version.release && this.major==version.major && this.minor==version.minor &&
                       this.qualifier.equals(version.qualifier);
            }
            return false;
        }
        
        public boolean similar(Version version) {
            return this.release==version.release && this.major==version.major && this.minor==version.minor;
        }
        
        public boolean hasCloserQualiferThan(String qualifierName, int qualiferBuild, int closest) {
            return this.qualifier.isCloserThan(qualifierName,qualiferBuild,closest);
        }
        
        @Override
        public String toString() {
            String str = this.release+"."+this.major+"."+this.minor;
            return Objects.nonNull(this.qualifier) ? str+this.qualifier : str;
        }
    }
    
    @Getter
    public static final class Qualifier {
        
        private final String name;
        private final int build;
        
        public Qualifier(String name, int build) {
            this.name = name;
            this.build = build;
        }
        
        @Override
        public boolean equals(Object other) {
            if(other instanceof Qualifier) {
                Qualifier qual = (Qualifier)other;
                return this.name.equals(qual.name) && this.build==qual.build;
            }
            return false;
        }
        
        public boolean isCloserThan(String name, int build, int closest) {
            return this.name.equals(name) && this.build<=build && build-this.build<closest;
        }
        
        @Override
        public String toString() {
            return "+"+this.name+"-"+this.build;
        }
    }
}