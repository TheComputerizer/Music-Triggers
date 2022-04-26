package mods.thecomputerizer.musictriggers.util.audio;

import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("ALL")
public class SetVolumeSound implements SoundInstance {

    protected Sound sound;
    @Nullable
    private WeightedSoundSet soundEvent;
    protected SoundCategory category;
    protected Identifier positionedSoundLocation;
    protected float volume;
    protected float pitch;
    protected float xPosF;
    protected float yPosF;
    protected float zPosF;
    protected boolean repeat;
    protected int repeatDelay;
    protected SoundInstance.AttenuationType attenuationType;

    public SetVolumeSound(Identifier soundId, SoundCategory categoryIn, float volume, float pitch, boolean repeat, int repeatDelay, SoundInstance.AttenuationType soundAttenuation, float xPosF, float yPosF, float zPosF) {
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

    public Identifier getId()
    {
        return this.positionedSoundLocation;
    }

    public WeightedSoundSet getSoundSet(SoundManager handler) {
        WeightedSoundSet soundeventaccessor = handler.get(this.positionedSoundLocation);
        if (soundeventaccessor == null) this.sound = SoundManager.MISSING_SOUND;
        else this.sound = soundeventaccessor.getSound();
        return soundeventaccessor;
    }

    public Sound getSound() {
        return this.sound;
    }

    public SoundCategory getCategory() {
        return this.category;
    }

    public boolean isRepeatable() {
        return this.repeat;
    }

    public boolean isRelative() {
        return false;
    }

    public int getRepeatDelay() {
        return this.repeatDelay;
    }

    public float getVolume() {
        return this.volume * this.sound.getVolume();
    }

    public float getPitch() {
        return this.pitch * this.sound.getPitch();
    }

    public double getX() {
        return this.xPosF;
    }

    public double getY() {
        return this.yPosF;
    }

    public double getZ() {
        return this.zPosF;
    }

    public SoundInstance.AttenuationType getAttenuationType() {
        return this.attenuationType;
    }

    public void setVolume(float vol) {
        this.volume = vol;
    }
}
