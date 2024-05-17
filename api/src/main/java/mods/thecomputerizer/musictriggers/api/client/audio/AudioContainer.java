package mods.thecomputerizer.musictriggers.api.client.audio;

import com.github.natanbc.lavadsp.rotation.RotationPcmAudioFilter;
import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI.Link;
import mods.thecomputerizer.musictriggers.api.network.MTNetwork;
import mods.thecomputerizer.musictriggers.api.network.MessageCurrentSong;

import javax.annotation.Nullable;
import java.util.*;

public class AudioContainer extends AudioRef {

    private int fade;
    private float fadeFactor;
    private AudioItem item;
    private int playlistIndex;

    public AudioContainer(ChannelAPI channel, String name) {
        super(channel,name);
    }

    private void checkFade(int fade) {
        if(fade>0) setFade(-fade);
        this.channel.setTrackVolume(getVolume());
    }

    public AudioTrack checkState(@Nullable AudioTrack track) {
        if(Objects.isNull(track)) {
            logWarn("Unable to get audio track!");
            return null;
        }
        switch(track.getState()) {
            case LOADING: {
                logWarn("Tried to play track that was still loading!");
                return null;
            }
            case FINISHED: {
                logDebug("Cloning finished track");
                return track.makeClone();
            }
            case PLAYING: {
                logInfo("Tried to play track that was already playing!");
                return null;
            }
            case SEEKING: {
                logInfo("Trying to play track from seeking state");
                return track;
            }
            case STOPPING: {
                logInfo("Trying to play track while it is stopping?");
                return track;
            }
            default: return track;
        }
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

    public @Nullable AudioTrack getTrack() {
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
        if(this.queued) {
            if(this.loaded) start(this.channel.getActiveTrigger());
            return;
        }
        for(Loop loop : this.loops) loop.run();
        if(this.fade>0) {
            if(this.fadeFactor==0f) this.fade = 0;
            else this.fade--;
            if(this.fade==0) {
                if(this.fadeFactor>0f) stopTrackImmediately();
                this.fadeFactor = 0f;
            }
            this.channel.setTrackVolume(getVolume());
        }
    }

    @Override
    public void queryInterrupt(@Nullable TriggerAPI next, AudioPlayer player) {
        if(this.fadeFactor>0f) return;
        InterruptHandler handler = getInterruptHandler();
        if(Objects.isNull(handler) || handler.isInterrputedBy(next)) this.channel.stop();
    }

    /**
     * Set the max of the new fade value to the progress of the interrupted value if there is one present
     */
    @Override
    public void setFade(int fade) {
        if(fade!=0 && this.fadeFactor!=0f)
            fade = (int)((float)fade*((float)this.fade/(this.fadeFactor<0f ? -1f/this.fadeFactor : 1f/this.fadeFactor)));
        this.fadeFactor = 1f/(float)fade;
        if(this.fadeFactor==0f) this.fade = 0;
        else {
            this.fade = Math.abs(fade);
            if(this.fade==0) this.fadeFactor = 0f;
        }
    }

    @SuppressWarnings("UnusedAssignment")
    private List<AudioFilter> setFilters(AudioTrack track, AudioDataFormat format, FloatPcmAudioFilter output) {
        List<AudioFilter> filters = new ArrayList<>();
        output = setTimescale(filters,output,format);
        output = setRotation(filters,output,format);
        Collections.reverse(filters);
        return filters;
    }

    @Override
    public void setItem(AudioItem item) {
        this.item = item;
        super.setItem(item);
    }

    private FloatPcmAudioFilter setRotation(
            List<AudioFilter> filters, FloatPcmAudioFilter output, AudioDataFormat format) {
        double rotationSpeed = getParameterAsDouble("rotation_speed");
        if(rotationSpeed!=0d) {
            output = new RotationPcmAudioFilter(output,format.sampleRate).setRotationSpeed(rotationSpeed);
            filters.add(output);
        }
        return output;
    }

    private FloatPcmAudioFilter setTimescale(
            List<AudioFilter> filters, FloatPcmAudioFilter output, AudioDataFormat format) {
        boolean needsTimeScale = false;
        double pitch = getParameterAsDouble("pitch");
        double speed = getParameterAsDouble("speed");
        if(pitch!=1d && pitch>0d) needsTimeScale = true;
        if(speed!=1d && speed>0d) needsTimeScale = true;
        if(needsTimeScale) {
            output = new TimescalePcmAudioFilter(output,format.channelCount,format.sampleRate).setPitch(pitch).setSpeed(speed);
            filters.add(output);
        }
        return output;
    }

    private void setPosition(AudioTrack track) {
        long position = this.channel.getStartTime();
        if(position==0L) position = getParameterAsLong("start_at");
        if(position>0L)  {
            track.setPosition(position);
            logDebug("Set track position to {}",position);
        }
    }

    @Override
    public void start(TriggerAPI trigger) {
        if(!this.loaded || this.loading) {
            logInfo("Queued track will play once it is finished loading");
            this.queued = true;
            return;
        }
        this.queued = false;
        AudioPlayer player = this.channel.getPlayer();
        if(Objects.isNull(player)) {
            logFatal("Cannot play track on missing audio player!");
            return;
        }
        AudioTrack track = checkState(getTrack());
        if(Objects.isNull(track)) return;
        checkFade(Objects.nonNull(trigger) ? trigger.getParameterAsInt("fade_in") : 0);
        setPosition(track);
        player.setFilterFactory(this::setFilters);
        player.playTrack(track);
        logInfo("Playing track");
        ChannelHelper helper = this.channel.getHelper();
        if(!"jukebox".equals(getChannelName()) && !"preview".equals(getChannelName()) && helper.isSyncable())
            MTNetwork.sendToServer(new MessageCurrentSong<>(helper,getChannelName(),this.name),false);
    }

    @Override
    public void stop() {
        logDebug("Stopping track");
        TriggerAPI trigger = this.channel.getActiveTrigger();
        if(Objects.nonNull(trigger)) {
            int fade = trigger.getParameterAsInt("fade_out");
            if(fade>0) setFade(fade);
            else stopTrackImmediately();
        }
        else stopTrackImmediately();
    }
    
    @Override
    public void stopped() {
        this.loops.forEach(Loop::reset);
    }

    private void stopTrackImmediately() {
        TriggerAPI trigger = this.channel.getActiveTrigger();
        if(Objects.nonNull(trigger)) {
            Link link = trigger.getActiveLink();
            if(Objects.nonNull(link)) link.setSnapshotInherit(this.channel.getPlayingSongTime());
        }
        this.channel.getPlayer().stopTrack();
    }
}
