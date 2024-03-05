package mods.thecomputerizer.musictriggers.client.gui.instance;

public abstract class AbstractChannelConfig extends AbstractConfig {
    private final String channelName;
    protected AbstractChannelConfig(String channelName) {
        this.channelName = channelName;
    }
    protected String getChannelName() {
        return this.channelName;
    }
}
