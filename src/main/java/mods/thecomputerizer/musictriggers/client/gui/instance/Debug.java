package mods.thecomputerizer.musictriggers.client.gui.instance;

import mods.thecomputerizer.musictriggers.client.gui.ButtonType;
import mods.thecomputerizer.musictriggers.client.gui.GuiPage;
import mods.thecomputerizer.musictriggers.client.gui.GuiParameters;
import mods.thecomputerizer.musictriggers.client.gui.GuiType;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Debug extends AbstractConfig {
    private boolean SHOW_DEBUG;
    private boolean CURRENT_SONG_ONLY;
    private String LOG_LEVEL;
    private boolean PLAY_NORMAL_MUSIC;
    private boolean REVERSE_PRIORITY;
    private boolean COMBINE_EQUAL_PRIORITY;
    private String[] BLOCKED_MOD_MUSIC;
    private String[] BLOCKED_MOD_RECORDS;

    public Debug(File configFile, boolean debugInfo, boolean currentSong, String logLevel, boolean overlap,
                 boolean reversePriority, boolean combinePools, String[] blockedMusic, String[] blockedRecords) {
        super(configFile);
        this.SHOW_DEBUG = debugInfo;
        this.CURRENT_SONG_ONLY = currentSong;
        this.LOG_LEVEL = logLevel;
        this.PLAY_NORMAL_MUSIC = overlap;
        this.REVERSE_PRIORITY = reversePriority;
        this.COMBINE_EQUAL_PRIORITY = combinePools;
        this.BLOCKED_MOD_MUSIC = blockedMusic;
        this.BLOCKED_MOD_RECORDS = blockedRecords;
    }

    @Override
    public List<GuiParameters.Parameter> getParameters(GuiType type) {
        return Arrays.asList(new GuiParameters.Parameter(type.getId(),"show_debug",null,this.SHOW_DEBUG,
                        (element) -> this.SHOW_DEBUG = element),
                new GuiParameters.Parameter(type.getId(),"current_song_only",null,this.CURRENT_SONG_ONLY,
                        (element) -> this.CURRENT_SONG_ONLY = element),
                new GuiParameters.Parameter(type.getId(),"log_level",null,this.LOG_LEVEL,
                        (element) -> this.LOG_LEVEL = element),
                new GuiParameters.Parameter(type.getId(),"play_normal_music",null,this.PLAY_NORMAL_MUSIC,
                        (element) -> this.PLAY_NORMAL_MUSIC = element),
                new GuiParameters.Parameter(type.getId(),"reverse_priority",null,this.REVERSE_PRIORITY,
                        (element) -> this.REVERSE_PRIORITY = element),
                new GuiParameters.Parameter(type.getId(),"combine_pools",null,this.COMBINE_EQUAL_PRIORITY,
                        (element) -> this.COMBINE_EQUAL_PRIORITY = element),
                new GuiParameters.Parameter(type.getId(),"blocked_mod_music",null,
                        (element) -> this.BLOCKED_MOD_MUSIC = element.toArray(new String[0]),this.BLOCKED_MOD_MUSIC),
                new GuiParameters.Parameter(type.getId(),"blocked_mod_records",null,
                        (element) -> this.BLOCKED_MOD_RECORDS = element.toArray(new String[0]),this.BLOCKED_MOD_RECORDS));
    }

    @Override
    public List<GuiPage.Icon> getPageIcons(String channel) {
        return Collections.singletonList(ButtonType.DEBUG.getIconButton(null,false));
    }

    @Override
    protected List<String> headerLines() {
        return new ArrayList<>();
    }

    @Override
    protected void write(String path) {
        ConfigDebug.update(this.SHOW_DEBUG,this.CURRENT_SONG_ONLY,this.LOG_LEVEL,this.PLAY_NORMAL_MUSIC,
                this.REVERSE_PRIORITY,this.COMBINE_EQUAL_PRIORITY,this.BLOCKED_MOD_MUSIC,this.BLOCKED_MOD_RECORDS);
        ConfigDebug.write();
    }
}
