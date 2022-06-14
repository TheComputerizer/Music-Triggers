package mods.thecomputerizer.musictriggers.common;

import net.minecraft.util.SoundEvent;

import java.util.List;

public class ModSounds {

    public static final ModSounds INSTANCE = new ModSounds();
    private List<SoundEvent> sounds;

    public void init(){
        //sounds = SoundHandler.allSoundEvents;
    }

    public List<SoundEvent> getSounds(){
        return sounds;
    }
}
