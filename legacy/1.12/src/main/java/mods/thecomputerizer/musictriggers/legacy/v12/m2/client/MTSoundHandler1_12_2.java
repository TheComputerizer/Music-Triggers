package mods.thecomputerizer.musictriggers.legacy.v12.m2.client;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ISoundEventListener;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class MTSoundHandler1_12_2 extends SoundHandler {
    
    private final SoundHandler wrapped;
    
    public MTSoundHandler1_12_2(IResourceManager manager, GameSettings settings, SoundHandler wrapped) {
        super(manager,settings);
        this.wrapped = wrapped;
    }
    
    @Override
    public void onResourceManagerReload(IResourceManager manager) {
        this.wrapped.onResourceManagerReload(manager);
    }
    
    @Override
    public void setSoundLevel(@Nonnull SoundCategory category, float volume) {
        ChannelHelper helper = ChannelHelper.getClientHelper();
        if(Objects.nonNull(helper)) helper.setCategoryVolume(category.getName(),volume);
        this.wrapped.setSoundLevel(category,volume);
    }
    
    @Override
    public SoundEventAccessor getAccessor(ResourceLocation location) {
        return this.wrapped.getAccessor(location);
    }
    
    public void playSound(ISound sound) {
        this.wrapped.playSound(sound);
    }
    
    public void playDelayedSound(ISound sound, int delay) {
        this.wrapped.playDelayedSound(sound,delay);
    }
    
    public void setListener(EntityPlayer player, float f) {
        this.wrapped.setListener(player,f);
    }
    
    public void setListener(Entity entity, float partialTicks) {
        this.wrapped.setListener(entity,partialTicks);
    }
    
    public void pauseSounds() {
        this.wrapped.pauseSounds();
    }
    
    public void stopSounds() {
        this.wrapped.stopSounds();
    }
    
    public void unloadSounds() {
        this.wrapped.unloadSounds();
    }
    
    public void update() {
        this.wrapped.update();
    }
    
    public void resumeSounds() {
        this.wrapped.resumeSounds();
    }
    
    public void stopSound(ISound sound) {
        this.wrapped.stopSound(sound);
    }
    
    public boolean isSoundPlaying(ISound sound) {
        return this.wrapped.isSoundPlaying(sound);
    }
    
    public void addListener(ISoundEventListener listener) {
        this.wrapped.addListener(listener);
    }
    
    public void removeListener(ISoundEventListener listener) {
        this.wrapped.removeListener(listener);
    }
    
    public void stop(String sound, SoundCategory category) {
        this.wrapped.stop(sound,category);
    }
}