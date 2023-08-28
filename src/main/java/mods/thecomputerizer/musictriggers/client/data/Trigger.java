package mods.thecomputerizer.musictriggers.client.data;

import CoroUtil.util.Vec3;
import de.ellpeck.nyx.capabilities.NyxWorld;
import de.ellpeck.nyx.lunarevents.BloodMoon;
import de.ellpeck.nyx.lunarevents.HarvestMoon;
import de.ellpeck.nyx.lunarevents.StarShower;
import io.netty.buffer.ByteBuf;
import lumien.bloodmoon.Bloodmoon;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.ClientEvents;
import mods.thecomputerizer.musictriggers.client.channels.Channel;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.logging.log4j.Level;
import org.orecruncher.dsurround.client.weather.Weather;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;
import weather2.api.WeatherDataHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@SuppressWarnings({"BooleanMethodIsAlwaysInverted"})
public class Trigger {

    private static final HashSet<String> ALL_TRIGGERS = new HashSet<>();
    private static final HashSet<String> ACCEPTED_TRIGGERS = new HashSet<>();
    private static final HashSet<String> SERVER_TRIGGERS = new HashSet<>();
    private static final HashMap<String, DefaultParameter> DEFAULT_PARAMETER_MAP = new HashMap<>();
    private static final HashMap<String, HashSet<String>> ACCEPTED_PARAMETERS = new HashMap<>();
    private static final HashMap<String, List<String>> REQUIRED_PARAMETERS = new HashMap<>();
    private static final HashMap<String, List<String>> CHOICE_REQUIRED_PARAMETERS = new HashMap<>();
    private static final HashMap<String, BiFunction<Trigger, EntityPlayerSP, Boolean>> TRIGGER_CONDITIONS = new HashMap<>();
    private static final Set<String> CACHED_EFFECTS = Collections.synchronizedSet(new HashSet<>());
    private static final List<String> NBT_MODES = Arrays.asList("KEY_PRESENT","VAL_PRESENT","GREATER","LESSER","EQUAL","INVERT");

    public static void loadData() {
        clearData();
        loadDefaultParameters();
        loadDefaultTriggers();
        addDefaultParameterOverrides();
        MinecraftForge.EVENT_BUS.post(new ReloadEvent());
    }

    private static void clearData() {
        ALL_TRIGGERS.clear();
        ACCEPTED_TRIGGERS.clear();
        SERVER_TRIGGERS.clear();
        DEFAULT_PARAMETER_MAP.clear();
        ACCEPTED_PARAMETERS.clear();
        REQUIRED_PARAMETERS.clear();
        CHOICE_REQUIRED_PARAMETERS.clear();
        TRIGGER_CONDITIONS.clear();
    }

    private static void loadDefaultParameters() {
        addParameter("priority",0);
        addParameter("identifier","not_set");
        addParameter("fade_in",0);
        addParameter("fade_out",0);
        addParameter("trigger_delay",0);
        addParameter("song_delay",0);
        addParameter("level",0);
        addParameter("persistence",0);
        addParameter("start_delay",0);
        addParameter("stop_delay",0);
        addParameter("time_bundle","ANY");
        addParameter("start_hour",0f);
        addParameter("end_hour",0f);
        addParameter("lowest_day_number",0);
        addParameter("highest_day_number",Integer.MAX_VALUE);
        addParameter("zone_min_x",Integer.MIN_VALUE);
        addParameter("zone_min_y",Integer.MIN_VALUE);
        addParameter("zone_min_z",Integer.MIN_VALUE);
        addParameter("zone_max_x",Integer.MAX_VALUE);
        addParameter("zone_max_y",Integer.MAX_VALUE);
        addParameter("zone_max_z",Integer.MAX_VALUE);
        addParameter("resource_name",Collections.singletonList("ANY"));
        addParameter("start_toggled",true);
        addParameter("not",false);
        addParameter("passive_persistence",false);
        addParameter("toggle_inactive_playable",false);
        addParameter("detection_range",16);
        addParameter("mob_targeting",true);
        addParameter("health",100f);
        addParameter("horde_targeting_percentage",50f);
        addParameter("horde_health_percentage",50f);
        addParameter("mob_nbt","ANY");
        addParameter("infernal",Collections.singletonList("ANY"));
        addParameter("champion",Collections.singletonList("ANY"));
        addParameter("victory_id","not_set");
        addParameter("victory_timeout",20);
        addParameter("victory_percentage",100f);
        addParameter("moon_phase",0);
        addParameter("light_type","ANY");
        addParameter("is_whitelist",true);
        addParameter("biome_category",Collections.singletonList("ANY"));
        addParameter("rain_type","ANY");
        addParameter("biome_temperature",Float.MIN_VALUE);
        addParameter("check_lower_temp",false);
        addParameter("biome_rainfall",Float.MIN_VALUE);
        addParameter("check_higher_rainfall",true);
        addParameter("check_for_sky",true);
        addParameter("check_above_level",false);
        addParameter("toggle_save_status",0);
        addParameter("max_tracks",0);
        addParameter("detection_y_ratio",0.5f);
        addParameter("items",Collections.singletonList("EMPTY"));
        addParameter("slots",Collections.singletonList("ANY"));
    }

    /**
     * Accepted parameter set
     */
    public static HashSet<String> makeParameterSet(boolean isHolder, String ... parameters) {
        return makeParameterSet(true,isHolder,parameters);
    }

    public static HashSet<String> makeParameterSet(boolean isAccepted, boolean isHolder, String ... parameters) {
        HashSet<String> acceptedParameters = isAccepted ?
                new HashSet<>(Arrays.asList("priority","fade_in","fade_out","trigger_delay","song_delay",
                        "start_toggled","not", "persistence","start_delay","stop_delay","passive_persistence",
                        "toggle_inactive_playable", "toggle_save_status", "max_tracks")) : new HashSet<>();
        if(isHolder) acceptedParameters.add("identifier");
        Collections.addAll(acceptedParameters, parameters);
        return acceptedParameters;
    }

