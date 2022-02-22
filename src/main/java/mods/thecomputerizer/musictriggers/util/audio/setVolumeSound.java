package mods.thecomputerizer.musictriggers.util.audio;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@SuppressWarnings("ALL")
@Mod.EventBusSubscriber(modid = MusicTriggers.MODID, value = Dist.CLIENT)
public class setVolumeSound implements SoundInstance {

    protected Sound sound;
    @Nullable
    private WeighedSoundEvents soundEvent;
    protected SoundSource category;
    protected ResourceLocation positionedSoundLocation;
    protected float volume;
    protected float pitch;
    protected float xPosF;
    protected float yPosF;
    protected float zPosF;
    protected boolean repeat;
    protected int repeatDelay;
    protected SoundInstance.Attenuation attenuationType;

    public setVolumeSound(ResourceLocation soundId, SoundSource categoryIn, float volume, float pitch, boolean repeat, int repeatDelay, SoundInstance.Attenuation soundAttenuation, float xPosF, float yPosF, float zPosF)
    {
        this.positionedSoundLocation = soundId;
        this.category = categoryIn;
        this.volume = volume;
        this.pitch = pitch;
        this.repeat = repeat;
        this.repeatDelay = repeatDelay;
        this.attenuationType = soundAttenuation;
        this.xPosF = xPosF;
        this.yPosF = yPosF;
        this.zPosF = zPosF;
    }

    public ResourceLocation getLocation()
    {
        return this.positionedSoundLocation;
    }

    public WeighedSoundEvents resolve(SoundManager handler)
    {
        WeighedSoundEvents soundeventaccessor = handler.getSoundEvent(this.positionedSoundLocation);
        if (soundeventaccessor == null) {
            this.sound = SoundManager.EMPTY_SOUND;
        } else {
            this.sound = soundeventaccessor.getSound();
        }

        return soundeventaccessor;
    }

    public Sound getSound()
    {
        return this.sound;
    }

    public SoundSource getSource()
    {
        return this.category;
    }

    public boolean isLooping()
    {
        return this.repeat;
    }

    public boolean isRelative()
    {
        return false;
    }

    public int getDelay()
    {
        return this.repeatDelay;
    }

    public float getVolume()
    {
        return this.volume * this.sound.getVolume();
    }

    public float getPitch()
    {
        return this.pitch * this.sound.getPitch();
    }

    public double getX()
    {
        return this.xPosF;
    }

    public double getY()
    {
        return this.yPosF;
    }

    public double getZ()
    {
        return this.zPosF;
    }

    public SoundInstance.Attenuation getAttenuation()
    {
        return this.attenuationType;
    }

    public void setVolume(float vol) { this.volume = vol;}
}
