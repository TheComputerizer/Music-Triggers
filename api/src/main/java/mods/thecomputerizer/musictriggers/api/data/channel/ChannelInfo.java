package mods.thecomputerizer.musictriggers.api.data.channel;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.ChannelData;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

@Getter
public class ChannelInfo extends ChannelData {

    private String category;
    private String commandsPath;
    private boolean excplicitlyOverrides;
    private String jukeboxPath;
    private String localFolder;
    private String mainPath;
    private boolean overridesMusic;
    private boolean pausesOverrides;
    private String redirectPath;
    private String rendersPath;
    private String togglesPath;

    public ChannelInfo(ChannelAPI channel, Table table) {
        super(channel);
        read(table);
    }

    public void read(Table table) {
        String name = getChannelName();
        this.category = table.getValOrDefault("sound_category","music");
        this.commandsPath = table.getValOrDefault("commands",name+"/commands");
        this.excplicitlyOverrides = table.getValOrDefault("explicitly_overrides",false);
        this.jukeboxPath = table.getValOrDefault("jukebox",name+"/jukebox");
        this.localFolder = table.getValOrDefault("local_folder",name+"config/MusicTriggers/songs");
        this.mainPath = table.getValOrDefault("main",name+"/main");
        this.overridesMusic = table.getValOrDefault("overrides_music",true);
        this.pausesOverrides = table.getValOrDefault("pauses_overrides",false);
        this.redirectPath = table.getValOrDefault("redirect",name+"/redirect");
        this.rendersPath = table.getValOrDefault("renders",name+"/renders");
        this.togglesPath = table.getValOrDefault("toggles",name+"/toggles");
    }
}