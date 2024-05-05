package mods.thecomputerizer.musictriggers.api.data.channel;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import java.io.File;
import java.util.Objects;

@Getter
public class ChannelInfo extends ChannelElement { //TODO Switch to parameters

    public static void writeExampleData(Toml table) {
        //AbstractType type = holder.addComment(table, Arrays.asList("Auto-generated example channel - you should "+
        //        "probably replace this with your own.","Note that each channel can only play 1 sound at a time and "+
        //        "trigger context is not shared between channels other than via links and toggles.","It is recommended "+
        //        "you keep all triggers and songs on a single channel unless you have some specific use case"));
        table.addEntry("commands","commands");
        table.addEntry("explicitly_overrides",false);
        table.addEntry("jukebox","jukebox");
        table.addEntry("local_folder",MTRef.CONFIG_PATH+"/songs");
        table.addEntry("main","main");
        table.addEntry("overrides_music",true);
        table.addEntry("pauses_overrides",false);
        table.addEntry("redirect","redirect");
        table.addEntry("renders","renders");
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
        TableRef ref = MTDataRef.CHANNEL_INFO;
        this.category = ref.getOrDefault(table,"sound_category");
        this.commandsPath = ref.getOrDefault(table,"commands");
        this.excplicitlyOverrides = ref.getOrDefault(table,"explicitly_overrides");
        this.jukeboxPath = ref.getOrDefault(table,"jukebox");
        String localPath = ref.getOrDefault(table,"local_folder");
        this.localFolder = new File(localPath);
        this.mainPath = ref.getOrDefault(table,"main");
        this.overridesMusic = ref.getOrDefault(table,"overrides_music");
        this.pausesOverrides = ref.getOrDefault(table,"pauses_overrides");
        this.redirectPath = ref.getOrDefault(table,"redirect");
        this.rendersPath = ref.getOrDefault(table,"renders");
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