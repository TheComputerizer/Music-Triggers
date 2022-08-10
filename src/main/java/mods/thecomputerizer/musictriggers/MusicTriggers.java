package mods.thecomputerizer.musictriggers;

import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.client.audio.Channel;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.client.gui.Mappings;
import mods.thecomputerizer.musictriggers.common.EventsCommon;
import mods.thecomputerizer.musictriggers.config.ConfigChannels;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import mods.thecomputerizer.musictriggers.util.CustomTick;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Random;

@Mod(modid = MusicTriggers.MODID, name = MusicTriggers.NAME, version = MusicTriggers.VERSION, dependencies = "required-after:mixinbooter")
public class MusicTriggers {
    public static final String MODID = "musictriggers";
    public static final String NAME = "Music Triggers";
    public static final String VERSION = "1.12.2-6.0-EX";
    public static Logger logger;
    public static File configDir;

    private static Random random;

    public MusicTriggers() {
        logger = LogManager.getLogger(MODID);
        random = new Random();
        configDir = new File(".", "config/MusicTriggers");
        if (!configDir.exists()) configDir.mkdir();
        for(ConfigChannels.ChannelInfo info : ConfigChannels.parse(new File(configDir,"channels.toml")))
            ChannelManager.createChannel(info.getChannelName(),info.getMain(),info.getTransitions(),info.getCommands(),info.getToggles(),info.getRedirect(),Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER,info.getPausedByJukeBox(),info.getOverridesNormalMusic());
        ChannelManager.parseConfigFiles();
        Mappings.init();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if(!ConfigRegistry.clientSideOnly) RegistryHandler.init();
        if(event.getSide()==Side.CLIENT) {
            MinecraftForge.EVENT_BUS.register(ChannelManager.class);
            MinecraftForge.EVENT_BUS.register(EventsClient.class);
        }
        MinecraftForge.EVENT_BUS.register(EventsCommon.class);
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
        if(e.getSide()==Side.CLIENT) {
            ClientRegistry.registerKeyBinding(Channel.GUI);
            CustomTick.setUp();
        }
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
