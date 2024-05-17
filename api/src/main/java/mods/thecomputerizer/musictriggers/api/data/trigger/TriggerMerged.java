package mods.thecomputerizer.musictriggers.api.data.trigger;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioPool;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.UniversalParameters;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.util.RandomHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Only used when independent_audio_pools is disabled
 */
@Getter
public class TriggerMerged extends TriggerAPI {

    private final Collection<TriggerAPI> triggers;
    private TriggerAPI playing;

    public TriggerMerged(ChannelAPI channel, Collection<TriggerAPI> triggers) {
        super(channel,"merged");
        this.triggers = triggers;
    }
    
    @Override
    public void activate() {
        super.activate();
        this.triggers.forEach(TriggerAPI::activate);
    }

    @Override
    public void close() {
        super.close();
        this.triggers.forEach(TriggerAPI::close);
        this.triggers.clear();
        this.playing = null;
    }
    
    @Override
    public void deactivate() {
        super.deactivate();
        this.triggers.forEach(TriggerAPI::deactivate);
    }

    @Override
    public void encode(ByteBuf buf) {
        super.encode(buf);
        NetworkHelper.writeCollection(buf,this.triggers,trigger -> trigger.encode(buf));
    }
    
    protected void executePlaying(Consumer<TriggerAPI> executor, Consumer<Void> superExecutor) {
        TriggerAPI playing = getOrSetPlaying();
        if(Objects.nonNull(playing)) executor.accept(playing);
        else superExecutor.accept(null);
    }

    @Override
    public @Nullable AudioPool getAudioPool() {
        return returnPlaying(TriggerAPI::getAudioPool,super::getAudioPool);
    }
    
    @Override
    public String getName() {
        StringJoiner joiner = new StringJoiner("+");
        for(TriggerAPI trigger : this.triggers) joiner.add(trigger.getNameWithID());
        return this.triggers.size()==1 ? joiner.toString() : "merged = "+joiner;
    }
    
    @Override
    public String getNameWithID() {
        return getName();
    }
    
    protected TriggerAPI getOrSetPlaying() {
        if(Objects.isNull(this.playing)) this.playing = getRandomTrigger();
        return this.playing;
    }
    
    @Override public @Nullable Parameter<?> getParameter(String name) {
        return returnPlaying(playing -> playing.getParameter(name),() -> super.getParameter(name));
    }
    
    public @Nullable TriggerAPI getRandomTrigger() {
        if(this.triggers.isEmpty()) return null;
        TriggerAPI trigger = RandomHelper.getBasicRandomEntry(this.triggers);
        return trigger instanceof TriggerMerged ? ((TriggerMerged)trigger).getRandomTrigger() : trigger;
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {}

    @Override
    public boolean isPlayableContext(TriggerContext context) {
        for(TriggerAPI trigger : this.triggers)
            if(trigger.isPlayableContext(context)) return true;
        return false;
    }

    @Override
    public boolean isContained(Collection<TriggerAPI> triggers) {
        for(TriggerAPI trigger : this.triggers)
            if(trigger.isContained(triggers)) return true;
        return false;
    }
    
    public boolean matches(Collection<TriggerAPI> triggers) {
        return TriggerHelper.matchesAll(this.triggers,triggers);
    }

    @Override
    public boolean matches(TriggerAPI other) {
        if(other instanceof TriggerMerged) return TriggerHelper.matchesAny(this.triggers,((TriggerMerged)other).triggers);
        if(other instanceof TriggerCombination) return other.isContained(this.triggers);
        for(TriggerAPI trigger : this.triggers)
            if(trigger.matches(other)) return true;
        return false;
    }

    @Override
    public void queue() {
        executePlaying(TriggerAPI::queue,v -> super.queue());
    }
    
    protected <V> V returnPlaying(Function<TriggerAPI,V> func, Supplier<V> superReturn) {
        TriggerAPI playing = getOrSetPlaying();
        return Objects.nonNull(playing) ? func.apply(playing) : superReturn.get();
    }
    
    @Override
    public void setUniversals(UniversalParameters universals) {
        super.setUniversals(universals);
        this.triggers.forEach(trigger -> trigger.setUniversals(universals));
    }

    @Override
    public void stopped() {
        executePlaying(playing -> {
            playing.stopped();
            this.playing = null;
        },v -> {});
    }
}
