package mods.thecomputerizer.musictriggers.api.data.audio;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.client.audio.AudioContainer;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelEventHandler;
import mods.thecomputerizer.musictriggers.api.data.parameter.UniversalParameters;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class AudioHelper {
    
    public static AudioRef decodeAudio(ChannelAPI channel, ByteBuf buf) {
        String name = NetworkHelper.readString(buf);
        if("pool".equals(name)) {
            Set<AudioRef> audio = NetworkHelper.readSet(buf,() -> decodeAudio(channel,buf));
            for(Collection<ChannelEventHandler> handlers : channel.getData().getTriggerEventMap().values()) {
                for(ChannelEventHandler handler : handlers) {
                    if(handler instanceof AudioPool) {
                        AudioPool pool = (AudioPool)handler;
                        if(pool.equivalent(channel,audio)) return pool;
                    }
                }
            }
            return null;
        }
        return decodeAudioElement(channel,buf,name);
    }
    
    /**
     * It's assumed by this point that we are not decoding an audio pool
     */
    public static AudioRef decodeAudioElement(ChannelAPI channel, ByteBuf buf, String name) {
        List<TriggerAPI> triggers = NetworkHelper.readList(buf,() -> TriggerHelper.decodeTrigger(channel,buf));
        return findAudio(channel,name,triggers);
    }
    
    public static AudioRef findAudio(ChannelAPI channel, String name, Collection<TriggerAPI> triggers) {
        for(AudioRef audio : channel.getData().getAudio())
            if(audio.getName().equals(name) && TriggerHelper.matchesAll(audio.getTriggers(),triggers))
                return audio;
        return null;
    }

    public static void parseAudio(ChannelAPI channel, Collection<AudioRef> audio, @Nullable Toml table) {
        if(Objects.isNull(table)) return;
        UniversalParameters universal = channel.getData().getUniversals(AudioRef.class);
        for(Toml songsTable : table.getAllTables()) {
            String name = songsTable.getName();
            if(name.equals("universal")) {
                if(Objects.isNull(universal) || !universal.parse(songsTable))
                    channel.logError("Failed to parse universal songs");
                else channel.logInfo("Intialized universal songs data");
            }
            else {
                AudioRef ref = channel.isClientChannel() ? new AudioContainer(channel, name) : new AudioRef(channel, name);
                if (ref.parse(songsTable)) audio.add(ref);
            }
        }
    }
}