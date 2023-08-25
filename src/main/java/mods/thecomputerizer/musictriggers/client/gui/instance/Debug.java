package mods.thecomputerizer.musictriggers.client.gui.instance;

import mods.thecomputerizer.musictriggers.client.gui.ButtonType;
import mods.thecomputerizer.musictriggers.client.gui.GuiPage;
import mods.thecomputerizer.musictriggers.client.gui.GuiParameters;
import mods.thecomputerizer.musictriggers.client.gui.GuiType;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Holder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Debug extends AbstractConfig {

    private final Holder fileData;

    public Debug(Holder fileData) {
        this.fileData = fileData;
    }

    @Override
    public List<GuiParameters.Parameter> getParameters(GuiType type) {
        return Arrays.asList(new GuiParameters.Parameter(type.getId(),"show_debug",
                        fileData.getOrCreateVar(null,"SHOW_DEBUG",false)),
                new GuiParameters.Parameter(type.getId(),"current_song_only",
                        fileData.getOrCreateVar(null,"CURRENT_SONG_ONLY",false)),
                new GuiParameters.Parameter(type.getId(),"allow_timestamps",
                        fileData.getOrCreateVar(null,"ALLOW_TIMESTAMPS",false)),
                new GuiParameters.Parameter(type.getId(),"encoding_quality",
                        fileData.getOrCreateVar(null,"ENCODING_QUALITY",10)),
                new GuiParameters.Parameter(type.getId(),"resampling_quality",
                        fileData.getOrCreateVar(null,"RESAMPLING_QUALITY","HIGH")),
                new GuiParameters.Parameter(type.getId(),"log_level",
                        fileData.getOrCreateVar(null,"LOG_LEVEL","INFO")),
                new GuiParameters.Parameter(type.getId(),"max_hover_elements",
                        fileData.getOrCreateVar(null,"MAX_HOVER_ELEMENTS",15)),
                new GuiParameters.Parameter(type.getId(),"play_normal_music",
                        fileData.getOrCreateVar(null,"PLAY_NORMAL_MUSIC",false)),
                new GuiParameters.Parameter(type.getId(),"reverse_priority",
                        fileData.getOrCreateVar(null,"REVERSE_PRIORITY",false)),
                new GuiParameters.Parameter(type.getId(),"combine_pools",
                        fileData.getOrCreateVar(null,"COMBINE_EQUAL_PRIORITY",false)),
                new GuiParameters.Parameter(type.getId(),"pause_when_tabbed",
                        fileData.getOrCreateVar(null,"PAUSE_WHEN_TABBED",true)),
                new GuiParameters.Parameter(type.getId(),"blocked_mod_categories",
                        fileData.getOrCreateVar(null,"BLOCKED_MOD_CATEGORIES",Collections.singletonList("minecraft;music"))),
                new GuiParameters.Parameter(type.getId(),"block_streaming_only",
                        fileData.getOrCreateVar(null,"BLOCK_STREAMING_ONLY",true)),
                new GuiParameters.Parameter(type.getId(),"interrupted_audio_categories",
                        fileData.getOrCreateVar(null,"INTERRUPTED_AUDIO_CATEGORIES",Collections.singletonList("music"))));
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
        ConfigDebug.update(this.fileData);
    }
}
