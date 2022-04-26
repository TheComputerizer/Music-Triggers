package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ModSounds {

    public static final ModSounds INSTANCE = new ModSounds();
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MusicTriggers.MODID);

    public static HashMap<String,SoundInstance> playableSounds = new HashMap<>();

    public void init(){
        List<SoundEvent> sounds = SoundHandler.allSoundEvents;
        for(SoundEvent s: sounds) {
            String songName = Objects.requireNonNull(s.getRegistryName()).toString().replaceAll("musictriggers:","");
            if(ConfigRegistry.registerDiscs) {
                SOUNDS.register(songName, () -> new SoundEvent(new ResourceLocation(MusicTriggers.MODID + ":music." + songName)));
                MusicTriggers.logger.info(songName+" is being initialized at resource location "+new ResourceLocation(MusicTriggers.MODID+":music." + songName));
            }
            if(FMLEnvironment.dist == Dist.CLIENT) {
                SoundInstance i = SimpleSoundInstance.forMusic(new SoundEvent(new ResourceLocation(MusicTriggers.MODID + ":music." + songName)));
                playableSounds.put("music." + songName, i);
            }
        }
    }
    public static void reload() {
        playableSounds = new HashMap<>();
        List<SoundEvent> sounds = SoundHandler.allSoundEvents;
        for(SoundEvent s: sounds) {
            String songName = Objects.requireNonNull(s.getRegistryName()).toString().replaceAll("musictriggers:", "");
            if(FMLEnvironment.dist == Dist.CLIENT) {
                SoundInstance i = SimpleSoundInstance.forMusic(new SoundEvent(new ResourceLocation(MusicTriggers.MODID + ":music." + songName)));
                playableSounds.put("music." + songName, i);
            }
        }
    }
}
