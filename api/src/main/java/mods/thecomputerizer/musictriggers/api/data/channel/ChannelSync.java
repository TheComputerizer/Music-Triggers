package mods.thecomputerizer.musictriggers.api.data.channel;

public class ChannelSync extends ChannelElement {

    public ChannelSync(ChannelAPI channel) {
        super(channel);
    }



    @Override
    public boolean isResource() {
        return false;
    }
}