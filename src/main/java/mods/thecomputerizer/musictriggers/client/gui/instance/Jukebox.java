package mods.thecomputerizer.musictriggers.client.gui.instance;

import mods.thecomputerizer.musictriggers.client.gui.ButtonType;
import mods.thecomputerizer.musictriggers.client.gui.GuiPage;
import mods.thecomputerizer.musictriggers.client.gui.GuiParameters;
import mods.thecomputerizer.musictriggers.client.gui.GuiType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Jukebox extends AbstractChannelConfig {
    public Jukebox(File configFile, String channelName) {
        super(configFile,channelName);
    }

    @Override
    public List<GuiParameters.Parameter> getParameters(GuiType type) {
        return new ArrayList<>();
    }

    @Override
    public List<GuiPage.Icon> getPageIcons(String channel) {
        return Collections.singletonList(ButtonType.CHANNEL.getIconButton("jukebox",false));
    }

    @Override
    protected List<String> headerLines() {
        return null;
    }

    @Override
    protected void write(String newFilePath) {

    }
}
