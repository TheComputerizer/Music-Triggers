package mods.thecomputerizer.musictriggers.api.config;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerRegistry;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml.TomlEntry;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.TomlRemapper;

import javax.annotation.Nullable;
import java.util.Objects;

public class MTConfigV7 extends ConfigVersion {
    
    public static final MTConfigV7 V7_0_0_BETA_1 = new MTConfigV7(0,0,"beta",1){};
    
    protected MTConfigV7(int major, int minor) {
        super(7,major,minor);
    }
    
    protected MTConfigV7(int major, int minor, String qualifierName, int qualifierBuild) {
        super(7,major,minor,qualifierName,qualifierBuild);
    }
    
    @Override public String getPathMain(Toml channel) {
        return MTRef.CONFIG_PATH+"/"+channel.getName()+"/"+channel.getOrSetValue("main","main");
    }
    
    @Override public Toml getGlobal() {
        return ChannelHelper.openToml(MTRef.GLOBAL_CONFIG,false,this);
    }
    
    @Override public @Nullable TomlRemapper getRemapper(TableRef ref) { //TODO Share common V6 and V7 remappers
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
    
    @Override public Toml getRenders(Toml channel) {
        String path = MTRef.CONFIG_PATH+"/"+channel.getName()+"/"+channel.getValueString("renders");
        return ChannelHelper.openToml(path,false,this);
    }
    
    @Override public Toml getToggles(Toml global) {
        String path = MTRef.CONFIG_PATH+"/"+global.getValueString("toggles_path");
        return ChannelHelper.openToml(path,false,this);
    }
    
    @Override public ConfigVersion getVersionTarget() {
        return this;
    }
    
    @Override public TomlEntry<?> remapAudioEntry(TomlEntry<?> entry) {
        return entry;
    }
    
    @Override public TomlEntry<?> remapChannelInfoEntry(String channel, TomlEntry<?> entry) {
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
    
    @Override public void verifyJukebox(Toml channel) {
    
    }
    
    @Override public void verifyRedirct(Toml channel) {
    
    }
}
