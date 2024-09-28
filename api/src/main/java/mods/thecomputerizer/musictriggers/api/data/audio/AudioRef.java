package mods.thecomputerizer.musictriggers.api.data.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.client.gui.MTScreenInfo;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.DataLink;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.WrapperLink;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.util.RandomHelper.WeightedEntry;

import javax.annotation.Nullable;
import java.util.*;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static mods.thecomputerizer.musictriggers.api.data.MTDataRef.AUDIO;
import static mods.thecomputerizer.musictriggers.api.data.MTDataRef.LOOP;

@Getter
public class AudioRef extends ChannelElement implements WeightedEntry {
    
    public static AudioRef addToGui(MTScreenInfo info, String name, String location, boolean file) {
        AudioRef ref = new AudioRef(info.getChannel(),name);
        ref.location = location;
        ref.file = file;
        return ref;
    }

    private final List<TriggerAPI> triggers;
    protected final List<Loop> loops;
    private InterruptHandler interruptHandler;
    protected long inheritedTime;
    protected boolean file;
    protected String location;
    protected boolean loaded;
    protected boolean loading;
    protected boolean queued;
    @Getter protected boolean looping;

    public AudioRef(ChannelAPI channel, String name) {
        super(channel,name);
        this.triggers = new ArrayList<>();
        this.loops = new ArrayList<>();
    }

    @Override public void close() {
        this.triggers.clear();
    }
    
    @Override public void deactivate() {
        this.queued = false;
    }

    public void encode(ByteBuf buf) {
        NetworkHelper.writeString(buf,this.name);
        NetworkHelper.writeList(buf,this.triggers,trigger -> trigger.encode(buf));
    }
    
    @Override public Collection<DataLink> getChildWrappers(MTScreenInfo parent) {
        if(Objects.isNull(this.interruptHandler))
            this.interruptHandler = new InterruptHandler(this.channel,Toml.getEmpty());
        DataLink interrupt = this.interruptHandler.getLink();
        interrupt.setType(parent.next("interrupt_handler",interrupt));
        WrapperLink loops = new WrapperLink(this.loops);
        loops.setType(parent.next("loops",loops));
        return Arrays.asList(interrupt,loops);
    }
    
    public int getPlayState() {
        return getParameterAsInt("play_once");
    }
    
    @Override public TableRef getReferenceData() {
        return AUDIO;
    }
    
    public double getSpeed() {
        return getParameterAsDouble("speed");
    }
    
    @Override protected String getSubTypeName() {
        return "Audio";
    }
    
    @Override public Class<? extends ChannelElement> getTypeClass() {
        return AudioRef.class;
    }
    
    public float getVolume(boolean unpaused) {
        return getParameterAsFloat(unpaused ? "volume" : "volume_when_paused");
    }
    
    @Override public int getWeight() {
        return getParameterAsInt("chance");
    }
    
    public boolean hasPlayedEnough(int count) {
        return count>=getParameterAsInt("play_x");
    }

    @Override public boolean isResource() {
        return false;
    }

    public void loadLocal(String location) {}

    public void loadRemote(String location) {}
    
    public boolean parse(Toml table) {
        if(super.parse(table) && parseTriggers(this.channel,this.triggers)) {
            logDebug("Successfully parsed with triggers {}",this.triggers);
            if(table.hasTable("interrupt_handler"))
                this.interruptHandler = new InterruptHandler(this.channel,table.getTable("interrupt_handler"));
            else this.interruptHandler = new InterruptHandler(this.channel,Toml.getEmpty());
            if(table.hasTable("loop")) {
                for(Toml loopTable : table.getTableArray("loop")) {
                    Loop loop = new Loop(this.channel,loopTable);
                    if(loop.valid) this.loops.add(loop);
                }
                logInfo("Registered loops: {}",this.loops);
            }
            return true;
        }
        logError("Failed to parse");
        return false;
    }
    
    public void queryInterrupt(@Nullable TriggerAPI next, AudioPlayer player) {}

