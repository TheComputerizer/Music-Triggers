package mods.thecomputerizer.musictriggers.api.data.audio;

import mods.thecomputerizer.musictriggers.api.client.audio.AudioContainer;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.UniversalParameters;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;

public class AudioHelper {

    public static void parseAudio(ChannelAPI channel, Collection<AudioRef> audio, @Nullable Table table) {
        if(Objects.isNull(table)) return;
        for(Table songsTable : table.getChildren().values()) {
            String name = songsTable.getName();
            if(name.equals("universal")) {
                UniversalParameters universal = channel.getData().getUniversals(AudioRef.class);
                if(Objects.isNull(universal) || !universal.parseParameters(songsTable))
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