package mods.thecomputerizer.musictriggers.api.server;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

public class ChannelServer extends ChannelAPI { //TODO implement this

    public ChannelServer(ChannelHelper helper, Table table) {
        super(helper,table);
    }

    @Override
    public AudioPlayer getPlayer() {
        return null;
    }

    @Override
    public boolean isClientChannel() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void loadLocalTrack(AudioRef ref, String location) {}

    @Override
    public void loadRemoteTrack(AudioRef ref, String location) {}

    @Override
    public void onResourcesLoaded() {}

    @Override
    public void onTrackStart(AudioTrack track) {}

    @Override
    public void onTrackStop(AudioTrackEndReason endReason) {}

    @Override
    public void setCategoryVolume(float volume) {}

    @Override
    public void setTrackVolume(float volume) {}

    @Override
    public void tickSlow() {

    }
}