    /**
     * fade<0 = fade in
     * fade>0 = fade out
     */
    public void setFade(int fade) {}

    public void setItem(AudioItem item) {
        this.loading = false;
        this.loaded = true;
    }
    
    public void setLoading() {
        this.loaded = false;
        this.loading = true;
    }
    
    @Override public void setWeight(int i) {}

    public void start(TriggerAPI trigger, boolean unpaused) {}
    
    @Override protected Toml toTomlExtra(Toml toml) {
        toml = super.toTomlExtra(toml);
        if(Objects.nonNull(this.interruptHandler))
            toml.addTable("interrupt_handler",this.interruptHandler.toToml());
        for(Loop loop :  this.loops) toml.addTable("loop",loop.toToml());
        return toml;
    }
    
    public static class InterruptHandler extends ChannelElement {

        private final int priority;
        private final List<TriggerAPI> triggers;

        public InterruptHandler(ChannelAPI channel, Toml table) {
            super(channel,"interrupt_handler");
            List<TriggerAPI> triggers = new ArrayList<>();
            int priority = ChannelHelper.getDebugBool("reverse_priority") ? MAX_VALUE : MIN_VALUE;
            if(parse(table) && parseTriggers(this.channel,triggers,"trigger_whitelist"))
                priority = getParameterAsInt("priority");
            this.priority = priority;
            this.triggers = triggers;
        }

        public boolean isInterrputedBy(@Nullable TriggerAPI trigger) {
            if(Objects.isNull(trigger)) return false;
            if(this.triggers.isEmpty()) return true;
            int priority = trigger.getParameterAsInt("priority");
            return this.triggers.isEmpty() || (ChannelHelper.getDebugBool("reverse_priority") ?
                    priority<=this.priority : priority>=this.priority) || trigger.isContained(this.triggers);
        }

        @Override     public boolean isResource() {
            return false;
        }

        @Override     public void close() {
            this.triggers.clear();
        }
        
        @Override public TableRef getReferenceData() {
            return MTDataRef.INTERRUPT_HANDLER;
        }
        
        @Override public Class<? extends ChannelElement> getTypeClass() {
            return AudioRef.class;
        }
        
        @Override protected String getSubTypeName() {
            return "Interrupt_Handler";
        }
    }
    
    @Getter
    public static class Loop extends ChannelElement {
        
        public static Loop addToGui(MTScreenInfo info) {
            return new Loop(info.getChannel(),Toml.getEmpty());
        }
        
        final boolean valid;
        final long from;
        final long to;
        final int total;
        int count;
        
        protected Loop(ChannelAPI channel, Toml table) {
            this(channel,table,false);
        }
        
        Loop(ChannelAPI channel, Toml table, boolean silent) {
            super(channel,"loop_point");
            boolean valid = parse(table);
            this.from = getParameterAsLong("from");
            this.to = getParameterAsLong("to");
            this.total = getParameterAsInt("loop_count");
            if(this.from==this.to) {
                if(!silent) logError("Cannot define loops with equal from and to values! {} = {}",this.from,this.to);
                valid = false;
            }
            this.valid = valid;
        }
        
        @Override public void close() {}
        
        @Override public String getName() {
            return String.format("(%1$d->%2$d)x%3$d",this.from,this.to,this.total);
        }
        
        @Override public TableRef getReferenceData() {
            return LOOP;
        }
        
        @Override protected String getSubTypeName() {
            return "Loop";
        }
        
        @Override public Class<? extends ParameterWrapper> getTypeClass() {
            return Loop.class;
        }
        
        @Override public boolean isResource() {
            return false;
        }
        
        public void reset() {
            this.count = 0;
        }
        
        public boolean run() {
            AudioTrack track = this.channel.getPlayer().getPlayingTrack();
            if(Objects.nonNull(track) && (this.total<=0 || this.count<this.total) && this.channel.getPlayingSongTime()>=this.from) {
                logInfo("Running");
                track.setPosition(this.to);
                this.count++;
                return true;
            }
            return false;
        }
    }
}