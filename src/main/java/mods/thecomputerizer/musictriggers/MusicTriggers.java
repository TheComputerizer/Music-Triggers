package mods.thecomputerizer.musictriggers;

import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.client.gui.Mappings;
import mods.thecomputerizer.musictriggers.common.EventsCommon;
import mods.thecomputerizer.musictriggers.common.TriggerCommand;
import mods.thecomputerizer.musictriggers.config.ConfigChannels;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Random;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class MusicTriggers implements ModInitializer {
    public static final String MODID = "musictriggers";

    public static final Logger logger = LogManager.getLogger();

    public static File configDir;
    private static Random random;

    @Override
    public void onInitialize() {
        random = new Random();
        configDir = new File(".", "config/MusicTriggers");
        if (!configDir.exists()) configDir.mkdir();
        ChannelManager.createJukeboxChannel();
        for(ConfigChannels.ChannelInfo info : ConfigChannels.parse(new File(configDir,"channels.toml")))
            ChannelManager.createChannel(info.getChannelName(),info.getMain(),info.getTransitions(),info.getCommands(),info.getToggles(),info.getRedirect(),FabricLoaderImpl.INSTANCE.getEnvironmentType()== EnvType.CLIENT,info.getPausedByJukeBox(),info.getOverridesNormalMusic());
        ChannelManager.parseConfigFiles();
        Mappings.init();
        if (ConfigRegistry.registerDiscs) RegistryHandler.init();
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> TriggerCommand.register(dispatcher));
        if(!ConfigRegistry.clientSideOnly) PacketHandler.registerReceivers();
        setUpCommonEvents();
    }

    private static void setUpCommonEvents() {
        ServerTickEvents.END_SERVER_TICK.register( server -> EventsCommon.onServerTick());
    }

    public static String[] stringBreaker(String s, String regex) {
        return s.split(regex);
    }

    public static int randomInt(String toConvert) {
        String[] broken = stringBreaker(toConvert,":");
        if(broken.length==1) return Integer.parseInt(broken[0]);
        int min = Integer.parseInt(broken[0]);
        int max = Integer.parseInt(broken[1]);
        if(min==max) return min;
        else if(min>max) {
            int temp = max;
            max = min;
            min = temp;
        }
        if(max-min<=0) return min;
        return min+random.nextInt(max-min);
    }
}
