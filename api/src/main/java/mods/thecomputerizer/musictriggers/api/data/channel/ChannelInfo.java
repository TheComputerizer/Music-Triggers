package mods.thecomputerizer.musictriggers.api.data.channel;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

import static mods.thecomputerizer.musictriggers.api.MTRef.CONFIG_PATH;
import static mods.thecomputerizer.musictriggers.api.data.MTDataRef.CHANNEL_INFO;

public class ChannelInfo extends ChannelElement {

    public static void writeExampleData(Toml table) {
        table.addComments(Arrays.asList("Auto-generated example channel - you should probably replace this with your "+
                "own.","Note that each channel can only play 1 sound at a time and trigger context is not shared "+
                "between channels other than via links and toggles.","It is recommended you keep all triggers and "+
                "songs on a single channel unless you have some specific use case"));
        table.addEntry("commands","commands");
        table.addEntry("explicitly_overrides",false);
        table.addEntry("has_paused_music",false);
        table.addEntry("jukebox","jukebox");
        table.addEntry("local_folder",CONFIG_PATH+"/songs");
        table.addEntry("main","main");
        table.addEntry("overrides_music",true);
        table.addEntry("paused_by_jukebox",true);
        table.addEntry("pauses_overrides",false);
        table.addEntry("play_normal_music",false);
        table.addEntry("redirect","redirect");
        table.addEntry("renders","renders");
        table.addEntry("sound_category","music");
    }

    @Getter private final String category;
    @Getter private final String commandsPath;
    @Getter private final boolean excplicitlyOverrides;
    private final boolean hasPausedMusic;
    @Getter private final String jukeboxPath;
    @Getter private final File localFolder;
    @Getter private final String mainPath;
    @Getter private final boolean overridesMusic;
    @Getter private final boolean pausedByJukebox;
    @Getter private final boolean pausesOverrides;
    @Getter private final String redirectPath;
    @Getter private final String rendersPath;

    public ChannelInfo(ChannelAPI channel, Toml table) {
        super(channel,"channel_info");
        if(parse(table)) {
            this.category = getParameterAsString("sound_category");
            this.commandsPath = getParameterAsString("commands");
            this.excplicitlyOverrides = getParameterAsBoolean("explicitly_overrides");
            this.hasPausedMusic = getParameterAsBoolean("has_paused_music");
            this.jukeboxPath = getParameterAsString("jukebox");
            this.localFolder = new File(getParameterAsString("local_folder"));
            this.mainPath = getParameterAsString("main");
            this.overridesMusic = !getParameterAsBoolean("play_normal_music");
            this.pausedByJukebox = getParameterAsBoolean("paused_by_jukebox");
            this.pausesOverrides = getParameterAsBoolean("pauses_overrides");
            this.redirectPath = getParameterAsString("redirect");
            this.rendersPath = getParameterAsString("renders");
        } else {
            this.category = "music";
            this.commandsPath = "commands";
            this.excplicitlyOverrides = false;
            this.hasPausedMusic = false;
            this.jukeboxPath = "jukebox";
            this.localFolder = new File(CONFIG_PATH+"/songs");
            this.mainPath = "main";
            this.overridesMusic = true;
            this.pausedByJukebox = true;
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
    
    @Override public TableRef getReferenceData() {
        return CHANNEL_INFO;
    }
    
    @Override protected String getSubTypeName() {
        return "Info";
    }
    
    @Override public Class<? extends ParameterWrapper> getTypeClass() {
        return ChannelInfo.class;
    }
    
    public boolean hasPausedMusic() {
        return this.hasPausedMusic;
    }
    
    @Override public boolean isResource() {
        return false;
    }

    @Override public void close() {}
}