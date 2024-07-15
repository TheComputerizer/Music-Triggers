package mods.thecomputerizer.musictriggers.api.server;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioPool;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Objects;

public class ChannelServer extends ChannelAPI {
    
    protected String currentSong;

    public ChannelServer(ChannelHelper helper, Toml table) {
        super(helper,table);
    }

    @Override public boolean checkDeactivate(TriggerAPI current, TriggerAPI next) {
        return Objects.isNull(current) ? Objects.nonNull(next) : !current.equals(next);
    }
    
    @Nullable @Override public String getFormattedSongTime() {
        logError("Tried to get the time of the playing song on the server!");
        return null;
    }
    
    @Override public String getLogType() {
        return "SERVER";
    }

    @Override public AudioPlayer getPlayer() {
        logError("Tried to get AudioPlayer instance on the server!");
        return null;
    }
    
    @Nullable @Override public AudioPool getPlayingPool() {
        logError("Tried to get the playing audio pool instance on the server!");
        return null;
    }
    
    @Nullable @Override public String getPlayingSongName() {
        return this.currentSong;
    }
    
    @Override public long getPlayingSongTime() {
        logError("Tried to get the time of the playing song on the server!");
        return 0L;
    }
    
    @Override public boolean isClientChannel() {
        return false;
    }

    @Override public boolean isValid() {
        return true;
    }

    @Override public String loadLocalTrack(AudioRef ref, String location) {
        logError("Tried to load local audio track on the server!");
        return null;
    }

    @Override public String loadRemoteTrack(AudioRef ref, String location) {
        logError("Tried to load remote audio track on the server!");
        return null;
    }

    @Override public void onResourcesLoaded() {
        logError("onResourcesLoaded called on the server!");
    }

    @Override public void onTrackStart(AudioTrack track) {
        logError("onTrackStart called on the server!");
    }

    @Override public void onTrackStop(AudioTrackEndReason endReason) {
        logError("onTrackStop called on the server! {}",endReason);
    }

    @Override public void setCategoryVolume(float volume) {
        logError("Tried to set category volume on the server!");
    }
    public void setCurrentSong(String name) {
        this.currentSong = StringUtils.isNotBlank(name) ? name : null;
    }
    
    @Override public void setMasterVolume(float volume) {
        logError("Tried to set master volume on the server!");
    }
    
    @Override public void setTrackVolume(float volume) {
        logError("Tried to set track volume on the server!");
    }
    
    @Override public boolean shouldBlockMusicTicker() {
        logError("Tried query the vanilla music ticker on the server!");
        return false;
    }
}
