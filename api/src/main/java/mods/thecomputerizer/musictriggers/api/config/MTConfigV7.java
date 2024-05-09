package mods.thecomputerizer.musictriggers.api.config;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml.TomlEntry;

import javax.annotation.Nullable;

import static mods.thecomputerizer.musictriggers.api.data.MTDataRef.FROM;

public class MTConfigV7 extends ConfigVersion {
    
    public static final MTConfigV7 V7_0_0_BETA_4 = new MTConfigV7(0,0,"beta",4){};
    public static final MTConfigV7 V7_0_0_BETA_3 = new MTConfigV7(0,0,"beta",3){
        @Override public ConfigVersion getVersionTarget() {
            return V7_0_0_BETA_4;
        }};
    public static final MTConfigV7 V7_0_0_BETA_1 = new MTConfigV7(0,0,"beta",1){
        @Override public ConfigVersion getVersionTarget() {
            return V7_0_0_BETA_4;
        }
    };
    
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
    
    @Override public TomlEntry<?> remapLinkEntry(String trigger, TomlEntry<?> entry) {
        return entry.getKey().equals("channel") ? new TomlEntry<>("target_channel",entry.getValue()) : entry;
    }
    
    @Override public TomlEntry<?> remapToggleFrom(TomlEntry<?> entry) {
        return entry.getKey().equals("condition") ? new TomlEntry<>("event",entry.getValue()) : entry;
    }
    
    @Override public TomlEntry<?> remapToggleTo(TomlEntry<?> entry) {
        return entry;
    }
    
    @Override public TomlEntry<?> remapTriggerEntry(String name, TomlEntry<?> entry) {
        return entry;
    }
    
    @Override public String remapTriggerName(String name) {
        return name;
    }
    
    @Nullable @Override public Toml upgradeToTable(TableRef ref, TomlEntry<?> entry) {
        if(ref==FROM && entry.getKey().equals("event")) {
            Toml table = Toml.getEmpty();
            String value = String.valueOf(entry.getValue());
            switch(value) {
                case "active": {
                    table.addEntry("name","activate");
                    break;
                }
                case "playable": {
                    table.addEntry("name","playable");
                    break;
                }
            }
            return table;
        }
        return null;
    }
    
    @Override public void verifyJukebox(Toml channel) {
    
    }
    
    @Override public void verifyRedirct(Toml channel) {
    
    }
}
