package mods.thecomputerizer.musictriggers.api.data.channel;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.toggle.ToggleAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.io.FileHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.TomlHelper;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ChannelHelper {

    @Getter private static final List<ChannelAPI> channels = new ArrayList<>();
    @Getter private static final List<ToggleAPI> toggles = new ArrayList<>();
    private static Holder togglesHolder;

    public static void initChannels(String channelsFile) {
        Holder channels = openToml(channelsFile,null);
        if(Objects.isNull(channels)) return;
        for(Table info : channels.getUniqueTables()) {
        }
        togglesHolder = openToml(channels.getValOrDefault("toggles","config/MusicTriggers/toggles"),null);
    }

    /**
     * Assumes the file extension is not present
     */
    public static @Nullable Holder openToml(String path, @Nullable ChannelAPI channel) {
        path+=".toml";
        try {
            return TomlHelper.readFully(path);
        } catch(IOException ex) {
            String msg = "Unable to read toml file at `{}`!";
            if(Objects.nonNull(channel)) channel.logError(msg,ex);
            else MTRef.logError(msg,ex);
            return null;
        }
    }

    /**
     * Assumes the file extension is not present
     */
    public static List<String> openTxt(String path, @Nullable ChannelAPI channel) {
        path+=".txt";
        File file = FileHelper.get(path,false);
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().collect(Collectors.toList());
        } catch(IOException ex) {
            String msg = "Unable to read toml file at `{}`!";
            if(Objects.nonNull(channel)) channel.logError(msg,ex);
            else MTRef.logError(msg,ex);
            return Collections.emptyList();
        }
    }

    public static void parseChannelData() {
        for(ChannelAPI channel : channels) channel.parseData();
        readToggles();
    }

    private static void readToggles() {

    }
}