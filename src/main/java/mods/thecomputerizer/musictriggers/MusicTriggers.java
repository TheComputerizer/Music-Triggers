package mods.thecomputerizer.musictriggers;

import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.client.audio.Channel;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.client.gui.Mappings;
import mods.thecomputerizer.musictriggers.common.EventsCommon;
import mods.thecomputerizer.musictriggers.config.ConfigChannels;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import mods.thecomputerizer.musictriggers.util.CustomTick;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Random;

@SuppressWarnings("ResultOfMethodCallIgnored")
@Mod(MusicTriggers.MODID)
public class MusicTriggers {
    public static final String MODID = "musictriggers";
    public static final Logger logger = LogManager.getLogger();
    public static File configDir;
    private static Random random;

    public MusicTriggers() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonsetup);
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        random = new Random();
        configDir = new File(".", "config/MusicTriggers");
        if (!configDir.exists()) configDir.mkdir();
        for(ConfigChannels.ChannelInfo info : ConfigChannels.parse(new File(configDir,"channels.toml")))
            ChannelManager.createChannel(info.getChannelName(),info.getMain(),info.getTransitions(),info.getCommands(),info.getToggles(),info.getRedirect(),FMLEnvironment.dist == Dist.CLIENT,info.getPausedByJukeBox(),info.getOverridesNormalMusic());
        ChannelManager.parseConfigFiles();
        Mappings.init();
        RegistryHandler.init(eventBus);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(ChannelManager.class);
            MinecraftForge.EVENT_BUS.register(EventsClient.class);
        }
        MinecraftForge.EVENT_BUS.register(EventsCommon.class);
    }

    private void clientSetup(final FMLClientSetupEvent ev) {
        ClientRegistry.registerKeyBinding(Channel.GUI);
        CustomTick.setUp();
    }

    public void commonsetup(FMLCommonSetupEvent ev) {
        if(!ConfigRegistry.clientSideOnly) PacketHandler.register();
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
