package mods.thecomputerizer.musictriggers.api.server;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import javax.annotation.Nullable;

public class ChannelServer extends ChannelAPI {

    public ChannelServer(ChannelHelper helper, Toml table) {
        super(helper,table);
    }

    @Override
    public boolean checkDeactivate(TriggerAPI current, TriggerAPI next) {
        return current!=next;
    }

    @Override
    public AudioPlayer getPlayer() {
        logError("Tried to get AudioPlayer instance on the server!");
        return null;
    }
    
    @Nullable @Override public String getPlayingSongName() {
        logError("Tried to get the name of the playing song on the server!");
        return null;
    }
    
    @Nullable @Override public String getPlayingSongTime() {
        logError("Tried to get the time of the playing song on the server!");
        return null;
    }
    
    @Override protected String getTypeName() {
        return "ServerChannel";
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
    public void loadLocalTrack(AudioRef ref, String location) {
        logError("Tried to load local audio track on the server!");
    }

    @Override
    public void loadRemoteTrack(AudioRef ref, String location) {
        logError("Tried to load remote audio track on the server!");
    }

    @Override
    public void onResourcesLoaded() {
        logError("onResourcesLoaded called on the server!");
    }

    @Override
    public void onTrackStart(AudioTrack track) {
        logError("onTrackStart called on the server!");
    }

    @Override
    public void onTrackStop(AudioTrackEndReason endReason) {
        logError("onTrackStop called on the server! {}");
    }

    @Override
    public void setCategoryVolume(float volume) {
        logError("Tried to set category volume on the server!");
    }

    @Override
    public void setTrackVolume(float volume) {
        logError("Tried to set track volume on the server!");
    }
    
    @Override public boolean shouldBlockMusicTicker() {
        logError("Tried query the vanilla music ticker on the server!");
        return false;
    }
}
