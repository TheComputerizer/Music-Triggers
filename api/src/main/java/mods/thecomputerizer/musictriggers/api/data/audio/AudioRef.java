package mods.thecomputerizer.musictriggers.api.data.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterDouble;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterFloat;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import javax.annotation.Nullable;
import java.util.*;

@Getter
public class AudioRef extends ParameterWrapper {

    private final String name;
    private final List<TriggerAPI> triggers;
    private InterruptHandler interruptHandler;

    public AudioRef(ChannelAPI channel, String name) {
        super(channel);
        this.name = name;
        this.triggers = new ArrayList<>();
    }

    public String audioMsg(String msg) {
        return getTypeName()+": "+msg;
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

    @Override
    public Class<? extends ChannelElement> getTypeClass() {
        return AudioRef.class;
    }

    @Override
    protected String getTypeName() {
        return "Audio["+getName()+"]";
    }

    @Override
    protected Map<String,Parameter<?>> initParameterMap() { //TODO Move filters into 1 or more subtables
        Map<String,Parameter<?>> map = new HashMap<>();
        addParameter(map,"chance",new ParameterInt(100));
        addParameter(map,"file_name",new ParameterString(""));
        addParameter(map,"pitch",new ParameterDouble(1d));
        addParameter(map,"play_once",new ParameterInt(0));
        addParameter(map,"play_x",new ParameterInt(1));
        addParameter(map,"resume_on_play",new ParameterBoolean(false));
        addParameter(map,"rotation_speed",new ParameterDouble(0d));
        addParameter(map,"speed",new ParameterDouble(1d));
        addParameter(map,"start_at",new ParameterInt(0));
        addParameter(map,"volume",new ParameterFloat(1f));
        initExtraParameters(map);
        return map;
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {}

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
        List<?> triggerRefs = table.getValueArray("triggers");
        if(!TriggerHelper.findTriggers(getChannel(),this.triggers,triggerRefs)) {
            logError(audioMsg("Failed to parse triggers {}!"),triggerRefs);
            return false;
        } else logDebug(audioMsg("Successfully parsed triggers {}"),this.triggers);
        if(table.hasTable("must_finish"))
            this.interruptHandler = new InterruptHandler(this,table.getTable("must_finish"));
        return parseParameters(table);
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

    @Override
    public boolean verifyRequiredParameters() {
        return true;
    }

    public static class InterruptHandler extends ChannelElement {

        private final int priority;
        private final List<TriggerAPI> triggers;

        public InterruptHandler(AudioRef parent, Toml table) {
            super(parent.getChannel());
            this.priority = table.getValueInt("priority",
                    getChannel().getHelper().getDebugBool("REVERSE_PRIORITY") ? Integer.MAX_VALUE : Integer.MIN_VALUE);
            this.triggers = parseTriggers(parent,table.getValueArray("trigger_whitelist"));
        }

        public boolean isInterrputedBy(@Nullable TriggerAPI trigger) {
            if(Objects.isNull(trigger)) return false;
            if(this.triggers.isEmpty()) return true;
            int priority = trigger.getParameterAsInt("priority");
            return this.triggers.isEmpty() || (getChannel().getHelper().getDebugBool("REVERSE_PRIORITY") ?
                    priority<=this.priority : priority>=this.priority) || trigger.isContained(this.triggers);
        }

        @Override
        public boolean isResource() {
            return false;
        }

        private List<TriggerAPI> parseTriggers(AudioRef ref, List<?> triggerRefs) {
            List<TriggerAPI> triggers = new ArrayList<>();
            if(!TriggerHelper.findTriggers(getChannel(),triggers,triggerRefs)) {
                logError(ref.audioMsg("Failed to parse 1 or more triggers in must_finish table!"));
                return Collections.emptyList();
            }
            return triggers;
        }

        @Override
        public void close() {
            this.triggers.clear();
        }
    }
}