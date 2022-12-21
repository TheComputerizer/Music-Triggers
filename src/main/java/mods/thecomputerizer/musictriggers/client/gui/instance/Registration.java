package mods.thecomputerizer.musictriggers.client.gui.instance;

import mods.thecomputerizer.musictriggers.client.gui.ButtonType;
import mods.thecomputerizer.musictriggers.client.gui.GuiPage;
import mods.thecomputerizer.musictriggers.client.gui.GuiParameters;
import mods.thecomputerizer.musictriggers.client.gui.GuiType;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Registration extends AbstractConfig {
    private boolean REGISTER_DISCS;
    private boolean CLIENT_SIDE_ONLY;

    public Registration(File configFile, boolean discs, boolean client) {
        super(configFile);
        this.REGISTER_DISCS = discs;
        this.CLIENT_SIDE_ONLY = client;
    }

    @Override
    public List<GuiParameters.Parameter> getParameters(GuiType type) {
        return Arrays.asList(new GuiParameters.Parameter("register_discs",type.getId(),null,this.REGISTER_DISCS,
                        (element) -> this.REGISTER_DISCS = element),
                new GuiParameters.Parameter("client_side_only",type.getId(),null,this.CLIENT_SIDE_ONLY,
                        (element) -> this.CLIENT_SIDE_ONLY = element));
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
        ConfigRegistry.write(this.REGISTER_DISCS,this.CLIENT_SIDE_ONLY);
    }

    public void save(boolean discs, boolean client) {
        this.REGISTER_DISCS = discs;
        this.CLIENT_SIDE_ONLY = client;
    }
}
