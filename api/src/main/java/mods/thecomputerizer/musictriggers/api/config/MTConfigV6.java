package mods.thecomputerizer.musictriggers.api.config;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.io.FileHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml.TomlEntry;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.TomlRemapper;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Objects;

/**
 Assumes 6.3.1 since V6 didn't have config versioning
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class MTConfigV6 extends ConfigVersion {
    
    public static final MTConfigV6 V6_3_1 = new MTConfigV6(3,1);
    
    protected MTConfigV6(int major, int minor) {
        super(6,major,minor);
    }
    
    protected MTConfigV6(int major, int minor, String qualifierName, int qualifierBuild) {
        super(6,major,minor,qualifierName,qualifierBuild);
    }
    
    @Override public Toml getGlobal() {
        Toml global = Toml.getEmpty();
        Toml debug = ChannelHelper.openToml(MTRef.CONFIG_PATH+"/debug",this);
        if(Objects.nonNull(debug)) {
            Toml registration = ChannelHelper.openToml(MTRef.CONFIG_PATH+"/registration", this);
            if(Objects.nonNull(registration)) {
                TomlEntry<?> entry = registration.getEntry("CLIENT_SIDE_ONLY");
                if(Objects.nonNull(entry)) debug.addEntry(entry.getKey(),entry.getValue());
                entry = registration.getEntry("REGISTER_DISCS");
                if(Objects.nonNull(entry)) debug.addEntry(entry.getKey(),entry.getValue());
                FileHelper.get(MTRef.CONFIG_PATH+"/registration",false).delete();
            }
            global.addTable("debug",debug);
            FileHelper.get(MTRef.CONFIG_PATH+"/debug",false).delete();
        }
        Toml channels = ChannelHelper.openToml(MTRef.CONFIG_PATH+"/channels",this);
        if(Objects.nonNull(channels)) {
            global.addTable("channels",channels);
            FileHelper.get(MTRef.CONFIG_PATH+"/channels",false).delete();
        }
        return global;
    }
    
    @Override public String getPathMain(Toml channel) {
        return channel.getOrSetValue("main",channel.getName()+"/main"); //6.3.1 Didn't write the default parameters
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
                    entry = remapAudioEntry(entry);
                    Toml table = upgradeToTable(entry);
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
        String path = channel.getOrSetValue("transitions",channel.getName()+"/transitions");
        Toml renders = ChannelHelper.openToml(path,this);
        if(Objects.nonNull(renders)) {
            File file = FileHelper.get(path+".toml",false);
            if(file.getName().contains("renders"))
                file.renameTo(new File(file.getParent(),file.getName().replace("transitions","renders")));
        }
        return renders;
    }
    
    @Override public Toml getToggles(Toml global) {
        Toml toggles = Toml.getEmpty();
        Toml channels = global.getTable("channels");
        if(Objects.nonNull(channels)) {
            for(Toml channel : channels.getAllTables()) {
                String path = channel.getOrSetValue("toggles", channel.getName()+"/toggles");
                Toml channelToggles = ChannelHelper.openToml(path,this);
                if(Objects.nonNull(channelToggles)) {
                    for(Toml toggle : channelToggles.getAllTables())
                        toggles.addTable(toggle.getName(),toggle);
                    FileHelper.get(path+".toml",false).delete(); //Remove the unused file
                }
            }
        }
        return toggles;
    }
    
    @Override
    public ConfigVersion getVersionTarget() {
        return ConfigVersionManager.findLatestQualified(7,0,0);
    }
    
    @Override public TomlEntry<?> remapAudioEntry(TomlEntry<?> entry) {
        return entry.getKey().equals("must_finish") ? new TomlEntry<>("interrupt_handler",entry.getValue()) : entry;
    }
    
    @Override public TomlEntry<?> remapChannelInfoEntry(TomlEntry<?> entry) {
        switch(entry.getKey()) {
            case "explicit_overrides": return new TomlEntry<>("explicitly_overrides",entry.getValue());
            case "overrides_normal_music": return new TomlEntry<>("overrides_music",entry.getValue());
            case "pause_overrides": return new TomlEntry<>("pauses_overrides",entry.getValue());
            case "songs_folder": return new TomlEntry<>("local_folder",entry.getValue());
            case "transitions": return new TomlEntry<>("renders",entry.getValue());
            default: return entry;
        }
    }
    
    @Override public TomlEntry<?> remapDebugEntry(TomlEntry<?> entry) {
        switch(entry.getKey()) {
            case "ALLOW_TIMESTAMPS": return new TomlEntry<>("allow_timestamps",entry.getValue());
            case "BLOCK_STREAMING_ONLY": return new TomlEntry<>("block_sound_effects",entry.getValue());
            case "BLOCKED_MOD_CATEGORIES": return new TomlEntry<>("blocked_sound_categories",entry.getValue());
            case "COMBINE_EQUAL_PRIORITY": return new TomlEntry<>("independent_audio_pools",entry.getValue());
            case "CURRENT_SONG_ONLY": return new TomlEntry<>("show_song_info",entry.getValue());
            case "ENCODING_QUALITY": return new TomlEntry<>("encoding_quality",entry.getValue());
            case "INTERRUPTED_AUDIO_CATEGORIES": return new TomlEntry<>("interrupted_sound_categories",entry.getValue());
            case "LOG_LEVEL":
            case "MAX_HOVER_ELEMENTS": return null;
            case "PAUSE_WHEN_TABBED": return new TomlEntry<>("pause_unless_focused",entry.getValue());
            case "PLAY_NORMAL_MUSIC": return new TomlEntry<>("play_normal_music",entry.getValue());
            case "RESAMPLING_QUALITY": return new TomlEntry<>("resampling_quality",entry.getValue());
            case "REVERSE_PRIORITY": return new TomlEntry<>("reverse_priority",entry.getValue());
            case "SHOW_DEBUG": return new TomlEntry<>("enable_debug_info",entry.getValue());
            default: return entry;
        }
    }
    
    @Override public TomlEntry<?> remapTriggerEntry(String name, TomlEntry<?> entry) {
        switch(entry.getKey()) {
            case "biome_category": return new TomlEntry<>("biome_tag",entry.getValue());
            case "check_higher_rainfall": return new TomlEntry<>("rainfall_greater_than",entry.getValue());
            case "check_lower_temp": return new TomlEntry<>("temperature_greater_than",entry.getValue());
            case "level": {
                switch(name) { //Double layer switch :|
                    case "lowhp": return new TomlEntry<>("health_percentage",entry.getValue());
                    case "raid": return new TomlEntry<>("wave",entry.getValue());
                    case "season": return new TomlEntry<>("season",entry.getValue());
                    default: return entry;
                }
            }
            case "song_delay": return new TomlEntry<>("ticks_between_audio",entry.getValue());
            case "start_delay": return new TomlEntry<>("ticks_before_active",entry.getValue());
            case "start_toggled": return new TomlEntry<>("start_as_disabled",entry.getValue());
            case "stop_delay": return new TomlEntry<>("active_cooldown",entry.getValue());
            case "trigger_delay": return new TomlEntry<>("ticks_before_audio",entry.getValue());
            default: return entry;
        }
    }
    
    @Override public String remapTriggerName(String name) {
        return name.equals("fallingstars") ? "starshower" : name;
    }
    
    @Nullable @Override public Toml upgradeToTable(TomlEntry<?> entry) {
        return entry.getKey().equals("interrupt_handler") ? Toml.getEmpty() : null;
    }
}
