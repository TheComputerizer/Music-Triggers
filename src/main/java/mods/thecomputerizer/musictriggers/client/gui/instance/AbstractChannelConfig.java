package mods.thecomputerizer.musictriggers.client.gui.instance;

import java.io.File;

public abstract class AbstractChannelConfig extends AbstractConfig {
    private final String channelName;
    protected AbstractChannelConfig(File configFile, String channelName) {
        super(configFile);
        this.channelName = channelName;
    }

    protected String getChannelName() {
        return this.channelName;
    }
}
