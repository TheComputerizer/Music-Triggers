package mods.thecomputerizer.musictriggers.server.channels;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.util.file.TomlUtil;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ServerChannelManager {

    private final static Map<String, ServerChannel> SERVER_CHANNELS = new HashMap<>();
    private final static List<String> DISABLED_GUI_BUTTONS = new ArrayList<>();
    public static void initialize(File channelsFile) throws IOException {
        SERVER_CHANNELS.clear();
        if(channelsFile.exists()) {
            Holder channels = TomlUtil.readFully(channelsFile);
            for(String type : new String[]{"gui","reload","log","playback","debug","registration"})
                checkDisabledGuiButton(channels,type);
            for (Table channel : channels.getTables().values()) {
                if (verifyChannelName(channel.getName())) {
                    ServerChannel serverChannel = new ServerChannel(channel);
                    if(serverChannel.isValid()) SERVER_CHANNELS.put(channel.getName(),serverChannel);
                } else
                    MusicTriggers.logExternally(Level.ERROR, "Channel {} failed to register! See the above errors for" +
                            "more information.", channel.getName());

            }
        }
    }

    private static void checkDisabledGuiButton(Holder holder, String type) {
        String valName = !type.matches("gui") ? "disable_"+type+"_button" : "disable_"+type;
        if(holder.getValOrDefault(valName,false)) DISABLED_GUI_BUTTONS.add(type);
    }

    private static boolean verifyChannelName(String channelName) {
        if(channelName.matches("preview") || channelName.matches("jukebox")) {
            MusicTriggers.logExternally(Level.ERROR, "Channel name cannot be set to \"jukebox\" or \"preview\"" +
                    "as those are used for internal functions!");
            return false;
        }
        else if(Objects.nonNull(SERVER_CHANNELS.get(channelName))) {
            MusicTriggers.logExternally(Level.ERROR, "Channel with name " + channelName + " already exists" +
                    "! Different channels must have unique names!");
            return false;
        }
        return true;
    }

    public static boolean hasConfig() {
        return !SERVER_CHANNELS.isEmpty();
    }

    public static List<ServerChannel> getChannels() {
        return new ArrayList<>(SERVER_CHANNELS.values());
    }

    public static List<String> getDisabledGuiButtons() {
        return DISABLED_GUI_BUTTONS;
    }
}