    private static void loadDefaultTriggers() {
        addTrigger("loading",false,makeParameterSet(false),(trigger,player) -> false, true);
        addTrigger("menu",false,makeParameterSet(false),(trigger,player) -> false,true);
        addTrigger("generic",false,makeParameterSet(false),(trigger,player) -> false,true);
        addTrigger("difficulty",false,makeParameterSet(true,"level"),
                Arrays.asList("identifier","level"),new ArrayList<>(),(trigger,player) -> {
            Minecraft mc = Minecraft.getMinecraft();
            if(Objects.isNull(mc.world)) return false;
            return trigger.difficultyHelper(mc.world.getDifficulty(), mc.world.getWorldInfo().isHardcoreModeEnabled());
        },true);
        addTrigger("time",false,makeParameterSet(true,"time_bundle",
                        "start_hour","end_hour","moon_phase", "lowest_day_number","highest_day_number"),
                Collections.singletonList("identifier"),Arrays.asList("time_bundle","start_hour"),(trigger,player) -> {
            World world = player.getEntityWorld();
            double time = (double) world.getWorldTime() / 24000.0;
            if (time > 1) time = time - (long) time;
            boolean pass;
            String bundle = trigger.getParameterString("time_bundle");
            double min;
            double max;
            if (bundle.matches("day")) {
                min = 0d;
                max = 0.54166666666d;
            } else if (bundle.matches("night")) {
                min = 0.54166666666d;
                max = 1d;
            } else if (bundle.matches("sunset")) {
                min = 0.5d;
                max = 0.54166666666d;
            } else if (bundle.matches("sunrise")) {
                min = 0.95833333333d;
                max = 1d;
            } else {
                double doubleStart = trigger.getParameterFloat("start_hour");
                double doubleEnd = trigger.getParameterFloat("end_hour");
                if (doubleEnd == -1) {
                    if (doubleStart <= 21d) doubleEnd = doubleStart + 3d;
                    else doubleEnd = doubleStart - 21d;
                }
                min = doubleStart / 24d;
                max = doubleEnd / 24d;
            }
            if (min < max) pass = time >= min && time < max;
            else pass = time >= min || time < max;
            return pass && trigger.timeTriggerExtras(world.getTotalWorldTime(),world.getMoonPhase() + 1);
        },true);
        addTrigger("light",false,makeParameterSet(true,"level","light_type"),
                Arrays.asList("identifier","level"),new ArrayList<>(),(trigger,player) ->
                trigger.averageLight(trigger.roundedPos(player),trigger.getParameterString("light_type"), player.getEntityWorld())
                        <= trigger.getParameterInt("level"),true);
        addTrigger("height",false,makeParameterSet(true,"level","check_for_sky","check_above_level"),
                Arrays.asList("identifier","level"),new ArrayList<>(),(trigger,player) ->
                trigger.handleHeight((int) player.posY,player.getEntityWorld().canSeeSky(trigger.roundedPos(player))),true);
        addTrigger("elytra",false,makeParameterSet(false),
                (trigger,player) -> player.getTicksElytraFlying() > 0,true);
        addTrigger("fishing",false,makeParameterSet(false),
                (trigger,player) -> player.fishEntity != null && player.fishEntity.isOverWater(),true);
        addTrigger("raining",false,makeParameterSet(false),
                (trigger,player) -> player.getEntityWorld().isRaining(),true);
        addTrigger("snowing",false,makeParameterSet(false),(trigger,player) -> {
            BlockPos pos = trigger.roundedPos(player);
            return player.getEntityWorld().isRaining() &&
                    player.getEntityWorld().getBiomeProvider().getTemperatureAtHeight(
                            player.getEntityWorld().getBiome(pos).getTemperature(pos),
                            player.getEntityWorld().getPrecipitationHeight(pos).getY()) < 0.15f;
        },true);
        addTrigger("storming",false,makeParameterSet(false),
                (trigger,player) -> player.getEntityWorld().isThundering(),true);
        addTrigger("lowhp",false,makeParameterSet(false,"level"),
                (trigger,player) -> trigger.handleHP(player.getHealth(), player.getMaxHealth()),true);
        addTrigger("dead",false,makeParameterSet(false),
                (trigger,player) -> player.getHealth() <= 0f || player.isDead,true);
        addTrigger("spectator",false,makeParameterSet(false),
                (trigger,player) -> player.isSpectator(),true);
        addTrigger("creative",false,makeParameterSet(false),
                (trigger,player) -> player.isCreative(),true);
        addTrigger("riding",false,makeParameterSet(true,"resource_name"),
                Collections.singletonList("identifier"),new ArrayList<>(),Trigger::checkRiding,true);
        addTrigger("underwater",false,makeParameterSet(false),(trigger,player) ->
                player.getEntityWorld().getBlockState(trigger.roundedPos(player)).getMaterial() == Material.WATER &&
                        player.getEntityWorld().getBlockState(trigger.roundedPos(player).up()).getMaterial() == Material.WATER,true);
        addTrigger("pet",false,makeParameterSet(false,"detection_range","detection_y_ratio"),
                (trigger,player) -> {
            boolean pass = false;
            int range = trigger.getParameterInt("detection_range");
            float yRatio = trigger.getParameterFloat("detection_y_ratio");
            for (EntityLiving ent : player.getEntityWorld().getEntitiesWithinAABB(EntityLiving.class,
                    new AxisAlignedBB(player.posX - range, player.posY - ((float) range*yRatio), player.posZ - range,
                            player.posX + range, player.posY + ((float) range*yRatio), player.posZ + range))) {
                if (ent instanceof EntityTameable && ent.serializeNBT().getString("Owner").matches(player.getName()))
                    pass = true;
            }
            return pass;
        },true);
        addTrigger("drowning",false,makeParameterSet(false,"level"),
                (trigger,player) -> player.getAir() < trigger.getParameterInt("level"),true);
        addTrigger("pvp",true,makeParameterSet(true), Collections.singletonList("identifier"),
                new ArrayList<>(),(trigger,player) -> trigger.channel.getSyncStatus().isTriggerActive(trigger),true);
        addTrigger("home",true,makeParameterSet(false,"detection_range","detection_y_ratio"),
                (trigger,player) -> trigger.channel.getSyncStatus().isTriggerActive(trigger),true);
        addTrigger("dimension",false,makeParameterSet(true,"resource_name"),
                Arrays.asList("identifier","resource_name"),new ArrayList<>(),
                (trigger,player) -> trigger.checkDimensionList(player.dimension),true);
        addTrigger("biome",false,makeParameterSet(true,"resource_name","biome_category",
                        "rain_type","biome_temperature","check_lower_temp","biome_rainfall","check_higher_rainfall"),
                Collections.singletonList("identifier"),Arrays.asList("resource_name","biome_category","rain_type",
                        "biome_temperature","biome_rainfall"),(trigger, player) ->
                trigger.checkBiome(player.getEntityWorld().getBiome(trigger.roundedPos(player)),
                        trigger.getParameterStringList("biome_category"),trigger.getParameterString("rain_type"),
                        trigger.getParameterFloat("biome_temperature"),trigger.getParameterBool("check_lower_temp"),
                        trigger.getParameterFloat("biome_rainfall"),trigger.getParameterBool("check_higher_rainfall")),true);
        addTrigger("structure",true,makeParameterSet(true,"resource_name"),
                Arrays.asList("identifier","resource_name"),new ArrayList<>(),
                (trigger,player) -> trigger.channel.getSyncStatus().isTriggerActive(trigger),true);
        addTrigger("mob",true,makeParameterSet(true,"resource_name","level",
                        "detection_range","detection_y_ratio","mob_targeting","health","horde_targeting_percentage", "horde_health_percentage",
                        "mob_nbt","infernal","champion","victory_id","victory_percentage"),
                Arrays.asList("identifier","resource_name"),new ArrayList<>(),
                (trigger,player) -> trigger.channel.getSyncStatus().isTriggerActive(trigger),true);
        addTrigger("zones",false,makeParameterSet(true,"zone_min_x",
                        "zone_max_x","zone_min_y","zone_max_y","zone_min_z","zone_max_z"),
                Collections.singletonList("identifier"),Arrays.asList("start_delay","zone_min_x","zone_max_x","zone_min_y",
                        "zone_max_y","zone_min_z","zone_max_z"),(trigger, player) -> {
            BlockPos pos = player.getPosition();
            return trigger.zoneHelper(pos.getX(),pos.getY(),pos.getZ());
        },true);
        addTrigger("effect",false,makeParameterSet(true,"resource_name"),
                Arrays.asList("identifier","resource_name"),new ArrayList<>(),(trigger,player) -> {
                    synchronized (CACHED_EFFECTS) {
                        boolean pass = false;
                        for(String effect : CACHED_EFFECTS)
                            if (trigger.checkResourceMatch(effect,false))
                                pass = true;
                        return pass;
                    }
                },true);
        addTrigger("victory",true,makeParameterSet(true,"victory_timeout"),
                Arrays.asList("identifier","persistence"),new ArrayList<>(),
                (trigger,player) -> trigger.channel.getSyncStatus().isTriggerActive(trigger),true);
        addTrigger("gui",false,makeParameterSet(true,"resource_name"),
                Arrays.asList("identifier","resource_name"),new ArrayList<>(),(trigger,player) -> {
            Minecraft mc = Minecraft.getMinecraft();
            return (Objects.nonNull(mc.currentScreen) && (trigger.getResource().isEmpty() ||
                    trigger.getResource().contains("ANY") ||
                    (trigger.checkResourceMatch(mc.currentScreen.getClass().getName(),false)) ||
                    (trigger.getResource().contains("CREDITS") && mc.currentScreen instanceof GuiWinGame)));
        },true);
        addTrigger("advancement",false,makeParameterSet(true,"resource_name"),
                Arrays.asList("identifier","resource_name","persistence"),new ArrayList<>(),(trigger,player) -> {
            boolean pass = (ClientEvents.GAINED_NEW_ADVANCEMENT && (trigger.getResource().isEmpty() ||
                    trigger.getResource().contains("ANY") ||
                    trigger.checkResourceMatch(ClientEvents.LAST_ADVANCEMENT,false)));
            if(pass) ClientEvents.GAINED_NEW_ADVANCEMENT = false;
            return pass;
        },true);
        addTrigger("statistic",false,makeParameterSet(true,"resource_name","level"),
                Arrays.asList("identifier","resource_name"),new ArrayList<>(),(trigger,player) ->
                trigger.checkStat(trigger.getParameterInt("level"),player),true);
        addTrigger("command",false,makeParameterSet(true),
                Arrays.asList("identifier","persistence"),new ArrayList<>(),(trigger,player) -> {
            boolean pass = ClientEvents.commandHelper(trigger);
            if(pass) ClientEvents.commandFinish(trigger);
            return pass;
        },true);
        addTrigger("inventory",false,makeParameterSet(true,"items","slots"),
                Arrays.asList("identifier","items"),new ArrayList<>(),Trigger::checkInventory,true);
        addTrigger("blockentity",false,makeParameterSet(true,"resource_name","detection_range","detection_y_ratio"),
                Arrays.asList("identifier","resource_name"),new ArrayList<>(),Trigger::checkForTile,true);
        addTrigger("gamestage",false,Collections.singletonList("gamestages"),
                makeParameterSet(true,"resource_name","is_whitelist"),
                Arrays.asList("identifier","resource_name"),new ArrayList<>(),
                (trigger,player) -> trigger.whitelistHelper(GameStageHelper.clientHasAnyOf(
                        player,trigger.getResource())),true);
        addTrigger("bloodmoon",false,Arrays.asList("bloodmoon","nyx"),makeParameterSet(false),
                (trigger,player) -> (Loader.isModLoaded("bloodmoon") &&
                Bloodmoon.proxy.isBloodmoon()) || (Loader.isModLoaded("nyx") &&
                NyxWorld.get(player.getEntityWorld()).currentEvent instanceof BloodMoon),true);
        addTrigger("harvestmoon",false, Collections.singletonList("nyx"),makeParameterSet(false),
                (trigger, player) -> NyxWorld.get(player.getEntityWorld()).currentEvent instanceof HarvestMoon,true);
        addTrigger("fallingstars",false, Collections.singletonList("nyx"),makeParameterSet(false),
                (trigger, player) -> NyxWorld.get(player.getEntityWorld()).currentEvent instanceof StarShower,true);
        addTrigger("rainintensity",false,Collections.singletonList("dsurround"),
                makeParameterSet(true,"level"), Collections.singletonList("identifier"),new ArrayList<>(),
                (trigger,player) -> Weather.getIntensityLevel()>(((float)trigger.getParameterInt("level"))/100f),true);
        addTrigger("tornado",false,Collections.singletonList("weather2"),
                makeParameterSet(true,"level","detection_range"),Arrays.asList("identifier","level"),
                new ArrayList<>(),(trigger,player) ->  Objects.nonNull(WeatherDataHelper.getWeatherManagerForClient()) &&
                Objects.nonNull(WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()),
                        trigger.getParameterInt("detection_range"))) &&
                WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()),
                        trigger.getParameterInt("detection_range")).levelCurIntensityStage >= trigger.getParameterInt("level"),true);
        addTrigger("hurricane",false,Collections.singletonList("weather2"),
                makeParameterSet(true,"detection_range"),
                (trigger,player) -> Objects.nonNull(WeatherDataHelper.getWeatherManagerForClient()) &&
                Objects.nonNull(WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()),
                        trigger.getParameterInt("detection_range"))) &&
                WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()),
                        trigger.getParameterInt("detection_range")).isHurricane(),true);
        addTrigger("sandstorm",false,Collections.singletonList("weather2"),
                makeParameterSet(true,"detection_range"),(trigger,player) ->
                        Objects.nonNull(WeatherDataHelper.getWeatherManagerForClient()) &&
                Objects.nonNull(WeatherDataHelper.getWeatherManagerForClient()
                        .getClosestStormAny(new Vec3(player.getPosition()),
                        trigger.getParameterInt("detection_range"))) &&
                WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()),
                        trigger.getParameterInt("detection_range")).isHurricane(),true);
        addTrigger("season",false,Collections.singletonList("sereneseasons"),
                makeParameterSet(true,"level"),Arrays.asList("identifier","level"), new ArrayList<>(),
                (trigger,player) -> trigger.seasonHelper(SeasonHelper.getSeasonState(player.getEntityWorld()).getSeason()),true);
    }

    private static void addDefaultParameterOverrides() {
        addTriggerParameterDefualt("mob","level",1);
        addTriggerParameterDefualt("drowning","level",100);
        addTriggerParameterDefualt("light","level",7);
        addTriggerParameterDefualt("lowhp","level",30);
    }

    public static <T> void addParameter(String name, T defaultValue) throws IllegalArgumentException {
        if(DEFAULT_PARAMETER_MAP.containsKey(name)) throw new IllegalArgumentException("Parameter with name "+name+"already exists!");
        DEFAULT_PARAMETER_MAP.put(name,new DefaultParameter(name,defaultValue));
    }

    /**
     * Vanilla + no required parameters
     */
    public static void addTrigger(String name, boolean isServerSide, HashSet<String> acceptedParameters,
                                  BiFunction<Trigger, EntityPlayerSP, Boolean> activationFunction,
                                  boolean forceOverwrite) throws IllegalArgumentException {
        addTrigger(name,isServerSide,new ArrayList<>(),acceptedParameters,new ArrayList<>(),new ArrayList<>(),
                activationFunction,forceOverwrite);
    }

    /**
     * No required parameters
     */
    public static void addTrigger(String name, boolean isServerSide,
                                  List<String> requiredMods,HashSet<String> acceptedParameters,
                                  BiFunction<Trigger, EntityPlayerSP, Boolean> activationFunction,
                                  boolean forceOverwrite) throws IllegalArgumentException {
        addTrigger(name,isServerSide,requiredMods,acceptedParameters,new ArrayList<>(),new ArrayList<>(),
                activationFunction,forceOverwrite);
    }

    /**
     * Vanilla trigger
     */
    public static void addTrigger(String name, boolean isServerSide, HashSet<String> acceptedParameters,
                                  List<String> requiredParameters, List<String> choiceRequiredParameters,
                                  BiFunction<Trigger, EntityPlayerSP, Boolean> activationFunction,
                                  boolean forceOverwrite) throws IllegalArgumentException {
        addTrigger(name,isServerSide,new ArrayList<>(),acceptedParameters,requiredParameters,choiceRequiredParameters,
                activationFunction,forceOverwrite);
    }

    /**
     * Only enable forceOverwrite if you know what you are doing as it can break compatability and base functionality.
     */
    public static void addTrigger(String name, boolean isServerSide, List<String> requiredMods,
                                  HashSet<String> acceptedParameters, List<String> requiredParameters,
                                  List<String> choiceRequiredParameters,
                                  BiFunction<Trigger, EntityPlayerSP, Boolean> activationFunction,
                                  boolean forceOverwrite) throws IllegalArgumentException {
        if(ALL_TRIGGERS.contains(name)) {
            if(!forceOverwrite)
                throw new IllegalArgumentException("Trigger with name " + name + "already exists! If you want to " +
                        "overwrite a trigger and you know what you are doing, enable forceOverwrite");
        } else ALL_TRIGGERS.add(name);
        if(!isServerSide || !ConfigRegistry.CLIENT_SIDE_ONLY) {
            if (requiredMods.isEmpty()) ACCEPTED_TRIGGERS.add(name);
            else {
                for (String modid : requiredMods) {
                    if (Loader.isModLoaded(modid)) {
                        ACCEPTED_TRIGGERS.add(name);
                        break;
                    }
                }
            }
        }
        if(ACCEPTED_TRIGGERS.contains(name)) {
            if(isServerSide)
                SERVER_TRIGGERS.add(name);
            ACCEPTED_PARAMETERS.put(name, acceptedParameters);
            REQUIRED_PARAMETERS.put(name, requiredParameters);
            CHOICE_REQUIRED_PARAMETERS.put(name, choiceRequiredParameters);
            TRIGGER_CONDITIONS.put(name, activationFunction);
        }
    }

    public static void addTriggerParameterDefualt(String trigger, String parameter, Object value) {
        if(!DEFAULT_PARAMETER_MAP.containsKey(parameter))
            MusicTriggers.logExternally(Level.ERROR,"Cannot add parameter override for nonexistant " +
                    "parameter {}",parameter);
        else DEFAULT_PARAMETER_MAP.get(parameter).addTriggerDefault(trigger,value);
    }

    public static HashSet<String> getAcceptedTriggers() {
        return ACCEPTED_TRIGGERS;
    }

    public static boolean isServerSide(String name) {
        return SERVER_TRIGGERS.contains(name);
    }

    public static HashSet<String> getAcceptedParameters(String trigger) {
        return ACCEPTED_PARAMETERS.get(trigger);
    }

    public static boolean isParameterAccepted(String trigger, String parameter) {
        return ACCEPTED_PARAMETERS.containsKey(trigger) && ACCEPTED_PARAMETERS.get(trigger).contains(parameter);
    }

    public static boolean isDefaultList(String parameter) {
        return DEFAULT_PARAMETER_MAP.get(parameter).value instanceof List<?>;
    }

    public static boolean isNonDefaultParameter(String trigger, String parameter, Object val) {
        return !DEFAULT_PARAMETER_MAP.get(parameter).isEquivalent(trigger,val);
    }

    public static Object getDefaultParameter(String trigger, String parameter) {
        return DEFAULT_PARAMETER_MAP.get(parameter).getValue(trigger);
    }

    public static boolean isLoaded(String trigger) {
        return ACCEPTED_TRIGGERS.contains(trigger);
    }

    public static void encodeDefaultParameters(ByteBuf buf) {
        HashSet<String> serverParameters = new HashSet<>();
        HashSet<String> nonSpecificParameters = makeParameterSet(false);
        nonSpecificParameters.remove("not");
        for(String trigger : SERVER_TRIGGERS)
            for(String parameter : getAcceptedParameters(trigger))
                if(!nonSpecificParameters.contains(parameter))
                    serverParameters.add(parameter);
        Map<String,DefaultParameter> defaultParameterMap = new HashMap<>();
        for(String parameter : serverParameters)
            defaultParameterMap.put(parameter,DEFAULT_PARAMETER_MAP.get(parameter));
        NetworkUtil.writeGenericMap(buf,defaultParameterMap,NetworkUtil::writeString,
                (buf1,parameter) -> parameter.encode(buf1));
    }


    public static int getUniversalInt(@Nullable Table universal, String parameter, int fallback) {
        return Objects.isNull(universal) ? fallback : MusicTriggers.randomInt("universal_"+parameter,
                universal.getValOrDefault(parameter,String.valueOf(fallback)),fallback);
    }

    public static float getUniversalFloat(@Nullable Table universal, String parameter, float fallback) {
        return Objects.isNull(universal) ? fallback : MusicTriggers.randomFloat("universal_"+parameter,
                universal.getValOrDefault(parameter,String.valueOf(fallback)),fallback);
    }

    public static boolean getUniversalBool(@Nullable Table universal, String parameter, boolean fallback) {
        return Objects.isNull(universal) ? fallback : universal.getValOrDefault(parameter,fallback);
    }

    public static String getUniversalString(@Nullable Table universal, String parameter, String fallback) {
        return Objects.isNull(universal) ? fallback : universal.getValOrDefault(parameter,fallback);
    }

    public static void updateEffectCache(EntityPlayerSP player) {
        if(Objects.isNull(player)) return;
        Set<String> activeEffects = player.getActivePotionEffects().stream()
                .map(instance -> Objects.isNull(instance) ? null : instance.getEffectName())
                .filter(Objects::nonNull).collect(Collectors.toSet());
        synchronized (CACHED_EFFECTS) {
            CACHED_EFFECTS.addAll(activeEffects);
            CACHED_EFFECTS.removeIf(effect -> !activeEffects.contains(effect));
        }
    }

    public static Set<String> getCachedEffects() {
        synchronized (CACHED_EFFECTS) {
            return CACHED_EFFECTS;
        }
    }

    private final Cache cache;
    private final Channel channel;
    private final String name;
    private final HashMap<String, Object> parameters;
    private final List<Table> linkTables;
    private final HashMap<Integer, Link> parsedLinkMap;
    private boolean isToggled;
    private boolean canPlayMoreAudio;

    public Trigger(String name, Channel channel, List<Table> links) {
        this.cache = new Cache();
        this.name = name;
        this.channel = channel;
        this.parameters = buildDefaultParameters(name);
        this.linkTables = links;
        this.parsedLinkMap = new HashMap<>();
        this.isToggled = false;
        this.canPlayMoreAudio = true;
    }

    private HashMap<String, Object> buildDefaultParameters(String trigger) {
        HashMap<String, Object> ret = new HashMap<>();
        for(String parameter : ACCEPTED_PARAMETERS.get(trigger))
            ret.put(parameter, DEFAULT_PARAMETER_MAP.get(parameter).getValue(trigger));
        return ret;
    }

    public boolean hasAllRequiredParameters() {
        if(REQUIRED_PARAMETERS.containsKey(this.name) && !REQUIRED_PARAMETERS.get(this.name).isEmpty()) {
            for(String parameter : REQUIRED_PARAMETERS.get(this.name)) {
                if(isDefault(parameter)) {
                    MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Trigger {} is missing required parameter {} so it"+
                            " will be skipped!",this.channel,this.name,parameter);
                    return false;
                }
            }
        }
        if(CHOICE_REQUIRED_PARAMETERS.containsKey(this.name) && !CHOICE_REQUIRED_PARAMETERS.get(this.name).isEmpty()) {
            boolean fail = false;
            StringBuilder builder = new StringBuilder();
            for(String parameter : REQUIRED_PARAMETERS.get(this.name)) {
                builder.append(parameter).append(" ");
                if(isDefault(parameter)) {
                    fail = true;
                    break;
                }
            }
            if(fail) {
                MusicTriggers.logExternally(Level.WARN, "Channel[{}] -  Trigger {} requires one the following parameters: {}",
                        this.channel,this.name,builder.toString());
                return false;
            }
        }
        this.isToggled = getParameterBool("start_toggled");
        return true;
    }

    public String getName() {
        return this.name;
    }

    public String getNameWithID() {
        if(hasID()) return this.name+"-"+ getParameterString("identifier");
        return getName();
    }

    @Override
    public String toString() {
        return getNameWithID();
    }

    public void initCache() {
        this.cache.initCache();
    }

    public void setParameter(String parameter, Object value) {
        if(parameter.matches("id")) parameter = "identifier";
        this.parameters.put(parameter, value);
    }

    private boolean isDefault(String parameter) {
        return DEFAULT_PARAMETER_MAP.get(parameter).isEquivalent(this.name,this.parameters.get(parameter));
    }

    public boolean hasID() {
        return isParameterAccepted(this.name,"identifier");
    }

    public String getParameterString(String parameter) {
        return this.parameters.get(parameter).toString();
    }

    public boolean getParameterBool(String parameter) {
        return Boolean.parseBoolean(getParameterString(parameter));
    }

    public int getParameterInt(String parameter) {
        int defVal = 0;
        try {
            defVal = DEFAULT_PARAMETER_MAP.get(parameter).getAsInt(this.name);
        } catch (NumberFormatException ex) {
            MusicTriggers.logExternally(Level.ERROR,"Tried to access default value of parameter {} incorrectly " +
                    "as an integer! Using substitute value of 0",parameter);
        }
        Object val = this.parameters.get(parameter);
        if(val instanceof String) return MusicTriggers.randomInt(parameter,(String) val,defVal);
        if(val instanceof Number) return ((Number)val).intValue();
        MusicTriggers.logExternally(Level.ERROR,"Tried to access parameter {} as an integer when it was " +
                "not stored as a number or String! Using default value {}",parameter,defVal);
        return defVal;
    }

    public float getParameterFloat(String parameter) {
        float defVal = 0;
        try {
            defVal = DEFAULT_PARAMETER_MAP.get(parameter).getAsFloat(this.name);
        } catch (NumberFormatException ex) {
            MusicTriggers.logExternally(Level.ERROR,"Tried to access default value of parameter {} incorrectly " +
                    "as a float! Using substitute value of 0",parameter);
        }
        Object val = this.parameters.get(parameter);
        if(val instanceof String) return MusicTriggers.randomFloat(parameter,(String) val,defVal);
        if(val instanceof Number) return ((Number)val).floatValue();
        MusicTriggers.logExternally(Level.ERROR,"Tried to access parameter {} as a float when it was not " +
                "stored as a number or String! Using default value {}",parameter,defVal);
        return defVal;
    }

    private List<?> getDefaultListParameter(String parameter) {
        try {
            return DEFAULT_PARAMETER_MAP.get(parameter).getAsList(this.name);
        } catch (IllegalArgumentException ex) {
            MusicTriggers.logExternally(Level.ERROR,"Tried to access default value of parameter {} incorrectly " +
                    "as a list! Using substitute list with element ANY",parameter);
        }
        return Collections.singletonList("ANY");
    }

    private List<String> getParameterStringList(String parameter) {
        List<?> defVal = getDefaultListParameter(parameter);
        Object val = this.parameters.get(parameter);
        if(val instanceof String) return Collections.singletonList((String)val);
        if(!(val instanceof List<?>)) {
            MusicTriggers.logExternally(Level.ERROR,"Tried to access parameter {} as a list when it was not " +
                    "stored as a list or string! Using default value {}",parameter,defVal);
            return makeStringList(defVal);
        }
        List<?> ret = (List<?>)val;
        if(ret.isEmpty()) {
            MusicTriggers.logExternally(Level.ERROR,"Parameter {} was stored as an empty list! Using default " +
                    "value {}",parameter,defVal);
            return makeStringList(defVal);
        }
        return makeStringList(ret);
    }

    private List<String> makeStringList(List<?> genericList) {
        List<String> ret = new ArrayList<>();
        for(Object element : genericList) {
            String asString = element.toString();
            if(!ret.contains(asString)) ret.add(asString);
        }
        return ret;
    }

    public boolean runActivationFunction(EntityPlayerSP player) {
        return TRIGGER_CONDITIONS.containsKey(getName()) && isActive(TRIGGER_CONDITIONS.get(getName()).apply(this,player));
    }

    @SuppressWarnings("unchecked")
    public <T> T getParameterWithUniversal(String parameter, @Nullable Table universal, T fallback) {
        try {
            return (T) this.cache.universalCache.getOrDefault(parameter,makeUniversalParameterCache(parameter,universal,fallback));
        } catch (ClassCastException ex) {
            MusicTriggers.logExternally(Level.ERROR,"Failed to get parameter with potential universal value {} "+
                            "for trigger {}! Default value of {} will be set. See the main log for the full stacktrace.",
                    parameter,getNameWithID(),DEFAULT_PARAMETER_MAP.get(parameter).value);
            Constants.MAIN_LOG.error("Failed to get parameter with potential universal value {} "+
                    "for trigger {}! Is the parameter stored incorrectly?",parameter,getNameWithID(),ex);
            this.parameters.put(parameter,DEFAULT_PARAMETER_MAP.get(parameter).value);
            return fallback;
        }
    }

    private <T> T makeUniversalParameterCache(String parameter, @Nullable Table universal, T fallback) {
        T cached = isDefault(parameter) ? getUniversalType(parameter,universal,fallback) : getParameterType(parameter,fallback);
        this.cache.universalCache.put(parameter,cached);
        return cached;
    }

    @SuppressWarnings("unchecked")
    private <T> T getParameterType(String parameter, T fallback) {
        if(fallback instanceof Number) {
            if(fallback instanceof Float || fallback instanceof Double)
                return (T)(Float)getParameterFloat(parameter);
            return (T)(Integer)getParameterInt(parameter);
        }
        if(fallback instanceof Boolean) return (T)(Boolean)getParameterBool(parameter);
        if(fallback instanceof String) return (T)getParameterString(parameter);
        if(fallback instanceof Collection<?>) return (T)getParameterStringList(parameter);
        return fallback;
    }

    @SuppressWarnings("unchecked")
    private <T> T getUniversalType(String parameter, @Nullable Table universal, T fallback) {
        if(fallback instanceof Number) {
            if(fallback instanceof Float || fallback instanceof Double)
                return (T)(Float)getUniversalFloat(universal,parameter,(Float)fallback);
            return (T)(Integer)getUniversalInt(universal,parameter,(Integer)fallback);
        }
        if(fallback instanceof Boolean) return (T)(Boolean)getUniversalBool(universal,parameter,(Boolean)fallback);
        if(fallback instanceof String) return (T)getUniversalString(universal,parameter,(String)fallback);
        return fallback;
    }

    private boolean isActive(boolean active) {
        if(getParameterBool("not")) return !active;
        return active;
    }

    public boolean timeTriggerExtras(long time, int worldPhase) {
        int day = (int)(time/24000);
        int phase = getParameterInt("moon_phase");
        return (day>=getParameterInt("lowest_day_number") && day<=getParameterInt("highest_day_number")) &&
                (phase<=0 || phase>8 || phase==worldPhase);
    }

    public boolean handleHeight(int playerY, boolean visibleSky) {
        return getParameterBool("check_above_level") ? playerY>getParameterInt("level") :
                ((!visibleSky || !getParameterBool("check_for_sky")) && playerY<getParameterInt("level"));
    }

    public boolean handleHP(float health, float maxHealth) {
        return health<(maxHealth*(((float) getParameterInt("level"))/100f));
    }

    public Set<String> getResource() {
        return this.cache.resourceCache;
    }

    public boolean zoneHelper(int x, int y, int z) {
        return x>getParameterInt("zone_min_x") && x<getParameterInt("zone_max_x") &&
                y>getParameterInt("zone_min_y") && y<getParameterInt("zone_max_y") &&
                z>getParameterInt("zone_min_z") && z<getParameterInt("zone_max_z");
    }

    public boolean difficultyHelper(EnumDifficulty difficulty, boolean hardcore) {
        int diff = getParameterInt("level");
        return (diff==4 && hardcore) || (diff==3 && difficulty==EnumDifficulty.HARD) ||
                (diff==2 && difficulty==EnumDifficulty.NORMAL) || (diff==1 && difficulty==EnumDifficulty.EASY) ||
                (diff==0 && difficulty==EnumDifficulty.PEACEFUL);
    }

    public boolean whitelistHelper(boolean initial) {
        boolean whitelist = getParameterBool("is_whitelist");
        return (whitelist && initial) || (!whitelist && !initial);
    }

    public boolean seasonHelper(Season season) {
        int id = getParameterInt("level");
        return (id==0 && season==Season.SPRING) || (id==1 && season==Season.SUMMER) || (id==2 && season==Season.AUTUMN) ||
                (id==3 && season==Season.WINTER);
    }

    public BlockPos roundedPos(EntityPlayer p) {
        return new BlockPos((Math.round(p.posX * 2) / 2.0), (Math.round(p.posY * 2) / 2.0), (Math.round(p.posZ * 2) / 2.0));
    }


    public double averageLight(BlockPos p, String lightType, World world) {
        if(lightType.matches("block") || lightType.matches("sky"))
            return lightType.matches("block") ?
                    world.getLightFor(EnumSkyBlock.BLOCK, p) : world.getLightFor(EnumSkyBlock.SKY, p);
        return world.getLight(p, true);
    }

    public boolean checkBiome(Biome b, List<String> categories, String rainType,
                              float temperature, boolean cold, float rainfall, boolean togglerainfall) {
        if(Objects.isNull(b.getRegistryName())) return false;
        Set<String> names = getResource();
        boolean pass = !names.contains("ANY") && (names.isEmpty() ||
                checkResourceMatch(b.getRegistryName().toString(),false));
        if(!pass) pass = !categories.contains("ANY") && (categories.isEmpty() ||
                checkOtherMatch(b.getTempCategory().toString(),categories,false));
        if(!pass) return false;
        pass = false;
        if(rainfall==Float.MIN_VALUE) pass = true;
        else if(b.getRainfall()>rainfall && togglerainfall) pass = true;
        else if(b.getRainfall()<rainfall && !togglerainfall) pass = true;
        if(pass) {
            if (rainType.matches("ANY"))
                return biomeTemp(b,temperature,cold);
            else if (rainType.matches("none") && !b.canRain())
                return biomeTemp(b,temperature,cold);
            else if (b.canRain() && rainType.matches("rain"))
                return biomeTemp(b,temperature,cold);
            else if (b.isSnowyBiome() && rainType.matches("snow"))
                return biomeTemp(b,temperature,cold);
        }
        return false;
    }

    private boolean biomeTemp(Biome b, float temperature, boolean cold) {
        float bt = b.getDefaultTemperature();
        if (temperature == Float.MIN_VALUE) return true;
        else if (bt >= temperature && !cold) return true;
        else return bt <= temperature && cold;
    }

    public boolean checkResourceMatch(String type, boolean match) {
        if(this.cache.isTypeCached("resource")) {
            for(String resource : this.cache.resourceCache) {
                if (match && type.matches(resource)) return true;
                else if (!match && type.contains(resource)) return true;
            }
        }
        return false;
    }

    public boolean checkOtherMatch(String type, Collection<String> otherList, boolean match) {
        for(String resource : otherList) {
            if (match && type.matches(resource)) return true;
            else if (!match && type.contains(resource)) return true;
        }
        return false;
    }

    @SuppressWarnings("ConstantConditions")
    public boolean checkDimensionList(int playerDim) {
        if(this.cache.isTypeCached("resource")) {
            for (String resource : this.cache.resourceCache)
                if ((String.valueOf(playerDim)).matches(resource)) return true;
            try {
                DimensionType dimension = DimensionType.getById(playerDim);
                return Objects.nonNull(dimension.name()) && checkResourceMatch(dimension.name(), false);
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }
        return false;
    }

    public boolean checkRiding(EntityPlayerSP player) {
        if(!player.isRiding() || Objects.isNull(player.getRidingEntity())) return false;
        if(getResource().isEmpty() || getResource().contains("ANY")) return true;
        if(checkResourceMatch(player.getRidingEntity().getName(),true)) return true;
        if(Objects.isNull(EntityList.getKey(player.getRidingEntity()))) return false;
        return checkResourceMatch(EntityList.getKey(player.getRidingEntity()).toString(),false);
    }

    public boolean checkStat(int level, EntityPlayerSP player) {
        if(this.cache.isTypeCached("statistic")) {
            NetHandlerPlayClient net = Minecraft.getMinecraft().getConnection();
            if(Objects.nonNull(net)) {
                net.sendPacket(new CPacketClientStatus(CPacketClientStatus.State.REQUEST_STATS));
                for(StatBase s : this.cache.statCache)
                    if(player.getStatFileWriter().readStat(s) > level)
                        return true;
            }
        }
        return false;
    }

    public boolean checkInventory(EntityPlayerSP player) {
        List<String> slotMatchers = getParameterStringList("slots");
        List<String> itemMatchers = getParameterStringList("items");
        if(slotMatchers.isEmpty() || itemMatchers.isEmpty()) return false;
        InventoryPlayer inventory = player.inventory;
        for(String slotMatcher : slotMatchers) {
            if(slotMatcher.matches("MAINHAND")) {
                if (itemMatchesAny(player.getHeldItemMainhand(), itemMatchers)) return true;
            } else if(slotMatcher.matches("OFFHAND")) {
                if (itemMatchesAny(player.getHeldItemOffhand(), itemMatchers)) return true;
            } else if(slotMatcher.matches("HOTBAR")) {
                for(int i=0;i<9;i++)
                    if(itemMatchesAny(inventory.getStackInSlot(i),itemMatchers)) return true;
            } else if(slotMatcher.matches("ARMOR")) {
                for(ItemStack stack : inventory.armorInventory)
                    if(itemMatchesAny(stack,itemMatchers)) return true;
            } else if(slotMatcher.matches("ANY")) {
                for(NonNullList<ItemStack> stackList : inventory.allInventories)
                    for(ItemStack stack : stackList)
                        if(itemMatchesAny(stack,itemMatchers)) return true;
            } else {
                int slot = MusicTriggers.randomInt("inventory_slot_number",slotMatcher,-1);
                if(slot>=0) return itemMatchesAny(inventory.getStackInSlot(slot),itemMatchers);
            }
        }
        return false;
    }

    private boolean itemMatchesAny(ItemStack stack, List<String> itemMatchers) {
        for(String itemMatcher : itemMatchers)
            if(itemMatches(stack,itemMatcher)) return true;
        return false;
    }

    private boolean itemMatches(ItemStack stack, String parsed) {
        String[] parts = parsed.split(":");
        if(parts.length==0) return false;
        if(parts.length==1) return parts[0].matches("EMPTY") && stack==ItemStack.EMPTY;
        ResourceLocation source = new ResourceLocation(parts[0],parts[1]);
        if(ForgeRegistries.ITEMS.containsKey(source) && ForgeRegistries.ITEMS.getValue(source)==stack.getItem()) {
            if(parts.length==2) return true;
            if(stack.getCount()>=MusicTriggers.randomInt("parsed_item_count",parts[2],0)) {
                if(parts.length==3) return true;
                StringBuilder builder = new StringBuilder();
                for(int i=3;i<parts.length;i++) {
                    builder.append(parts[i]);
                    if(i<parts.length-1) builder.append(":");
                }
                return checkNBT(stack, builder.toString());
            }
        }
        return false;
    }

    private boolean checkNBT(ItemStack stack, String nbt) {
        if(nbt.matches("ANY")) return true;
        String[] parts = nbt.split(";");
        try {
            if(parts.length==0) return true;
            if(NBT_MODES.contains(parts[0])) {
                if(parts.length==1) return false;
                NBTTagCompound data = stack.serializeNBT();
                switch (parts[0]) {
                    case "KEY_PRESENT" : {
                        if(parts[1].matches("INVERT")) {
                            if(parts.length==2) return false;
                            NBTBase finalKey = getFinalTag(data,Arrays.copyOfRange(parts,2,parts.length));
                            return Objects.isNull(finalKey) || !(finalKey instanceof NBTTagCompound);
                        }
                        NBTBase finalKey = getFinalTag(data,Arrays.copyOfRange(parts,1,parts.length));
                        return Objects.nonNull(finalKey) && finalKey instanceof NBTTagCompound;
                    }
                    case "VAL_PRESENT" : {
                        if(parts[1].matches("INVERT")) {
                            if(parts.length==2) return false;
                            NBTBase finalVal = getFinalTag(data,Arrays.copyOfRange(parts,2,parts.length));
                            return Objects.isNull(finalVal) || finalVal instanceof NBTTagCompound;
                        }
                        NBTBase finalVal = getFinalTag(data,Arrays.copyOfRange(parts,1,parts.length));
                        return Objects.nonNull(finalVal) && !(finalVal instanceof NBTTagCompound);
                    }
                    case "INVERT" : {
                        boolean compare = Boolean.parseBoolean(parts[parts.length-1]);
                        NBTBase finalVal = getFinalTag(data,Arrays.copyOfRange(parts,1,parts.length-1));
                        if(finalVal instanceof NBTTagByte)
                            return (compare && ((NBTTagByte)finalVal).getByte()==1) ||
                                    (!compare && ((NBTTagByte)finalVal).getByte()==0);
                        return false;
                    }
                    case "GREATER" : {
                        double compare = Double.parseDouble(parts[parts.length-1]);
                        int from = 1;
                        if(parts[1].matches("EQUAL")) {
                            if(parts.length==2) return false;
                            from = 2;
                        }
                        return numNBT(getFinalTag(data,Arrays.copyOfRange(parts,from,parts.length-1)),compare,true,from==2);
                    }
                    case "LESSER" : {
                        double compare = Double.parseDouble(parts[parts.length-1]);
                        int from = 1;
                        if(parts[1].matches("EQUAL")) {
                            if(parts.length==2) return false;
                            from = 2;
                        }
                        return numNBT(getFinalTag(data,Arrays.copyOfRange(parts,from,parts.length-1)),compare,false,from==2);
                    }
                    default: {
                        int from = 1;
                        boolean pass = false;
                        if(parts[1].matches("INVERT")) {
                            if(parts.length==2) return false;
                            from = 2;
                        }
                        NBTBase finalVal = getFinalTag(data,Arrays.copyOfRange(parts,from,parts.length-1));
                        if(finalVal instanceof NBTTagCompound) return false;
                        if(finalVal instanceof  NBTTagByte) {
                            boolean compare = Boolean.parseBoolean(parts[parts.length-1]);
                            pass = (compare && ((NBTTagByte) finalVal).getByte() == 0) ||
                                    (!compare && ((NBTTagByte) finalVal).getByte() == 1);
                        }
                        else if(finalVal instanceof NBTPrimitive)
                            pass = ((NBTPrimitive)finalVal).getDouble()==Double.parseDouble(parts[parts.length-1]);
                        else if(finalVal instanceof NBTTagString) {
                            pass = ((NBTTagString)finalVal).getString().trim().toLowerCase()
                                    .matches(parts[parts.length-1].trim().toLowerCase());
                        }
                        return pass || from==2;
                    }
                }
            }
            return false;
        } catch (NumberFormatException e) {
            MusicTriggers.logExternally(Level.ERROR, "Tried to check numerical value of NBT data against a non " +
                    "numerical value of {}",parts[parts.length-1]);
        } catch (Exception ignored) {
        }
        return false;
    }

    private NBTBase getFinalTag(NBTTagCompound tag, String[] parts) {
        if(!tag.hasKey(parts[0])) return null;
        if(parts.length==1) return tag.getTag(parts[0]);
        try {
            return getFinalTag(tag.getCompoundTag(parts[0]), Arrays.copyOfRange(parts,1,parts.length));
        } catch (ClassCastException ignored) {
            return null;
        }
    }

    private boolean numNBT(NBTBase tag, double comp, boolean greater, boolean equal) {
        if(!(tag instanceof NBTPrimitive)) return false;
        double val = ((NBTPrimitive)tag).getDouble();
        return greater ? equal ? val>=comp : val>comp : equal ? val<=comp : val<comp;
    }

    public boolean checkForTile(EntityPlayerSP player) {
        float range = (float)getParameterInt("detection_range");
        float yRatio = getParameterFloat("detection_y_ratio");
        BlockPos pos = roundedPos(player);
        AxisAlignedBB box = new AxisAlignedBB(pos.getX()-range,pos.getY()-(range*yRatio), pos.getZ()-range,
                pos.getX()+range,pos.getY()+(range*yRatio),pos.getZ()+range);
        for(TileEntity loadedTile : player.getEntityWorld().loadedTileEntityList) {
            for(Class<? extends TileEntity> tileClass : this.cache.blockEntityCache) {
                if (tileClass.isAssignableFrom(loadedTile.getClass())) {
                    BlockPos tilePos = loadedTile.getPos();
                    return tilePos.getX() <= box.maxX && tilePos.getX() >= box.minX && tilePos.getY() <= box.maxY &&
                            tilePos.getY() >= box.minY && tilePos.getZ() <= box.maxZ && tilePos.getZ() >= box.minZ;
                }
            }
        }
        return false;
    }

    public boolean isToggled() {
        return this.isToggled;
    }

    public void setToggle(boolean state, boolean needsUpdate) {
        if(needsUpdate && !this.isToggled==state && getParameterInt("toggle_save_status")==2)
            this.channel.storeToggleState(this,state);
        this.isToggled = state;
    }
    
    public boolean maxedAudioCount() {
        return !this.canPlayMoreAudio;
    }

    public void onAudioFinish(int numTracksPlayed) {
        int maxTracks = getParameterInt("max_tracks");
        this.canPlayMoreAudio = maxTracks<=0 || numTracksPlayed<maxTracks;
    }

    public void reenableAudio() {
        this.canPlayMoreAudio = true;
    }

    public void onLogOut() {
        if(getParameterInt("toggle_save_status")>=1)
            this.isToggled = getParameterBool("start_toggled");
    }

    public void parseLinks() {
        int index = 0;
        for(Table link : this.linkTables) {
            Link readLink = new Link(this.channel.getChannelName(),this,link);
            if (readLink.isValid()) {
                Constants.debugError("VALIDATED LINK FROM {} TO {}",readLink.parentChannel,readLink.linkedChannel);
                this.parsedLinkMap.put(index, readLink);
                index++;
            } else MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Link table at index {} for song {} " +
                    "was invalid! Please double check that the channel is valid and there is at least 1 valid trigger " +
                    "set.",this.channel.getChannelName(),index,getName());
        }
    }

    public Collection<Link> getLinks() {
        return Collections.unmodifiableCollection(this.parsedLinkMap.values());
    }

    class Cache {
        private final Map<String, Object> universalCache;
        /**
         * Current types: [ "resource" "statistic" "blockentity" ]
         */
        private final Set<String> typesCached;
        private final Set<String> resourceCache;
        private final Set<StatBase> statCache;
        private final Set<Class<? extends TileEntity>> blockEntityCache;

        private Cache() {
            this.universalCache = new HashMap<>();
            this.typesCached = new HashSet<>();
            this.resourceCache = new HashSet<>();
            this.statCache = new HashSet<>();
            this.blockEntityCache = new HashSet<>();
        }

        private boolean isTypeCached(String type) {
            return this.typesCached.contains(type);
        }

        private boolean isTrigger(String trigger) {
            return Trigger.this.name.matches(trigger);
        }

        private void initCache() {
            if(isParameterAccepted(Trigger.this.name,"resource_name")) {
                this.resourceCache.addAll(getParameterStringList("resource_name"));
                this.typesCached.add("resource");
            }
            if(isTrigger("statistic")) {
                makeStatCache();
                this.typesCached.add("statistic");
            }
            if(isTrigger("blockentity")) {
                for(String resource : this.resourceCache) {
                    ResourceLocation location = new ResourceLocation(resource);
                    if(TileEntity.REGISTRY.containsKey(location))
                        this.blockEntityCache.add(TileEntity.REGISTRY.getObject(location));
                }
                this.typesCached.add("blockentity");
            }
        }

        private void makeStatCache() {
            this.statCache.clear();
            for(StatBase stat : StatList.ALL_STATS)
                if(Objects.nonNull(stat) && checkResourceMatch(stat.statId,false))
                    this.statCache.add(stat);
        }
    }

    public static final class DefaultParameter {
        private final String name;
        private final Object value;
        private final Map<String,Object> triggerValues;

        public DefaultParameter(ByteBuf buf) {
            this.name = NetworkUtil.readString(buf);
            this.value = NetworkUtil.parseGenericObj(buf);
            this.triggerValues = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,NetworkUtil::parseGenericObj);
        }

        private DefaultParameter(String name, Object value) {
            this.name = name;
            this.value = value;
            this.triggerValues = new HashMap<>();
        }

        private void addTriggerDefault(String trigger, Object triggerValue) {
            if(this.value.getClass().isAssignableFrom(triggerValue.getClass()))
                this.triggerValues.put(trigger, triggerValue);
            else MusicTriggers.logExternally(Level.ERROR,"Cannot add trigger based default for trigger {} with " +
                    "type {} to parameter {} that has type {}",trigger,triggerValue.getClass().getName(),this.name,
                    this.value.getClass().getName());
        }

        public Object getValue(String trigger) {
            return this.triggerValues.getOrDefault(trigger,this.value);
        }

        private boolean isEquivalent(String trigger, Object otherVal) {
            return isEquivalent(getValue(trigger),otherVal);
        }

        private boolean isEquivalent(Object defVal, Object otherVal) {
            if(!defVal.getClass().isAssignableFrom(otherVal.getClass())) return false;
            if(!List.class.isAssignableFrom(otherVal.getClass())) return defVal.toString().matches(otherVal.toString());
            List<?> defList = (List<?>)defVal;
            List<?> otherList = (List<?>)otherVal;
            if(defList.size()!=otherList.size()) return false;
            for(int i=0;i<defList.size();i++)
                if(!isEquivalent(defList.get(i),otherList.get(i))) return false;
            return true;
        }

        public int getAsInt(String trigger) throws NumberFormatException {
            Object val = this.triggerValues.getOrDefault(trigger, this.value);
            if(val instanceof String) return Integer.parseInt((String)val);
            if(val instanceof Number) return ((Number)val).intValue();
            throw new NumberFormatException("Cannot cast parameter "+this.name+" stored as type "+
                    val.getClass().getName()+" to an integer!");
        }

        public float getAsFloat(String trigger) throws NumberFormatException {
            Object val = this.triggerValues.getOrDefault(trigger, this.value);
            if(val instanceof String) return Float.parseFloat((String)val);
            if(val instanceof Number) return ((Number)val).floatValue();
            throw new NumberFormatException("Cannot cast parameter "+this.name+" stored as type "+
                    val.getClass().getName()+" to a float!");
        }

        public List<?> getAsList(String trigger) throws TypeNotPresentException {
            Object val = this.triggerValues.getOrDefault(trigger, this.value);
            if(val instanceof List<?>) return (List<?>)val;
            throw new IllegalArgumentException("Cannot cast parameter "+this.name+" stored as type "+
                    val.getClass().getName()+" to a list!");
        }

        public void encode(ByteBuf buf) {
            Map<String,Object> triggerDefualts = new HashMap<>();
            for(Map.Entry<String,Object> triggerEntry : this.triggerValues.entrySet())
                if(Trigger.isServerSide(triggerEntry.getKey()))
                    triggerDefualts.put(triggerEntry.getKey(),triggerEntry.getValue());
            NetworkUtil.writeString(buf,this.name);
            NetworkUtil.writeGenericObj(buf,this.value);
            NetworkUtil.writeGenericMap(buf,triggerDefualts,NetworkUtil::writeString,NetworkUtil::writeGenericObj);
        }
    }

    public static final class Link {

        private final Channel parentChannel;
        private final Channel linkedChannel;
        private final Trigger parentTrigger;
        private final List<Trigger> requiredTriggers;
        private final List<Trigger> linkTo;
        private final boolean inheritTime;
        private final boolean resume;
        private final MutableLong interuptedTime;
        private final MutableLong linkedTime;
        private Audio resumeFrom;
        private Audio resumeTo;

        private Link(String parentChannel, Trigger parentTrigger, Table data) {
            this.parentChannel = parentTrigger.channel;
            String channel = data.getValOrDefault("channel","jukebox");
            this.linkedChannel = Objects.nonNull(channel) && !channel.isEmpty() && !channel.matches("jukebox") ?
                    ChannelManager.getNonDefaultChannel(channel) : null;
            if(Objects.isNull(this.linkedChannel)) {
                MusicTriggers.logExternally(Level.WARN,"Channel [{}] - Incorrect channel name {} in link table " +
                        "for trigger {}!",parentChannel,channel,parentTrigger.getName());
                this.parentTrigger = null;
                this.requiredTriggers = new ArrayList<>();
                this.linkTo = new ArrayList<>();
            }
            else {
                this.parentTrigger = parentTrigger;
                this.requiredTriggers = parseTriggers(parentChannel,parentTrigger,parentTrigger.channel.getRegisteredTriggers(),
                        data.getValOrDefault("required_triggers",new ArrayList<>()));
                this.linkTo = parseTriggers(parentChannel,parentTrigger,this.linkedChannel.getRegisteredTriggers(),
                        data.getValOrDefault("linked_triggers",new ArrayList<>()));
            }
            this.inheritTime = data.getValOrDefault("inherit_time",false);
            this.resume = data.getValOrDefault("resume_after_link",true);
            this.interuptedTime = new MutableLong();
            this.linkedTime = new MutableLong();
        }

        private List<Trigger> parseTriggers(String channel, Trigger parentTrigger, List<Trigger> triggers,
                                            List<String> potentialTriggers) {
            List<Trigger> ret = new ArrayList<>();
            for(String potential : potentialTriggers) {
                boolean found = false;
                for(Trigger trigger : triggers) {
                    if(trigger.getNameWithID().matches(potential)) {
                        ret.add(trigger);
                        found = true;
                        break;
                    }
                }
                if(!found) MusicTriggers.logExternally(Level.WARN, "Channel[{}] - Trigger with name {} under " +
                        "link for trigger {} was not recognized as a registered trigger and will be skipped",channel,
                        potential,parentTrigger.getNameWithID());
            }
            return ret;
        }

        public boolean isValid() {
            return Objects.nonNull(this.linkedChannel) && Objects.nonNull(this.parentTrigger) && !this.linkTo.isEmpty();
        }

        public boolean isActive(HashSet<Trigger> activeTriggers) {
            if(this.requiredTriggers.size()==1 && this.requiredTriggers.get(0)==this.parentTrigger) return true;
            HashSet<Trigger> otherActiveTriggers = new HashSet<>();
            for(Trigger trigger : activeTriggers)
                if(trigger!=this.parentTrigger)
                    otherActiveTriggers.add(trigger);
            return this.requiredTriggers.isEmpty() ? otherActiveTriggers.isEmpty() :
                    otherActiveTriggers.containsAll(this.requiredTriggers);
        }

        public boolean shouldLink(HashSet<Trigger> otherTriggers) {
            return otherTriggers.containsAll(this.linkTo);
        }

        public void activate() {
            this.resumeFrom = null;
            this.resumeTo = null;
        }

        public boolean inheritTime() {
            return this.inheritTime;
        }

        public Channel getParentChannel() {
            return this.parentChannel;
        }

        public Channel getLinkedChannel() {
            return this.linkedChannel;
        }

        public boolean areChannelsDifferent() {
            return this.parentChannel!=this.linkedChannel;
        }

        public long getTime(Channel channel) {
            return channel==this.parentChannel ? this.resume ? this.inheritTime ? this.linkedTime.longValue() :
                    this.interuptedTime.longValue() : 0L : this.linkedTime.longValue();
        }

        public void setTime(Channel channel, long time, @Nullable Audio audio) {
            if(channel==this.parentChannel) {
                this.interuptedTime.setValue(time);
                this.linkedTime.setValue(time);
                if(Objects.nonNull(audio)) this.resumeFrom = audio;
            } else if(channel==this.linkedChannel) {
                this.linkedTime.setValue(time);
                if(Objects.nonNull(audio)) this.resumeTo = audio;
            }
        }

        public Audio getResumedAudio(Channel channel) {
            return channel==this.parentChannel ? this.resumeFrom : channel==this.linkedChannel ? this.resumeTo : null;
        }
    }

    public static final class ReloadEvent extends Event {
        private ReloadEvent() {}
    }
}
