package mods.thecomputerizer.musictriggers.api.client.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;

import javax.annotation.Nullable;
import java.util.Objects;

public class AudioContainer extends AudioRef {

    private AudioTrack track;
    @Getter private boolean playing = false;
    private int fade;
    private float fadeFactor;

    public AudioContainer(ChannelAPI channel, String name) {
        super(channel,name);
    }

    protected float getFade() {
        return (float)this.fade*this.fadeFactor;
    }

    public @Nullable AudioTrack getTrack() {
        return this.track;
    }

    @Override
    public float getVolume() {
        return this.fade<=0 ? 1f+getFade() : getFade();
    }

    @Override
    public void play() {
        TriggerAPI trigger = this.channel.getActiveTrigger();
        if(Objects.nonNull(trigger)) setFade(-trigger.getParameterAsInt("fade_in"));
        this.playing = true;
    }

    @Override
    public void playing() {
        if(!this.playing) return;
        if(this.fade>0) {
            if(this.fadeFactor==0f) this.fade = 0;
            else this.fade--;
            if(this.fade==0) {
                if(this.fadeFactor>0f) this.channel.getPlayer().stopTrack();
                this.fadeFactor = 0f;
            }
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
    public void setTrack(AudioTrack track) {
        this.track = track;
    }

    @Override
    public void stop() {
        TriggerAPI trigger = this.channel.getActiveTrigger();
        if(Objects.nonNull(trigger)) setFade(trigger.getParameterAsInt("fade_out"));
    }
}
