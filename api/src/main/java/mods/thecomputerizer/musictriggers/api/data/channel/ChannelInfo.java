package mods.thecomputerizer.musictriggers.api.data.channel;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

import java.io.File;
import java.util.Objects;

@Getter
public class ChannelInfo extends ChannelElement {

    private final String category;
    private final String commandsPath;
    private final boolean excplicitlyOverrides;
    private final String jukeboxPath;
    private final File localFolder;
    private final String mainPath;
    private final boolean overridesMusic;
    private final boolean pausesOverrides;
    private final String redirectPath;
    private final String rendersPath;

    public ChannelInfo(ChannelAPI channel, Table table) {
        super(channel);
        String name = getChannelName();
        this.category = table.getValOrDefault("sound_category","music");
        this.commandsPath = table.getValOrDefault("commands",name+"/commands");
        this.excplicitlyOverrides = table.getValOrDefault("explicitly_overrides",false);
        this.jukeboxPath = table.getValOrDefault("jukebox",name+"/jukebox");
        String localPath = table.getValOrDefault("local_folder", MTRef.CONFIG_PATH+"/songs");
        this.localFolder = new File(localPath);
        this.mainPath = table.getValOrDefault("main",name+"/main");
        this.overridesMusic = table.getValOrDefault("overrides_music",true);
        this.pausesOverrides = table.getValOrDefault("pauses_overrides",false);
        this.redirectPath = table.getValOrDefault("redirect",name+"/redirect");
        this.rendersPath = table.getValOrDefault("renders",name+"/renders");
        if(!this.localFolder.exists() && !this.localFolder.mkdirs())
            logError("Unable to make songs folder at path `{}`! Local files will be unable to load.",localPath);
    }

    public boolean canReadFiles() {
        return Objects.nonNull(this.localFolder) && this.localFolder.exists() && this.localFolder.isDirectory();
    }

    @Override
    public boolean isResource() {
        return false;
    }
}