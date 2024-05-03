package mods.thecomputerizer.musictriggers.api.data.channel;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import java.io.File;
import java.util.Objects;

@Getter
public class ChannelInfo extends ChannelElement {

    public static void writeExampleData(Toml table) {
        //AbstractType type = holder.addComment(table, Arrays.asList("Auto-generated example channel - you should "+
        //        "probably replace this with your own.","Note that each channel can only play 1 sound at a time and "+
        //        "trigger context is not shared between channels other than via links and toggles.","It is recommended "+
        //        "you keep all triggers and songs on a single channel unless you have some specific use case"));
        String name = table.getName();
        table.addEntry("commands",name+"/commands");
        table.addEntry("explicitly_overrides",false);
        table.addEntry("jukebox",name+"/jukebox");
        table.addEntry("local_folder",MTRef.CONFIG_PATH+"/songs");
        table.addEntry("main",name+"/main");
        table.addEntry("overrides_music",true);
        table.addEntry("pauses_overrides",false);
        table.addEntry("redirect",name+"/redirect");
        table.addEntry("renders",name+"/renders");
        table.addEntry("sound_category","music");
    }

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

    public ChannelInfo(ChannelAPI channel, Toml table) {
        super(channel);
        String name = getChannelName();
        this.category = table.getOrSetValue("sound_category","music");
        this.commandsPath = table.getOrSetValue("commands",name+"/commands");
        this.excplicitlyOverrides = table.getOrSetValue("explicitly_overrides",false);
        this.jukeboxPath = table.getOrSetValue("jukebox",name+"/jukebox");
        String localPath = table.getOrSetValue("local_folder", MTRef.CONFIG_PATH+"/songs");
        this.localFolder = new File(localPath);
        this.mainPath = table.getOrSetValue("main",name+"/main");
        this.overridesMusic = table.getOrSetValue("overrides_music",true);
        this.pausesOverrides = table.getOrSetValue("pauses_overrides",false);
        this.redirectPath = table.getOrSetValue("redirect",name+"/redirect");
        this.rendersPath = table.getOrSetValue("renders",name+"/renders");
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

    @Override
    public void close() {

    }
}