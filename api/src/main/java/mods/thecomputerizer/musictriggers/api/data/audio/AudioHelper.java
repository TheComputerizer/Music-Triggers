package mods.thecomputerizer.musictriggers.api.data.audio;

import mods.thecomputerizer.musictriggers.api.client.audio.AudioContainer;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;

public class AudioHelper {

    public static void parseAudio(ChannelAPI channel, Collection<AudioRef> audio, @Nullable Table table) {
        if(Objects.isNull(table)) return;
        for(Table songsTable : table.getChildren().values()) {
            String name = songsTable.getName();
            AudioRef ref = channel.isClientChannel() ? new AudioContainer(channel,name) : new AudioRef(channel,name);
            if(ref.parse(table)) audio.add(ref);
        }
    }
}