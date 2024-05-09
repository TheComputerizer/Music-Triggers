package mods.thecomputerizer.musictriggers.api.data.channel;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

@Getter
public class ChannelInfo extends ChannelElement {

    public static void writeExampleData(Toml table) {
        table.addComments(Arrays.asList("Auto-generated example channel - you should probably replace this with your "+
                "own.","Note that each channel can only play 1 sound at a time and trigger context is not shared "+
                "between channels other than via links and toggles.","It is recommended you keep all triggers and "+
                "songs on a single channel unless you have some specific use case"));
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
        super(channel,"channel_info");
        if(parse(table)) {
            this.category = getParameterAsString("sound_category");
            this.commandsPath = getParameterAsString("commands");
            this.excplicitlyOverrides = getParameterAsBoolean("explicitly_overrides");
            this.jukeboxPath = getParameterAsString("jukebox");
            this.localFolder = new File(getParameterAsString("local_folder"));
            this.mainPath = getParameterAsString("main");
            this.overridesMusic = !getParameterAsBoolean("play_normal_music");
            this.pausesOverrides = getParameterAsBoolean("pauses_overrides");
            this.redirectPath = getParameterAsString("redirect");
            this.rendersPath = getParameterAsString("renders");
        } else {
            this.category = "music";
            this.commandsPath = "commands";
            this.excplicitlyOverrides = false;
            this.jukeboxPath = "jukebox";
            this.localFolder = new File(MTRef.CONFIG_PATH+"/songs");
            this.mainPath = "main";
            this.overridesMusic = true;
            this.pausesOverrides = false;
            this.redirectPath = "redirect";
            this.rendersPath = "renders";
        }
        if(!this.localFolder.exists() && !this.localFolder.mkdirs())
            logError("Unable to make songs folder at path `{}`! Local files will be unable to load.",this.localFolder);
    }

    public boolean canReadFiles() {
        return Objects.nonNull(this.localFolder) && this.localFolder.exists() && this.localFolder.isDirectory();
    }
    
    @Override protected TableRef getReferenceData() {
        return MTDataRef.CHANNEL_INFO;
    }
    
    @Override protected String getSubTypeName() {
        return "Info";
    }
    
    @Override public Class<? extends ParameterWrapper> getTypeClass() {
        return ChannelInfo.class;
    }
    
    @Override
    public boolean isResource() {
        return false;
    }

    @Override
    public void close() {

    }
}