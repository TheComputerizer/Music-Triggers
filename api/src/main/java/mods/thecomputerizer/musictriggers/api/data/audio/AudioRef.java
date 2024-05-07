package mods.thecomputerizer.musictriggers.api.data.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import javax.annotation.Nullable;
import java.util.*;

@Getter
public class AudioRef extends ChannelElement {

    private final List<TriggerAPI> triggers;
    private InterruptHandler interruptHandler;

    public AudioRef(ChannelAPI channel, String name) {
        super(channel,name);
        this.triggers = new ArrayList<>();
    }

    @Override
    public void close() {
        this.triggers.clear();
    }

    public void encode(ByteBuf buf) {
        NetworkHelper.writeString(buf,this.name);
        NetworkHelper.writeList(buf,this.triggers,trigger -> trigger.encode(buf));
    }

    public float getVolume() {
        return 0f;
    }
    
    @Override protected TableRef getReferenceData() {
        return MTDataRef.AUDIO;
    }
    
    @Override
    public Class<? extends ChannelElement> getTypeClass() {
        return AudioRef.class;
    }

    @Override
    protected String getSubTypeName() {
        return "Audio";
    }

    public void queryInterrupt(@Nullable TriggerAPI next, AudioPlayer player) {}

    public boolean isLoaded() {
        return false;
    }

    @Override
    public boolean isResource() {
        return false;
    }

    public void loadLocal(String location) {}

    public void loadRemote(String location) {}

    public boolean parse(Toml table) {
        if(super.parse(table) && parseTriggers(this.channel,this.triggers)) {
            logDebug("Successfully parsed triggers {}",this.triggers);
            if(table.hasTable("interrupt_handler"))
                this.interruptHandler = new InterruptHandler(this.channel,table.getTable("interrupt_handler"));
        }
        logError("Failed to parse audio");
        return false;
    }

    /**
     * fade<0 = fade in
     * fade>0 = fade out
     */
    public void setFade(int fade) {}

    public void setItem(AudioItem item) {}

    public void start(TriggerAPI trigger) {}

    @Override
    public String toString() {
        return getTypeName();
    }

    public static class InterruptHandler extends ChannelElement {

        private final int priority;
        private final List<TriggerAPI> triggers;

        public InterruptHandler(ChannelAPI channel, Toml table) {
            super(channel,"interrupt_handler");
            List<TriggerAPI> triggers = new ArrayList<>();
            if(parse(table) && parseTriggers(this.channel,triggers,"trigger_whitelist"))
                this.priority = getParameterAsInt("priority");
            else this.priority = this.channel.getHelper().getDebugBool("reverse_priority") ?
                    Integer.MAX_VALUE : Integer.MIN_VALUE;
            this.triggers = triggers;
        }

        public boolean isInterrputedBy(@Nullable TriggerAPI trigger) {
            if(Objects.isNull(trigger)) return false;
            if(this.triggers.isEmpty()) return true;
            int priority = trigger.getParameterAsInt("priority");
            return this.triggers.isEmpty() || (this.channel.getHelper().getDebugBool("reverse_priority") ?
                    priority<=this.priority : priority>=this.priority) || trigger.isContained(this.triggers);
        }

        @Override
        public boolean isResource() {
            return false;
        }

        @Override
        public void close() {
            this.triggers.clear();
        }
        
        @Override protected TableRef getReferenceData() {
            return MTDataRef.INTERRUPT_HANDLER;
        }
        
        @Override protected Class<? extends ChannelElement> getTypeClass() {
            return AudioRef.class;
        }
        
        @Override protected String getSubTypeName() {
            return "Interrupt_Handler";
        }
    }
}