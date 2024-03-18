package mods.thecomputerizer.musictriggers.api.data.audio;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerHelper;

import javax.annotation.Nullable;
import java.util.*;

public class AudioPool extends AudioRef {

    public static @Nullable AudioPool unsafeMerge(AudioPool ... pools) {
        return unsafeMerge(Arrays.asList(pools));
    }

    public static @Nullable AudioPool unsafeMerge(Collection<AudioPool> pools) {
        AudioPool merged = null;
        for(AudioPool pool : pools) {
            if(Objects.isNull(merged)) merged = new AudioPool(pool);
            else merged.addAudio(pool);
        }
        return merged;
    }

    private final Set<AudioRef> audio;
    private final Set<AudioRef> playableAudio;
    private final TriggerAPI trigger;
    @Getter private final boolean valid;
    private AudioRef queuedAudio;

    public AudioPool(String name, AudioRef ref) {
        super(ref.getChannel(),name);
        this.audio = new HashSet<>();
        this.playableAudio = new HashSet<>();
        this.trigger = parseTrigger(ref.getTriggers());
        if(Objects.nonNull(this.trigger)) {
            this.audio.add(ref);
            this.valid = true;
        }
        else {
            logWarn("Unable to create audio pool {} from reference audio {}! The triggers were not recognized.",name,ref);
            this.valid = false;
        }
    }

    /**
     * Used for merges only
     */
    private AudioPool(AudioRef ref) {
        super(ref.getChannel(),ref.getName()+"_merge");
        this.audio = new HashSet<>();
        this.playableAudio = new HashSet<>();
        this.trigger = parseTrigger(ref.getTriggers());
        this.audio.add(ref);
        this.valid = true;
    }

    @Override
    public void activate() {
        this.playableAudio.addAll(this.audio);
    }

    public void addAudio(AudioRef ref) {
        this.audio.add(ref);
    }

    protected boolean canRequeue(AudioRef ref) {
        return ref.getParameterAsInt("play_once")<2;
    }

    public void clear() {
        this.audio.clear();
    }

    public boolean hasAudio() {
        return !this.audio.isEmpty();
    }

    @Override
    public boolean matchingTriggers(Collection<TriggerAPI> triggers) {
        return !triggers.isEmpty() && this.trigger.matches(triggers);
    }

    public AudioPool merge(AudioPool ... pools) {
        return merge(Arrays.asList(pools));
    }

    public AudioPool merge(Collection<AudioPool> pools) {
        AudioPool merged = new AudioPool(this);
        for(AudioPool pool : pools) merged.addAudio(pool);
        return merged;
    }

    private TriggerAPI parseTrigger(List<TriggerAPI> triggers) {
        if(triggers.isEmpty()) return null;
        if(triggers.size()==1) return triggers.get(0);
        return TriggerHelper.getCombination(getChannel(),triggers);
    }

    @Override
    public void play() {
        if(Objects.nonNull(this.queuedAudio)) this.queuedAudio.play();
    }

    @Override
    public void playing() {
        if(Objects.nonNull(this.queuedAudio)) this.queuedAudio.playing();
    }

    @Override
    public void queue() {
        AudioRef nextQueue = null;
        this.playableAudio.removeIf(audio -> Objects.nonNull(this.queuedAudio) && audio==this.queuedAudio);
        if(this.playableAudio.isEmpty())
            for(AudioRef ref : this.audio)
                if(canRequeue(ref)) this.playableAudio.add(ref);
        int sum = 0;
        for(AudioRef audio : this.playableAudio)
            if(audio!=this.queuedAudio) sum+=audio.getParameterAsInt("chance");
        int rand = MTRef.randomInt(this.channel,sum);
        for(AudioRef audio : this.playableAudio) {
            rand-=(audio==this.queuedAudio ? 0 : audio.getParameterAsInt("chance"));
            if(rand<=0) nextQueue = audio;
        }
        if(nextQueue instanceof AudioPool) nextQueue.queue();
        this.queuedAudio = nextQueue;
    }

    @Override
    public void stop() {
        if(Objects.nonNull(this.queuedAudio)) this.queuedAudio.stop();
    }

    @Override
    public void stopped() {
        if(Objects.nonNull(this.queuedAudio) && this.queuedAudio.getParameterAsInt("play_once")>0)
            this.playableAudio.remove(this.queuedAudio);
    }
}
