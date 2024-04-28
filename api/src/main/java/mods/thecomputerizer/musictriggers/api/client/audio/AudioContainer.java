package mods.thecomputerizer.musictriggers.api.client.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class AudioContainer extends AudioRef {

    private int fade;
    private float fadeFactor;
    private AudioItem item;
    private int playlistIndex;

    public AudioContainer(ChannelAPI channel, String name) {
        super(channel,name);
    }

    @Override
    public void close() {
        super.close();
        this.fade = 0;
        this.fadeFactor = 0f;
        this.item = null;
        this.playlistIndex = 0;
    }

    protected float getFade() {
        return (float)this.fade*this.fadeFactor;
    }

    protected @Nullable AudioTrack getTrack() {
        if(this.item instanceof AudioTrack) return (AudioTrack)this.item;
        if(this.item instanceof AudioPlaylist) {
            AudioPlaylist playlist = (AudioPlaylist)this.item;
            if(Objects.nonNull(playlist.getSelectedTrack())) return playlist.getSelectedTrack();
            List<AudioTrack> tracks = playlist.getTracks();
            if(Objects.nonNull(tracks) && !tracks.isEmpty()) {
                AudioTrack track = tracks.get(this.playlistIndex);
                this.playlistIndex++;
                if(this.playlistIndex>=tracks.size()) this.playlistIndex = 0;
                return track;
            }
        }
        return null;
    }

    @Override
    public float getVolume() {
        return this.fade<=0 ? 1f : (this.fadeFactor<=0 ? 1f+getFade() : getFade());
    }

    @Override
    public void loadLocal(String location) {
        this.channel.loadLocalTrack(this,location);
    }

    @Override
    public void loadRemote(String location) {
        this.channel.loadRemoteTrack(this,location);
    }

    @Override
    public void playing() {
        if(this.fade>0) {
            if(this.fadeFactor==0f) this.fade = 0;
            else this.fade--;
            if(this.fade==0) {
                if(this.fadeFactor>0f) {
                    AudioPlayer player = this.channel.getPlayer();
                    if(Objects.nonNull(player)) player.stopTrack();
                }
                this.fadeFactor = 0f;
            }
            this.channel.setTrackVolume(getVolume());
        }
    }

    /**
     * Set the max of the new fade value to the progress of the interrupted value if there is one present
     */
    @Override
    public void setFade(int fade) {
        if(fade<0 && this.fadeFactor!=0f)
            fade = (int)((float)fade*((float)this.fade/(this.fadeFactor<0f ? -1f/this.fadeFactor : 1f/this.fadeFactor)));
        this.fadeFactor = 1f/(float)fade;
        if(this.fadeFactor==0f) this.fade = 0;
        else {
            this.fade = Math.abs(fade);
            if(this.fade==0) this.fadeFactor = 0f;
        }
    }

    @Override
    public void setItem(AudioItem item) {
        this.item = item;
    }

    @Override
    public void start(TriggerAPI trigger) {
        logInfo("Starting audio");
        AudioPlayer player = this.channel.getPlayer();
        AudioTrack track = getTrack();
        if(Objects.isNull(player) || Objects.isNull(track)) return;
        logInfo("Starting fade");
        if(Objects.nonNull(trigger)) setFade(-trigger.getParameterAsInt("fade_in"));
        logInfo("Setting volume");
        this.channel.setTrackVolume(getVolume());
        logInfo("Playing track");
        player.playTrack(track);
    }

    @Override
    public void stop() {
        TriggerAPI trigger = this.channel.getActiveTrigger();
        if(Objects.nonNull(trigger) && isInterrputedBy(trigger)) setFade(trigger.getParameterAsInt("fade_out"));
    }
}
