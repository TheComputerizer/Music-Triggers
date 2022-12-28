package mods.thecomputerizer.musictriggers;

import com.rits.cloning.Cloner;
import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.client.audio.Channel;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.common.EventsCommon;
import mods.thecomputerizer.musictriggers.common.MusicTriggersItems;
import mods.thecomputerizer.musictriggers.common.objects.CustomRecord;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.config.ConfigChannels;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import mods.thecomputerizer.musictriggers.util.CustomTick;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mod(Constants.MODID)
public class MusicTriggers {
    private static LogUtil.ModLogger MOD_LOG;
    public static List<String> savedMessages = new ArrayList<>();
    private static Random random;

    public MusicTriggers() throws FileExistsException {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        MOD_LOG = LogUtil.create(Constants.MODID);
        random = new Random();
        if (!Constants.CONFIG_DIR.exists())
            if(!Constants.CONFIG_DIR.mkdir())
                throw new FileExistsException("Unable to create file directory at "+Constants.CONFIG_DIR.getPath()+
                        "! Music Triggers is unable to load any further.");
        if(FMLEnvironment.dist == Dist.CLIENT) {
            ChannelManager.createJukeboxChannel();
            ConfigChannels.initialize(new File(Constants.CONFIG_DIR, "channels.toml"));
            for (ConfigChannels.ChannelInfo info : ConfigChannels.CHANNELS)
                ChannelManager.createChannel(info);
            ChannelManager.parseConfigFiles();
        }
        if (ConfigRegistry.REGISTER_DISCS) RegistryHandler.init(eventBus);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(ChannelManager.class);
            MinecraftForge.EVENT_BUS.register(EventsClient.class);
        }
        MinecraftForge.EVENT_BUS.register(EventsCommon.class);
    }

    private void clientSetup(final FMLClientSetupEvent ev) {
        ClientRegistry.registerKeyBinding(Channel.GUI);
        CustomTick.addCustomTickEvent(20);
        //music disc texture overrides
        ev.enqueueWork(() -> ItemModelsProperties.register(MusicTriggersItems.MUSIC_TRIGGERS_RECORD.get(), new ResourceLocation(Constants.MODID, "trigger"),
                (stack, worldIn, entityIn) -> {
                    if (stack.getOrCreateTag().contains("triggerID"))
                        return MusicTriggersRecord.mapTriggerToFloat(stack.getOrCreateTag().getString("triggerID"));
                    return 0f;
                }));
        ev.enqueueWork(() -> ItemModelsProperties.register(MusicTriggersItems.CUSTOM_RECORD.get(),
                new ResourceLocation(Constants.MODID, "custom_record"),
                (stack, worldIn, entityIn) -> {
                    if (stack.getOrCreateTag().contains("triggerID"))
                        return CustomRecord.mapTriggerToFloat(stack.getOrCreateTag().getString("triggerID"));
                    return 0f;
                }));
    }

    public void commonSetup(FMLCommonSetupEvent ev) {
        if (ConfigRegistry.CLIENT_SIDE_ONLY)
            ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        else PacketHandler.register();
    }

    public void loadComplete(FMLLoadCompleteEvent ev) {
        if(FMLEnvironment.dist == Dist.CLIENT)
            ChannelManager.readResourceLocations();
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
        if(level==Level.DEBUG) return TextFormatting.GRAY+logLevel;
        else if(level==Level.INFO) return logLevel;
        else if(level==Level.WARN) return TextFormatting.GOLD+logLevel;
        else if(level==Level.ERROR) return TextFormatting.RED+logLevel;
        else return TextFormatting.DARK_RED+logLevel;
    }

    public static <T> T clone(final T o) {
        return Cloner.standard().deepClone(o);
    }
}
