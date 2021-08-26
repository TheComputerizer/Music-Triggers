package mods.thecomputerizer.musictriggers.common;

import com.google.common.collect.Lists;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = MusicTriggers.MODID)
public class ModSounds {

    public static final ModSounds INSTANCE = new ModSounds();
    private List<SoundEvent> sounds;

    public void init(){
        sounds = SoundHandler.allSoundEvents;
    }

    public List<SoundEvent> getSounds(){
        return sounds;
    }
}
