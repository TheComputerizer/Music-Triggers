package mods.thecomputerizer.musictriggers.api.data.audio;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class AudioHelper {

    public static void parseAudio(ChannelAPI channel, List<AudioRef> audio, @Nullable Table table) {
        if(Objects.isNull(table)) return;
        for(Table songsTable : table.getChildren().values()) {

        }
    }
}