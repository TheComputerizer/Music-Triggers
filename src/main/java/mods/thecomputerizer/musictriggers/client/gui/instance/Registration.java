package mods.thecomputerizer.musictriggers.client.gui.instance;

import mods.thecomputerizer.musictriggers.client.gui.ButtonType;
import mods.thecomputerizer.musictriggers.client.gui.GuiPage;
import mods.thecomputerizer.musictriggers.client.gui.GuiParameters;
import mods.thecomputerizer.musictriggers.client.gui.GuiType;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Holder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Registration extends AbstractConfig {
    private final Holder fileData;

    public Registration(Holder fileData) {
        this.fileData = fileData;
    }

    @Override
    public List<GuiParameters.Parameter> getParameters(GuiType type) {
        return Arrays.asList(new GuiParameters.Parameter(type.getId(),"register_discs",
                        this.fileData.getOrCreateVar(null,"REGISTER_DISCS",true)),
                new GuiParameters.Parameter(type.getId(),"client_side_only",
                        this.fileData.getOrCreateVar(null,"CLIENT_SIDE_ONLY",false)));
    }

    @Override
    public List<GuiPage.Icon> getPageIcons(String channel) {
        return Collections.singletonList(ButtonType.REGISTRATION.getIconButton(null,false));
    }

    @Override
    protected List<String> headerLines() {
        return new ArrayList<>();
    }

    @Override
    protected void write(String path) {
        ConfigRegistry.write(this.fileData);
    }
}
