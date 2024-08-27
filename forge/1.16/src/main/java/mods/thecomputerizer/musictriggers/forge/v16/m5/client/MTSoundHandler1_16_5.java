package mods.thecomputerizer.musictriggers.forge.v16.m5.client;

import mcp.MethodsReturnNonnullByDefault;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.core.ReflectionHelper;
import net.minecraft.client.GameSettings;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ISoundEventListener;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Objects;

@MethodsReturnNonnullByDefault @ParametersAreNonnullByDefault
public class MTSoundHandler1_16_5 extends SoundHandler {
    
    private final SoundHandler wrapped;
    
    public MTSoundHandler1_16_5(SoundHandler wrapper, IResourceManager manager, GameSettings options) {
        super(manager,options);
        this.wrapped = wrapper;
    }
    
    @Override public void addListener(ISoundEventListener listener) {
        this.wrapped.addListener(listener);
    }
    
    @Override protected Loader prepare(IResourceManager manager, IProfiler profiler) {
        return (Loader)ReflectionHelper.invokeMethod(this.wrapped.getClass(),"prepare",this.wrapped,new Class<?>[]{
                        IResourceManager.class,IProfiler.class},manager,profiler);
    }
    
    /**
     * I should really make some hooks for version specific ASM
     */
    @Override protected void apply(Loader loader, IResourceManager manager, IProfiler profiler) {
        ReflectionHelper.invokeMethod(this.wrapped.getClass(),"apply",this.wrapped,new Class<?>[]{
                Loader.class,IResourceManager.class,IProfiler.class},loader,manager,profiler);
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
    
    public @Nullable SoundEventAccessor getSoundEvent(ResourceLocation location) {
        return this.wrapped.getSoundEvent(location);
    }
    
    public boolean isActive(ISound sound) {
        return this.wrapped.isActive(sound);
    }
    
    public void queueTickingSound(ITickableSound sound) {
        this.wrapped.queueTickingSound(sound);
    }
    
    public void pause() {
        this.wrapped.pause();
    }
    
    public void play(ISound sound) {
        this.wrapped.play(sound);
    }
    
    public void playDelayed(ISound sound, int delay) {
        this.wrapped.playDelayed(sound,delay);
    }
    
    public void removeListener(ISoundEventListener listener) {
        this.wrapped.removeListener(listener);
    }
    
    public void resume() {
        this.wrapped.resume();
    }
    
    public void stop() {
        this.wrapped.stop();
    }
    
    public void stop(ISound sound) {
        this.wrapped.stop(sound);
    }
    
    public void stop(@Nullable ResourceLocation id, @Nullable SoundCategory category) {
        this.wrapped.stop(id,category);
    }
    
    public void tick(boolean paused) {
        this.wrapped.tick(paused);
    }
    
    public void updateSource(ActiveRenderInfo info) {
        this.wrapped.updateSource(info);
    }
    
    public void updateSourceVolume(SoundCategory category, float volume) {
        ChannelHelper helper = ChannelHelper.getClientHelper();
        if(Objects.nonNull(helper)) helper.setCategoryVolume(category.getName(),volume);
        this.wrapped.updateSourceVolume(category,volume);
    }
}
