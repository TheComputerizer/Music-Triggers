package mods.thecomputerizer.musictriggers;

import com.rits.cloning.Cloner;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import mods.thecomputerizer.musictriggers.network.NetworkHandler;
import mods.thecomputerizer.musictriggers.registry.BlockRegistry;
import mods.thecomputerizer.musictriggers.registry.ItemRegistry;
import mods.thecomputerizer.musictriggers.registry.TileRegistry;
import mods.thecomputerizer.musictriggers.server.ServerEvents;
import mods.thecomputerizer.musictriggers.server.TriggerCommand;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.*;

public class MusicTriggers implements ModInitializer {
    private static LogUtil.ModLogger MOD_LOG;
    private static final LinkedHashMap<Integer, Tuple<String, Integer>> ORDERED_LOG_MESSAGES = new LinkedHashMap<>();
    private static int MESSAGE_COUNTER;
    private static Random RANDOM;

    @Override
    public void onInitialize() {
        MOD_LOG = LogUtil.create(Constants.MODID);
        MESSAGE_COUNTER = 0;
        RANDOM = new Random();
        if(!Constants.CONFIG_DIR.exists())
            if(!Constants.CONFIG_DIR.mkdirs())
                throw new RuntimeException("Unable to create file directory at "+Constants.CONFIG_DIR.getPath()+
                        "! Music Triggers is unable to load any further.");
        ConfigRegistry.initialize(new File(Constants.CONFIG_DIR,"registration.toml"),false);
        if(ConfigRegistry.REGISTER_DISCS) {
            Constants.MAIN_LOG.info("Loading Music Triggers discs and blocks");
            BlockRegistry.register();
            ItemRegistry.register();
            TileRegistry.register();
        }
        if(!ConfigRegistry.CLIENT_SIDE_ONLY) NetworkHandler.registerReceivers(false);
        setUpCommonEvents();
    }

    private void setUpCommonEvents() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> TriggerCommand.register(dispatcher));
        ServerTickEvents.END_SERVER_TICK.register(server -> ServerEvents.onServerTick());
        AttackEntityCallback.EVENT.register(((player, world, hand, entity, hitResult) -> ServerEvents.livingAttack(player,entity)));
    }

    public static ResourceLocation getIcon(String type, String name) {
        return Objects.nonNull(type) ? new ResourceLocation(Constants.MODID,"textures/"+type+"/"+name+".png") :
                new ResourceLocation(Constants.MODID,"textures/"+name);
    }

    public static String[] stringBreaker(String s, String regex) {
        return s.split(regex);
    }

    public static int randomInt(int max) {
        return RANDOM.nextInt(max);
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
        return min+RANDOM.nextInt(max-min);
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
        return min+(RANDOM.nextFloat()*h);
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
        ORDERED_LOG_MESSAGES.put(MESSAGE_COUNTER,new Tuple<>("["+String.format("%-5s",level.toString())+"] "+
                LogUtil.injectParameters(message, parameters),colorizeLogLevel(level)));
        MESSAGE_COUNTER++;
    }

    private static int colorizeLogLevel(Level level) {
        if(level==Level.DEBUG) return GuiUtil.makeRGBAInt(200,200,200,255);
        else if(level==Level.INFO) return GuiUtil.WHITE;
        else if(level==Level.WARN) return GuiUtil.makeRGBAInt(255,215,0,255);
        else if(level==Level.ERROR) return GuiUtil.makeRGBAInt(255,0,0,255);
        return GuiUtil.makeRGBAInt(100,0,0,255);
    }

    public static Set<Map.Entry<Integer,Tuple<String,Integer>>> getLogEntries() {
        return ORDERED_LOG_MESSAGES.entrySet();
    }

    public static void clearLog() {
        ORDERED_LOG_MESSAGES.clear();
        MESSAGE_COUNTER = 0;
    }

    public static <T> T clone(final T o) {
        return Cloner.standard().deepClone(o);
    }

    public static File configFile(String path, String extension) {
        return FileUtil.generateNestedFile(new File(Constants.CONFIG_DIR,path+"."+extension),false);
    }
}
