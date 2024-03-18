package mods.thecomputerizer.musictriggers.api.server;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerSelectorAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

public class ServerChannel extends ChannelAPI {

    public ServerChannel(Table table) {
        super(table);
    }

    @Override
    public AudioPlayer getPlayer() {
        return null;
    }

    @Override
    protected TriggerSelectorAPI<?,?> getSelector() {
        return null;
    }

    @Override
    public boolean isClientChannel() {
        return false;
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
    public void tickFast() {

    }

    @Override
    public void tickSlow() {

    }
}
