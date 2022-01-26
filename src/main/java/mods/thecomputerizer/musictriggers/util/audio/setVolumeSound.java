package mods.thecomputerizer.musictriggers.util.audio;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SuppressWarnings("ALL")
@SideOnly(Side.CLIENT)
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

    public ResourceLocation getSoundLocation()
    {
        return this.positionedSoundLocation;
    }

    public SoundEventAccessor createAccessor(SoundHandler handler)
    {
        this.soundEvent = handler.getAccessor(this.positionedSoundLocation);

        if (this.soundEvent == null)
        {
            this.sound = SoundHandler.MISSING_SOUND;
        }
        else
        {
            this.sound = this.soundEvent.cloneEntry();
        }

        return this.soundEvent;
    }

    public Sound getSound()
    {
        return this.sound;
    }

    public SoundCategory getCategory()
    {
        return this.category;
    }

    public boolean canRepeat()
    {
        return this.repeat;
    }

    public int getRepeatDelay()
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

    public float getXPosF()
    {
        return this.xPosF;
    }

    public float getYPosF()
    {
        return this.yPosF;
    }

    public float getZPosF()
    {
        return this.zPosF;
    }

    public ISound.AttenuationType getAttenuationType()
    {
        return this.attenuationType;
    }

    public void setVolume(float vol) { this.volume = vol;}
}
