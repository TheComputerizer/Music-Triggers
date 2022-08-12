package mods.thecomputerizer.musictriggers.common;

import net.minecraft.client.sound.SoundInstance;

import java.util.HashMap;

public class ModSounds {

    public static HashMap<String, SoundInstance> playableSounds = new HashMap<>();

    public static void init(){
        /*
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

         */
    }
    public static void reload() {
        playableSounds = new HashMap<>();
        /*
        List<SoundEvent> sounds = SoundHandler.allSoundEvents;
        for(SoundEvent s: sounds) {
            String songName = Objects.requireNonNull(s.getId()).toString().replaceAll("musictriggers:", "");
            SoundInstance i = PositionedSoundInstance.music(new SoundEvent(new Identifier(MusicTriggersCommon.MODID + ":music." + songName)));
            playableSounds.put("music." + songName, i);
        }
         */
    }
}
