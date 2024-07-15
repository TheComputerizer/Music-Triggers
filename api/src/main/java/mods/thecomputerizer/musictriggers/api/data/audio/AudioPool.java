package mods.thecomputerizer.musictriggers.api.data.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelEventHandler;
import mods.thecomputerizer.musictriggers.api.data.nbt.NBTLoadable;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.UniversalParameters;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.BaseTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.ListTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.TagHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.util.RandomHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AudioPool extends AudioRef implements NBTLoadable {

    private final Set<AudioRef> audio;
    private final Set<AudioRef> playableAudio;
    private final TriggerAPI trigger;
    @Getter private AudioRef queuedAudio;

    public AudioPool(TriggerAPI trigger) {
        super(trigger.getChannel(),"audio_pool");
        this.audio = new HashSet<>();
        this.playableAudio = new HashSet<>();
        this.trigger = trigger;
    }

    @Override
    public void close() {
        super.close();
        this.audio.clear();
        this.playableAudio.clear();
        this.queuedAudio = null;
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
        for(AudioRef ref : this.audio) refJoinfer.add(ref.getName());
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
        if(this.playableAudio.isEmpty()) recalculatePlayable(i -> i<=1);
        return !this.playableAudio.isEmpty();
    }
    
    @Override
    public boolean hasDataToSave() {
        for(AudioRef ref : this.audio)
            if(!this.playableAudio.contains(ref) && ref.getPlayState()==4) return true;
        return false;
    }
    
    public void injectHandlers(AudioRef ref, Collection<ChannelEventHandler> handlers) {
        this.audio.add(ref);
        this.playableAudio.add(ref);
        handlers.add(this);
        handlers.addAll(ref.loops);
    }
    
    @Override
    public boolean isLoaded() {
        return Objects.nonNull(this.queuedAudio) && !this.queuedAudio.isLoading() && this.queuedAudio.isLoaded();
    }
    
    @Override
    public boolean isQueued() {
        return hasQueue() && this.queuedAudio.isQueued();
    }
    
    public boolean hasQueue() {
        return Objects.nonNull(this.queuedAudio);
    }

    @Override
    public void queryInterrupt(@Nullable TriggerAPI next, AudioPlayer player) {
        if(Objects.isNull(this.queuedAudio)) this.channel.getPlayer().stopCurrentTrack();
        else this.queuedAudio.queryInterrupt(trigger,player);
    }
    
    @Override
    public void onConnected(CompoundTagAPI<?> worldData) {
        if(worldData.contains("audio")) {
            for(BaseTagAPI<?> audio : worldData.getListTag("audio")) {
                CompoundTagAPI<?> audioTag = audio.asCompoundTag();
                AudioRef ref = getAudioForName(audioTag.getString("name"));
                if(Objects.nonNull(ref) && hasPlayedEnough(audioTag.getPrimitiveTag("play_count").asInt()))
                    this.playableAudio.remove(ref);
            }
        }
    }
    
    public void onDisconnected() {
        recalculatePlayable(i -> i==3);
    }
    
    @Override
    public void onLoaded(CompoundTagAPI<?> globalData) {}

    @Override
    public void play() {
        if(Objects.nonNull(this.queuedAudio)) this.queuedAudio.play();
    }
    
    @Override
    public void playable() {
        recalculatePlayable(i -> i<=2);
    }

    @Override
    public void playing() {
        if(Objects.nonNull(this.queuedAudio)) this.queuedAudio.playing();
    }

    @Override
    public void queue() {
        if(this.playableAudio.isEmpty()) return;
        AudioRef nextQueue = RandomHelper.getWeightedEntry(ThreadLocalRandom.current(),this.playableAudio);
        if(Objects.isNull(nextQueue)) return;
        if(nextQueue instanceof AudioPool) nextQueue.queue();
        this.queuedAudio = nextQueue;
    }
    
    protected void recalculatePlayable(Function<Integer,Boolean> func) {
        for(AudioRef ref : this.audio)
            if(func.apply(ref.getPlayState())) this.playableAudio.add(ref);
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
        for(AudioRef ref : played) {
            CompoundTagAPI<?> audioTag = TagHelper.makeCompoundTag();
            audioTag.putString("name",ref.getName());
            audioTag.putInt("play_count",1);
            tag.addTag(audioTag);
        }
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
        else logWarn("Tried to start empty queue from {}",trigger);
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
            if(this.playableAudio.isEmpty()) recalculatePlayable(i -> i<=1);
            this.queuedAudio = null;
        }
    }
}
