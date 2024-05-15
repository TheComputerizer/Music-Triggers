package mods.thecomputerizer.musictriggers.api.data.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelEventHandler;
import mods.thecomputerizer.musictriggers.api.data.nbt.storage.NBTLoadable;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.UniversalParameters;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.ListTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.TagHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.util.RandomHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AudioPool extends AudioRef implements NBTLoadable {

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
    @Getter private AudioRef queuedAudio;

    public AudioPool(String name, AudioRef ref) {
        super(ref.getChannel(),name);
        this.audio = new HashSet<>();
        this.playableAudio = new HashSet<>();
        this.trigger = parseTrigger(ref.getTriggers());
        if(Objects.nonNull(this.trigger)) {
            this.audio.add(ref);
            this.playableAudio.add(ref);
            this.valid = true;
        }
        else {
            ref.logWarn("Unable to create audio pool {} from reference! The triggers were not recognized.",name);
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
        this.playableAudio.add(ref);
        this.valid = true;
    }

    @Override
    public void activate() {
        this.playableAudio.addAll(this.audio);
    }

    public void addAudio(AudioRef ref) {
        this.audio.add(ref);
        this.playableAudio.add(ref);
    }
    
    @Override
    public void addHandlers(Collection<ChannelEventHandler> handlers) {
        handlers.add(this);
        this.audio.forEach(ref -> ref.addHandlers(handlers));
    }

    @Override
    public void close() {
        super.close();
        this.audio.clear();
        this.playableAudio.clear();
        this.queuedAudio = null;
    }
    
    @Override
    public void deactivate() {
        recalculatePlayable(i -> i<=2);
    }

    @Override
    public void encode(ByteBuf buf) {
        NetworkHelper.writeString(buf,"pool");
        buf.writeInt(this.audio.size());
        for(AudioRef ref : this.audio) ref.encode(buf);
    }
    
    public @Nullable AudioRef getAudioForName(String name) {
        for(AudioRef ref : this.audio)
            if(ref.getName().equals(name)) return ref;
        return null;
    }
    
    public List<AudioRef> getFlattened() {
        List<AudioRef> flattened = new ArrayList<>();
        getFlattened(flattened,this);
        return flattened;
    }
    
    private void getFlattened(List<AudioRef> flattened, AudioPool pool) {
        for(AudioRef ref : pool.audio) {
            if(ref instanceof AudioPool) getFlattened(flattened,(AudioPool)ref);
            else flattened.add(ref);
        }
    }

    @Override
    public @Nullable InterruptHandler getInterruptHandler() {
        return Objects.nonNull(this.queuedAudio) ? this.queuedAudio.getInterruptHandler() : null;
    }
    
    @Override
    public String getName() {
        StringJoiner refJoinfer = new StringJoiner("+");
        this.audio.forEach(ref -> refJoinfer.add(ref.getName()));
        return this.audio.size()==1 ? refJoinfer.toString() : "pool = "+refJoinfer;
    }
    
    @Override
    public @Nullable Parameter<?> getParameter(String name) {
        return Objects.nonNull(this.queuedAudio) ? this.queuedAudio.getParameter(name) : super.getParameter(name);
    }

    @Override
    public float getVolume() {
        return Objects.nonNull(this.queuedAudio) ? this.queuedAudio.getVolume() : 0f;
    }

    public boolean hasAudio() {
        return !this.audio.isEmpty();
    }

    @Override
    public void queryInterrupt(@Nullable TriggerAPI next, AudioPlayer player) {
        if(Objects.isNull(this.queuedAudio)) this.channel.getPlayer().stopTrack();
        else this.queuedAudio.queryInterrupt(trigger,player);
    }
    
    @Override
    public void onConnected(CompoundTagAPI<?> worldData) {
        ListTagAPI<?> audioList = worldData.getListTag("audio");
        if(Objects.nonNull(audioList)) {
            audioList.iterable().forEach(audio -> {
                CompoundTagAPI<?> audioTag = audio.asCompoundTag();
                AudioRef ref = getAudioForName(audioTag.getString("name"));
                if(Objects.nonNull(ref) && hasPlayedEnough(audioTag.getPrimitiveTag("play_count").asInt()))
                    this.playableAudio.remove(ref);
            });
        }
    }
    
    public void onDisconnected() {
        recalculatePlayable(i -> i==3);
    }
    
    @Override
    public void onLoaded(CompoundTagAPI<?> globalData) {
    
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
        if(!this.valid || this.audio.isEmpty()) return;
        AudioRef nextQueue = null;
        if(this.playableAudio.isEmpty()) recalculatePlayable(i -> i<=1);
        int sum = 0;
        for(AudioRef audio : this.playableAudio)
            if(audio!=this.queuedAudio) sum+=audio.getParameterAsInt("chance");
        int rand = sum>0 ? RandomHelper.randomInt(sum) : 0;
        for(AudioRef audio : this.playableAudio) {
            rand-=(audio==this.queuedAudio ? 0 : audio.getParameterAsInt("chance"));
            if(rand<=0) nextQueue = audio;
        }
        if(nextQueue instanceof AudioPool) nextQueue.queue();
        this.queuedAudio = nextQueue;
        logInfo("Queued reference {}",this.queuedAudio);
    }
    
    protected void recalculatePlayable(Function<Integer,Boolean> func) {
        this.audio.forEach(ref -> {
            if(!this.playableAudio.contains(ref) && func.apply(ref.getPlayState()))
                this.playableAudio.add(ref);
        });
    }
    
    @Override
    public void saveGlobalTo(CompoundTagAPI<?> globalData) {}
    
    @Override
    public void saveWorldTo(CompoundTagAPI<?> worldData) {
        List<AudioRef> played = this.audio.stream()
                .filter(ref -> !this.playableAudio.contains(ref) && ref.getPlayState()==4)
                .collect(Collectors.toList());
        if(played.isEmpty()) return;
        ListTagAPI<?> tag = TagHelper.makeListTag();
        played.forEach(ref -> {
            CompoundTagAPI<?> audioTag = TagHelper.makeCompoundTag();
            audioTag.putString("name",ref.getName());
            audioTag.putInt("play_count",1);
            tag.addTag(audioTag);
        });
        worldData.putTag("audio",tag);
    }
    
    @Override
    public void setUniversals(UniversalParameters universals) {
        super.setUniversals(universals);
        for(AudioRef ref : this.audio) ref.setUniversals(universals);
    }

    @Override
    public void start(TriggerAPI trigger) {
        if(Objects.nonNull(this.queuedAudio)) this.queuedAudio.start(trigger);
        else logDebug("Why was the queued reference null");
    }

    @Override
    public void stop() {
        if(Objects.nonNull(this.queuedAudio)) this.queuedAudio.stop();
    }

    @Override
    public void stopped() {
        if(Objects.nonNull(this.queuedAudio)) {
            this.queuedAudio.stopped();
            this.playableAudio.remove(this.queuedAudio);
            this.queuedAudio = null;
        }
    }
}
