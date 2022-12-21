package mods.thecomputerizer.musictriggers;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.rits.cloning.Cloner;
import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.client.audio.Channel;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.common.EventsCommon;
import mods.thecomputerizer.musictriggers.config.ConfigChannels;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import mods.thecomputerizer.musictriggers.util.CustomTick;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.io.FileExistsException;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mod(modid = Constants.MODID, name = Constants.NAME, version = Constants.VERSION, dependencies = Constants.DEPENDENCIES)
public class MusicTriggers {
    private static LogUtil.ModLogger MOD_LOG;
    public static List<String> savedMessages = new ArrayList<>();
    private static Random random;

    public MusicTriggers() throws FileExistsException {
        MOD_LOG = LogUtil.create(Constants.MODID);
        random = new Random();
        if (!Constants.CONFIG_DIR.exists())
            if(!Constants.CONFIG_DIR.mkdir())
                throw new FileExistsException("Unable to create file directory at "+Constants.CONFIG_DIR.getPath()+
                        "! Music Triggers is unable to load any further.");
        if(FMLCommonHandler.instance().getSide()==Side.CLIENT) {
            ChannelManager.createJukeboxChannel();
            ConfigChannels.initialize(new File(Constants.CONFIG_DIR, "channels.toml"));
            for (ConfigChannels.ChannelInfo info : ConfigChannels.CHANNELS)
                ChannelManager.createChannel(info.getChannelName(), info.getSoundCategory(), info.getMain(),
                        info.getTransitions(), info.getCommands(), info.getToggles(), info.getRedirect(), info.getSongsFolder(),
                        info.getPausedByJukeBox(), info.getOverridesNormalMusic(), info);
            ChannelManager.parseConfigFiles();
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if(!ConfigRegistry.CLIENT_SIDE_ONLY) RegistryHandler.init();
        if(event.getSide()==Side.CLIENT) {
            MinecraftForge.EVENT_BUS.register(ChannelManager.class);
            MinecraftForge.EVENT_BUS.register(EventsClient.class);
            CustomTick.addCustomTickEvent(20);
        }
        MinecraftForge.EVENT_BUS.register(EventsCommon.class);
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
        if(e.getSide()==Side.CLIENT) {
            ClientRegistry.registerKeyBinding(Channel.GUI);
        }
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        if(e.getSide()==Side.CLIENT) ChannelManager.readResourceLocations();
    }

    public static ResourceLocation getIcon(String type, String name) {
        return new ResourceLocation(Constants.MODID,"textures/"+type+"/"+name+".png");
    }

    public static String[] stringBreaker(String s, String regex) {
        return s.split(regex);
    }

    public static int randomInt(int max) {
        return random.nextInt(max);
    }

    /*
     * Uses a fallback in case someone decides to add something that is not a number to a number parameter
     */
    public static int randomInt(String parameter, String toConvert, int fallback) {
        String[] broken = stringBreaker(toConvert,":");
        if(broken.length==1) return parse(parameter, broken[0], fallback);
        int min = parse(parameter, broken[0], fallback);
        int max = parse(parameter, broken[1], fallback);
        if(min==max) return min;
        else if(min>max) {
            int temp = max;
            max = min;
            min = temp;
        }
        if(max-min<=0) return min;
        return min+random.nextInt(max-min);
    }

    private static int parse(String parameter, String element, int fallback) {
        try {
            return Integer.parseInt(element);
        } catch (NumberFormatException ignored) {
            logExternally(Level.WARN,"Invalid element {} for parameter {}! Using fallback {}",element,parameter,
                    fallback);
            return fallback;
        }
    }

    public static float randomFloat(String parameter, String toConvert, float fallback) {
        String[] broken = stringBreaker(toConvert,":");
        if(broken.length==1) return parse(parameter, broken[0], fallback);
        float min = parse(parameter, broken[0], fallback);
        float max = parse(parameter, broken[1], fallback);
        if(min==max) return min;
        else if(min>max) {
            float temp = max;
            max = min;
            min = temp;
        }
        if(max-min<=0) return min;
        return min+random.nextFloat(max-min);
    }

    private static float parse(String parameter, String element, float fallback) {
        try {
            return Float.parseFloat(element);
        } catch (NumberFormatException ignored) {
            logExternally(Level.WARN,"Invalid element {} for parameter {}! Using fallback {}",element,parameter,
                    fallback);
            return fallback;
        }
    }

    public static void logExternally(Level level, String message, Object ... parameters) {
        MOD_LOG.log(level, message, parameters);
        if(level!=Level.DEBUG) savedMessages.add(colorizeLogLevel(level)+ LogUtil.injectParameters(message, parameters));
    }

    private static String colorizeLogLevel(Level level) {
        String logLevel = "["+level.toString()+"] ";
        if(level==Level.DEBUG) return ChatFormatting.GRAY+logLevel;
        else if(level==Level.INFO) return logLevel;
        else if(level==Level.WARN) return ChatFormatting.GOLD+logLevel;
        else if(level==Level.ERROR) return ChatFormatting.RED+logLevel;
        else return ChatFormatting.DARK_RED+logLevel;
    }

    public static <T> T clone(final T o) {
        return Cloner.standard().deepClone(o);
    }
}
