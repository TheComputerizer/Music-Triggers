package mods.thecomputerizer.musictriggers.api.server;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerSelectorAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

public class ChannelServer extends ChannelAPI { //TODO implement this

    public ChannelServer(Table table) {
        super(table);
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
    public void loadTrack(AudioRef ref, String location) {

    }

    @Override
    public void onTrackStart(AudioTrack track) {

    }

    @Override
    public void onTrackStop(AudioTrackEndReason endReason) {}

    @Override
    public void setCategoryVolume(float volume) {

    }

    @Override
    public void setTrackVolume(float volume) {

    }

    @Override
    public void tickSlow() {

    }
}
