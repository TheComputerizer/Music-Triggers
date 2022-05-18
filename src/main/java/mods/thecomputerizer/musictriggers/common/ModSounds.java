package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ModSounds {

    public static HashMap<String, SoundInstance> playableSounds = new HashMap<>();

    public static void init(){
        List<SoundEvent> sounds = SoundHandler.allSoundEvents;
        for(SoundEvent s: sounds) {
            String songName = Objects.requireNonNull(s.getId()).toString().replaceAll("musictriggers:","");
            Identifier soundID = new Identifier(MusicTriggersCommon.MODID + ":music." + songName);
            SoundEvent soundEvent = new SoundEvent(soundID);
            if(!ConfigRegistry.clientSideOnly) {
                Registry.register(Registry.SOUND_EVENT, soundID, soundEvent);
                MusicTriggersCommon.logger.info(songName+" is being initialized at resource location "+new Identifier(MusicTriggersCommon.MODID+":music." + songName));
            }
            if(FabricLoaderImpl.INSTANCE.getEnvironmentType()== EnvType.CLIENT) {
                SoundInstance i = PositionedSoundInstance.music(soundEvent);
                playableSounds.put("music." + songName, i);
            }
        }
    }
    public static void reload() {
        playableSounds = new HashMap<>();
        List<SoundEvent> sounds = SoundHandler.allSoundEvents;
        for(SoundEvent s: sounds) {
            String songName = Objects.requireNonNull(s.getId()).toString().replaceAll("musictriggers:", "");
            SoundInstance i = PositionedSoundInstance.music(new SoundEvent(new Identifier(MusicTriggersCommon.MODID + ":music." + songName)));
            playableSounds.put("music." + songName, i);
        }
    }
}
