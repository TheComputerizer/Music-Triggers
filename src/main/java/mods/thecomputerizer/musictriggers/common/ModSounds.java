package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ModSounds {

    public static final ModSounds INSTANCE = new ModSounds();
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MusicTriggers.MODID);

    public static HashMap<String,ISound> playableSounds = new HashMap<>();

    public void init(){
        List<SoundEvent> sounds = SoundHandler.allSoundEvents;
        for(SoundEvent s: sounds) {
            String songName = Objects.requireNonNull(s.getRegistryName()).toString().replaceAll("musictriggers:","");
            SOUNDS.register(songName, () -> new SoundEvent(new ResourceLocation(MusicTriggers.MODID+":music." + songName)));
            MusicTriggers.logger.info(songName+" is being initialized at resource location "+new ResourceLocation(MusicTriggers.MODID+":music." + songName));
            ISound i = SimpleSound.forMusic(new SoundEvent(new ResourceLocation(MusicTriggers.MODID+":music." + songName)));
            playableSounds.put("music."+songName,i);
        }
    }
}
