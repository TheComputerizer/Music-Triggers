package mods.thecomputerizer.musictriggers.client.gui.instance;

import mods.thecomputerizer.musictriggers.client.gui.GuiPage;
import mods.thecomputerizer.musictriggers.client.gui.GuiParameters;
import mods.thecomputerizer.musictriggers.client.gui.GuiType;

import java.util.List;

public abstract class AbstractConfig {
    public abstract List<GuiParameters.Parameter> getParameters(GuiType type);
    public abstract List<GuiPage.Icon> getPageIcons(String channel);
    protected abstract List<String> headerLines();
    protected abstract void write(String newFilePath);
}
