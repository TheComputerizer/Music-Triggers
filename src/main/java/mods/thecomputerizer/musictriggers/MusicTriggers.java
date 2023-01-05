package mods.thecomputerizer.musictriggers;

import com.rits.cloning.Cloner;
import mods.thecomputerizer.musictriggers.common.EventsCommon;
import mods.thecomputerizer.musictriggers.common.TriggerCommand;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicTriggers implements ModInitializer {

    private static LogUtil.ModLogger MOD_LOG;
    public static List<String> savedMessages = new ArrayList<>();
    private static Random random;

    @Override
    public void onInitialize() {
        MOD_LOG = LogUtil.create(Constants.MODID);
        random = new Random();
        if (!Constants.CONFIG_DIR.exists())
            if(!Constants.CONFIG_DIR.mkdir())
                throw new RuntimeException("Unable to create file directory at "+Constants.CONFIG_DIR.getPath()+
                        "! Music Triggers is unable to load any further.");
        if (ConfigRegistry.REGISTER_DISCS) RegistryHandler.init();
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> TriggerCommand.register(dispatcher));
        if(!ConfigRegistry.CLIENT_SIDE_ONLY) PacketHandler.registerReceivers();
        setUpCommonEvents();
    }

    private static void setUpCommonEvents() {
        ServerTickEvents.END_SERVER_TICK.register( server -> EventsCommon.onServerTick());
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
        float h = max-min;
        if(h<=0) return min;
        return min+(random.nextFloat()*h);
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
