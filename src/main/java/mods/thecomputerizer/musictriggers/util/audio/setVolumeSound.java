package mods.thecomputerizer.musictriggers.util.audio;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@SuppressWarnings("ALL")
@Mod.EventBusSubscriber(modid = MusicTriggers.MODID, value = Dist.CLIENT)
public class setVolumeSound implements ISound {

    protected Sound sound;
    @Nullable
    private SoundEventAccessor soundEvent;
    protected SoundCategory category;
    protected ResourceLocation positionedSoundLocation;
    protected float volume;
    protected float pitch;
    protected float xPosF;
    protected float yPosF;
    protected float zPosF;
    protected boolean repeat;
    protected int repeatDelay;
    protected ISound.AttenuationType attenuationType;

    public setVolumeSound(ResourceLocation soundId, SoundCategory categoryIn, float volume, float pitch, boolean repeat, int repeatDelay, ISound.AttenuationType soundAttenuation, float xPosF, float yPosF, float zPosF)
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

    public SoundEventAccessor resolve(SoundHandler handler)
    {
        SoundEventAccessor soundeventaccessor = handler.getSoundEvent(this.positionedSoundLocation);
        if (soundeventaccessor == null) {
            this.sound = SoundHandler.EMPTY_SOUND;
        } else {
            this.sound = soundeventaccessor.getSound();
        }

        return soundeventaccessor;
    }

    public Sound getSound()
    {
        return this.sound;
    }

    public SoundCategory getSource()
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

    public ISound.AttenuationType getAttenuation()
    {
        return this.attenuationType;
    }

    public void setVolume(float vol) { this.volume = vol;}
}
