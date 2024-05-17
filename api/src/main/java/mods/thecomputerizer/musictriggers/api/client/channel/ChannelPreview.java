package mods.thecomputerizer.musictriggers.api.client.channel;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.shadow.org.joml.Vector3i;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

public final class ChannelPreview extends ChannelClientSpecial {
    
    public ChannelPreview(ChannelHelper helper, Toml table) {
        super(helper,table);
    }
    
    @Override public void checkStop(Vector3i pos) {
        stop();
    }
    
    @Override public String getLogType() {
        return "PREVIEW";
    }
    
    @Override public boolean showDebugSongInfo() {
        return false;
    }
}
