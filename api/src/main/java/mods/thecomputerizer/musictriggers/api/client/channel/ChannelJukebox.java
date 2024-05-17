package mods.thecomputerizer.musictriggers.api.client.channel;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.shadow.org.joml.Vector3i;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

public final class ChannelJukebox extends ChannelClientSpecial {
    
    public ChannelJukebox(ChannelHelper helper, Toml table) {
        super(helper,table);
        this.setMasterVolume(1f);
        this.setCategoryVolume(1f);
        this.setTrackVolume(1f);
    }
    
    @Override
    public boolean checkJukebox(boolean jukebox) {
        return true;
    }
    
    @Override
    public void checkStop(Vector3i pos) {
        if(this.playingPos==pos || this.playingPos.distance(pos)<=2)
            stop(); //In case the position moves or there is a rounding error?
    }
    
    @Override public String getLogType() {
        return "JUKEBOX";
    }
}