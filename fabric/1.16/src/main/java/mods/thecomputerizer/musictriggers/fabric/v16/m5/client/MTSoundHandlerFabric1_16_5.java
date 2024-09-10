package mods.thecomputerizer.musictriggers.fabric.v16.m5.client;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.core.ReflectionHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Options;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.ProfilerFiller;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class MTSoundHandlerFabric1_16_5 extends SoundManager {
    
    private final SoundManager wrapped;
    
    public MTSoundHandlerFabric1_16_5(SoundManager wrapper, ResourceManager manager, Options options) {
        super(manager,options);
        this.wrapped = wrapper;
    }
    
    @Override public void addListener(SoundEventListener listener) {
        this.wrapped.addListener(listener);
    }
    
    @Override protected Preparations prepare(ResourceManager manager, ProfilerFiller profiler) {
        return (Preparations)ReflectionHelper.invokeMethod(this.wrapped.getClass(),"prepare",this.wrapped,
                new Class<?>[]{ResourceManager.class,ProfilerFiller.class},manager,profiler);
    }
    
    /**
     * I should really make some hooks for version specific ASM
     */
    @Override protected void apply(Preparations loader, ResourceManager manager, ProfilerFiller profiler) {
        ReflectionHelper.invokeMethod(this.wrapped.getClass(),"apply",this.wrapped,new Class<?>[]{
                Preparations.class,ResourceManager.class,ProfilerFiller.class},loader,manager,profiler);
    }
    
    public void destroy() {
        this.wrapped.destroy();
    }
    
    public Collection<ResourceLocation> getAvailableSounds() {
        return this.wrapped.getAvailableSounds();
    }
    
    public String getDebugString() {
        return this.wrapped.getDebugString();
    }
    
    public @Nullable WeighedSoundEvents getSoundEvent(ResourceLocation location) {
        return this.wrapped.getSoundEvent(location);
    }
    
    public boolean isActive(SoundInstance sound) {
        return this.wrapped.isActive(sound);
    }
    
    public void queueTickingSound(TickableSoundInstance sound) {
        this.wrapped.queueTickingSound(sound);
    }
    
    public void pause() {
        this.wrapped.pause();
    }
    
    public void play(SoundInstance sound) {
        this.wrapped.play(sound);
    }
    
    public void playDelayed(SoundInstance sound, int delay) {
        this.wrapped.playDelayed(sound,delay);
    }
    
    public void removeListener(SoundEventListener listener) {
        this.wrapped.removeListener(listener);
    }
    
    public void resume() {
        this.wrapped.resume();
    }
    
    public void stop() {
        this.wrapped.stop();
    }
    
    public void stop(SoundInstance sound) {
        this.wrapped.stop(sound);
    }
    
    public void stop(@Nullable ResourceLocation id, @Nullable SoundSource category) {
        this.wrapped.stop(id,category);
    }
    
    public void tick(boolean paused) {
        this.wrapped.tick(paused);
    }
    
    public void updateSource(Camera info) {
        this.wrapped.updateSource(info);
    }
    
    public void updateSourceVolume(SoundSource category, float volume) {
        ChannelHelper helper = ChannelHelper.getClientHelper();
        if(Objects.nonNull(helper)) helper.setCategoryVolume(category.getName(),volume);
        this.wrapped.updateSourceVolume(category,volume);
    }
}