package mods.thecomputerizer.musictriggers.api.config;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.log.MTLogger;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerRegistry;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml.TomlEntry;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.TomlRemapper;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnusedReturnValue") @Getter
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
    
    public List<String> getHeaderLines(String name) {
        switch(name) {
            case "commands": return Arrays.asList(" Commands header!"," Line 2");
            case "global": return Arrays.asList(" Global header!"," Line 2");
            case "jukebox": return Arrays.asList(" Jukebox header!"," Line 2");
            case "main": return Arrays.asList(" Main header!"," Line 2");
            case "redirect": return Arrays.asList(" Redirect header!"," Line 2");
            case "renders": return Arrays.asList(" Renders header!"," Line 2");
            case "toggles": return Arrays.asList(" Toggles header!"," Line 2");
            default: return Collections.emptyList();
        }
    }
    
    public abstract Toml getGlobal();
    public abstract String getPathMain(Toml channel);
    
    public @Nullable TomlRemapper getRemapper(@Nullable TableRef ref) { //TODO Make a ParameterRemapper class or something to consolidate stuff
        if(Objects.isNull(ref)) return null;
        String name = ref.getName();
        switch(name) {
            case "audio":
            case "universal_audio": return new TomlRemapper() {
                @Nullable @Override public TomlRemapper getNextRemapper(String table) {
                    return null;
                }
                @Override public String remapTable(String name) {
                    return name;
                }
                @Override public TomlEntry<?> remapEntry(Toml parent, TomlEntry<?> entry) {
                    entry = remapAudioEntry(entry);
                    Toml table = upgradeToTable(ref,entry);
                    if(Objects.nonNull(table)) {
                        parent.addTable(entry.getKey(),table);
                        return null;
                    }
                    return entry;
                }
            };
            case "channel": return new TomlRemapper() {
                @Nullable @Override public TomlRemapper getNextRemapper(String table) {
                    return null;
                }
                @Override public String remapTable(String name) {
                    return name;
                }
                @Override public TomlEntry<?> remapEntry(Toml toml, TomlEntry<?> entry) {
                    return remapChannelInfoEntry(toml.getName(),entry);
                }
            };
            case "channels": return new TomlRemapper() {
                @Nullable @Override public TomlRemapper getNextRemapper(String table) {
                    return getRemapper(MTDataRef.CHANNEL_INFO);
                }
                @Override public String remapTable(String name) {
                    return name;
                }
                @Override public TomlEntry<?> remapEntry(Toml parent, TomlEntry<?> entry) {
                    return entry;
                }
            };
            case "link": return new TomlRemapper() {
                @Nullable @Override public TomlRemapper getNextRemapper(String table) {
                    return null;
                }
                @Override public String remapTable(String name) {
                    return name;
                }
                @Override public TomlEntry<?> remapEntry(Toml parent, TomlEntry<?> entry) {
                    return remapLinkEntry(parent.getParent().getName(),entry);
                }
            };
            case "from": return new TomlRemapper() {
                @Nullable @Override public TomlRemapper getNextRemapper(String name) {
                    return null;
                }
                @Override public String remapTable(String table) {
                    return table;
                }
                @Override public TomlEntry<?> remapEntry(Toml parent, TomlEntry<?> entry) {
                    entry = remapToggleFrom(entry);
                    Toml table = upgradeToTable(ref,entry);
                    if(Objects.nonNull(table)) {
                        parent.addTable(entry.getKey(),table);
                        return null;
                    }
                    return entry;
                }
            };
            case "global":
            case "main":
            case "toggles": return new ConfigRemapper(ref) {
                @Override public TomlRemapper getNextRemapper(TableRef next) {
                    return getRemapper(next);
                }
            };
            case "debug": return new TomlRemapper() {
                @Nullable @Override public TomlRemapper getNextRemapper(String table) {
                    return null;
                }
                @Override public String remapTable(String name) {
                    return name;
                }
                @Override public TomlEntry<?> remapEntry(Toml parent, TomlEntry<?> entry) {
                    return remapDebugEntry(entry);
                }
            };
            case "songs": return new TomlRemapper() {
                @Nullable @Override public TomlRemapper getNextRemapper(String table) {
                    return getRemapper("universal".equals(table) ? MTDataRef.UNIVERSAL_AUDIO : MTDataRef.AUDIO);
                }
                @Override public String remapTable(String name) {
                    return name;
                }
                @Override public TomlEntry<?> remapEntry(Toml toml, TomlEntry<?> entry) {
                    return entry;
                }
            };
            case "to": return new TomlRemapper() {
                @Nullable @Override public TomlRemapper getNextRemapper(String name) {
                    return null;
                }
                @Override public String remapTable(String table) {
                    return table;
                }
                @Override public TomlEntry<?> remapEntry(Toml parent, TomlEntry<?> entry) {
                    entry = remapToggleTo(entry);
                    Toml table = upgradeToTable(ref,entry);
                    if(Objects.nonNull(table)) {
                        parent.addTable(entry.getKey(),table);
                        return null;
                    }
                    return entry;
                }
            };
            case "triggers": return new TomlRemapper() {
                @Nullable @Override public TomlRemapper getNextRemapper(String table) {
                    return getRemapper("universal".equals(table) ? MTDataRef.UNIVERSAL_TRIGGERS : ref.findChild(table));
                }
                @Override public String remapTable(String name) {
                    return remapTriggerName(name);
                }
                @Override public TomlEntry<?> remapEntry(Toml parent, TomlEntry<?> entry) {
                    return entry;
                }
            };
            default: {
                if(name.equals("universal_triggers") || TriggerRegistry.isRegistred(name)) {
                    return new TomlRemapper() {
                        @Nullable @Override public TomlRemapper getNextRemapper(String name) {
                            return getRemapper(ref.findChild(name));
                        }
                        @Override public String remapTable(String name) {
                            return name;
                        }
                        @Override public TomlEntry<?> remapEntry(Toml parent, TomlEntry<?> entry) {
                            return remapTriggerEntry(parent.getName(),entry);
                        }
                    };
                }
                return null;
            }
        }
    }
    
    public abstract Toml getRenders(Toml channel);
    public abstract Toml getToggles(Toml global);
    public abstract ConfigVersion getVersionTarget();
    
    public boolean hasCloserQualiferThan(ConfigVersion version, int closest) {
        return hasCloserQualiferThan(version.version,closest);
    }
    
    public boolean hasCloserQualiferThan(Version version, int closest) {
        return this.version.hasCloserQualiferThan(version.qualifier.name,version.qualifier.build,closest);
    }
    
    public void logAll(String msg, Object ...args) {
        MTLogger.log("ConfigMapper",this.version.toString(),Level.ALL,msg,args);
    }
    
    public void logDebug(String msg, Object ...args) {
        MTLogger.log("ConfigMapper",this.version.toString(),Level.DEBUG,msg,args);
    }
    
    public void logError(String msg, Object ...args) {
        MTLogger.log("ConfigMapper",this.version.toString(),Level.ERROR,msg,args);
    }
    
    public void logFatal(String msg, Object ...args) {
        MTLogger.log("ConfigMapper",this.version.toString(),Level.FATAL,msg,args);
    }
    
    public void logInfo(String msg, Object ...args) {
        MTLogger.log("ConfigMapper",this.version.toString(),Level.INFO,msg,args);
    }
    
    public void logTrace(String msg, Object ...args) {
        MTLogger.log("ConfigMapper",this.version.toString(),Level.TRACE,msg,args);
    }
    
    public void logWarn(String msg, Object ...args) {
        MTLogger.log("ConfigMapper",this.version.toString(),Level.WARN,msg,args);
    }
    
    public void remap() {
        ConfigVersion target = getVersionTarget();
        if(Objects.isNull(target) || this==target) {
            logInfo("Config version is up to date");
            return;
        }
        logInfo("Remapping from {} to target {}",this.version,getVersionTarget().version);
        Toml global = getGlobal();
        if(Objects.nonNull(global)) {
            getToggles(global); //Delete extra toggles files if needed
            Toml channels = global.getTable("channels");
            if(Objects.nonNull(channels)) {
                logInfo("Remapping channels");
                for(Toml channel : channels.getAllTables()) {
                    getRenders(channel); //Rename renders files if needed
                    verifyJukebox(channel);
                    verifyRedirct(channel);
                    logInfo("Remapping channel "+channel.getName());
                    String mainPath = getPathMain(channel);
                    Toml main = ChannelHelper.openToml(mainPath,false,this);
                    if(Objects.nonNull(main)) writeIfRemapped(main,MTDataRef.FILE_MAP.get("main"),mainPath);
                }
            }
            logInfo("Remapping global configs");
            writeIfRemapped(global,MTDataRef.FILE_MAP.get("global"),MTRef.GLOBAL_CONFIG);
        }
        logInfo("Successfully remapped config files!");
    }
    
    public abstract TomlEntry<?> remapAudioEntry(TomlEntry<?> entry);
    public abstract TomlEntry<?> remapChannelInfoEntry(String channel, TomlEntry<?> entry);
    public abstract TomlEntry<?> remapDebugEntry(TomlEntry<?> entry);
    public abstract TomlEntry<?> remapLinkEntry(String trigger, TomlEntry<?> entry);
    public abstract TomlEntry<?> remapToggleFrom(TomlEntry<?> entry);
    public abstract TomlEntry<?> remapToggleTo(TomlEntry<?> entry);
    public abstract TomlEntry<?> remapTriggerEntry(String name, TomlEntry<?> entry);
    public abstract String remapTriggerName(String name);
    public abstract @Nullable Toml upgradeToTable(TableRef ref, TomlEntry<?> entry);
    public abstract void verifyJukebox(Toml channel);
    public abstract void verifyRedirct(Toml channel);
    
    public boolean similar(Version version) {
        return this.version.similar(version);
    }
    
    protected void writeIfRemapped(Toml toml, TableRef ref, String path) {
        TomlRemapper remapper = getRemapper(ref);
        if(Objects.nonNull(remapper) && remapper.remap(toml)) {
            logInfo("Writing to {}",path);
            toml.clearComments();
            toml.addComments(getHeaderLines(ref.getName()));
            MTDataRef.writeToFile(toml,path);
        }
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