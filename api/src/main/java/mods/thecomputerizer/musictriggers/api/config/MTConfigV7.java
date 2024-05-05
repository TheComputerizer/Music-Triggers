package mods.thecomputerizer.musictriggers.api.config;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml.TomlEntry;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.TomlRemapper;

import javax.annotation.Nullable;

public class MTConfigV7 extends ConfigVersion {
    
    public static final MTConfigV7 V7_0_0_BETA_1 = new MTConfigV7(0,0,"beta",1){};
    
    protected MTConfigV7(int major, int minor) {
        super(7,major,minor);
    }
    
    protected MTConfigV7(int major, int minor, String qualifierName, int qualifierBuild) {
        super(7,major,minor,qualifierName,qualifierBuild);
    }
    
    @Override public String getPathMain(Toml channel) {
        return channel.getOrSetValue("main",channel.getName()+"/main"); //6.3.1 Didn't write the default parameters
    }
    
    @Override public Toml getGlobal() {
        return ChannelHelper.openToml(MTRef.GLOBAL_CONFIG,this);
    }
    
    @Override public @Nullable TomlRemapper getRemapper(TableRef ref) {
        switch(ref.getName()) {
            case "audio": return new TomlRemapper() {
                @Nullable @Override public TomlRemapper getNextRemapper(String table) {
                    return null;
                }
                
                @Override public String remapTable(String name) {
                    return name;
                }
                
                @Override public TomlEntry<?> remapEntry(Toml parent, TomlEntry<?> entry) {
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
                    return remapChannelInfoEntry(entry);
                }
            };
            case "channels":
            case "global":
            case "main":
                return new ConfigRemapper(ref) {
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
            case "trigger": return new TomlRemapper() {
                @Nullable @Override public TomlRemapper getNextRemapper(String table) {
                    return null;
                }
                
                @Override public String remapTable(String name) {
                    return name;
                }
                
                @Override public TomlEntry<?> remapEntry(Toml parent, TomlEntry<?> entry) {
                    return remapTriggerEntry(parent.getName(),entry);
                }
            };
            case "triggers": return new TomlRemapper() {
                @Nullable @Override public TomlRemapper getNextRemapper(String table) {
                    return null;
                }
                
                @Override public String remapTable(String name) {
                    return remapTriggerName(name);
                }
                
                @Override public TomlEntry<?> remapEntry(Toml parent, TomlEntry<?> entry) {
                    return entry;
                }
            };
            default: return null;
        }
    }
    
    @Override public Toml getRenders(Toml channel) {
        return ChannelHelper.openToml(channel.getValueString("renders"),this);
    }
    
    @Override public Toml getToggles(Toml global) {
        return ChannelHelper.openToml(global.getValueString("toggles_path"),this);
    }
    
    @Override public ConfigVersion getVersionTarget() {
        return this;
    }
    
    @Override public TomlEntry<?> remapAudioEntry(TomlEntry<?> entry) {
        return entry;
    }
    
    @Override public TomlEntry<?> remapChannelInfoEntry(TomlEntry<?> entry) {
        return entry;
    }
    
    @Override public TomlEntry<?> remapDebugEntry(TomlEntry<?> entry) {
        return entry;
    }
    
    @Override public TomlEntry<?> remapTriggerEntry(String name, TomlEntry<?> entry) {
        return entry;
    }
    
    @Override public String remapTriggerName(String name) {
        return name;
    }
    
    @Nullable @Override public Toml upgradeToTable(TomlEntry<?> entry) {
        return null;
    }
}
