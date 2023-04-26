package mods.thecomputerizer.musictriggers.client.data;

import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.weather.event.AcidRain;
import corgitaco.betterweather.weather.event.Blizzard;
import corgitaco.betterweather.weather.event.Cloudy;
import corgitaco.betterweather.weather.event.Rain;
import corgitaco.enhancedcelestials.EnhancedCelestialsWorldData;
import corgitaco.enhancedcelestials.LunarContext;
import corgitaco.enhancedcelestials.lunarevent.BloodMoon;
import corgitaco.enhancedcelestials.lunarevent.BlueMoon;
import corgitaco.enhancedcelestials.lunarevent.HarvestMoon;
import corgitaco.enhancedcelestials.lunarevent.Moon;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.ClientEvents;
import mods.thecomputerizer.musictriggers.client.MusicPicker;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.WinGameScreen;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CClientStatusPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Level;
import org.orecruncher.lib.WorldUtils;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;

import java.util.*;
import java.util.function.BiFunction;

import static mods.thecomputerizer.musictriggers.MusicTriggers.stringBreaker;

@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "unused"})
public class Trigger {

    private static final HashSet<String> ALL_TRIGGERS = new HashSet<>();
    private static final HashSet<String> ACCEPTED_TRIGGERS = new HashSet<>();
    private static final HashSet<String> SERVER_TRIGGERS = new HashSet<>();
    private static final HashMap<String, String> DEFAULT_PARAMETER_MAP = new HashMap<>();
    private static final HashMap<String, List<String>> ACCEPTED_PARAMETERS = new HashMap<>();
    private static final HashMap<String, List<String>> REQUIRED_PARAMETERS = new HashMap<>();
    private static final HashMap<String, List<String>> CHOICE_REQUIRED_PARAMETERS = new HashMap<>();
    private static final HashMap<String, BiFunction<Trigger, ClientPlayerEntity, Boolean>> TRIGGER_CONDITIONS = new HashMap<>();

    public static void loadDefaultData() {
        clearData();
        loadDefaultParameters();
        loadDefaultTriggers();
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
        addParameter("priority","0");
        addParameter("identifier","not_set");
        addParameter("fade_in","0");
        addParameter("fade_out","0");
        addParameter("trigger_delay","0");
        addParameter("song_delay","0");
        addParameter("level",String.valueOf(Integer.MIN_VALUE));
        addParameter("persistence","0");
        addParameter("start_delay","0");
        addParameter("time_bundle","any");
        addParameter("start_hour","0");
        addParameter("end_hour","0");
        addParameter("lowest_day_number","0");
        addParameter("highest_day_number",String.valueOf(Integer.MAX_VALUE));
        addParameter("zone_min_x",String.valueOf(Integer.MIN_VALUE));
        addParameter("zone_min_y",String.valueOf(Integer.MIN_VALUE));
        addParameter("zone_min_z",String.valueOf(Integer.MIN_VALUE));
        addParameter("zone_max_x",String.valueOf(Integer.MAX_VALUE));
        addParameter("zone_max_y",String.valueOf(Integer.MAX_VALUE));
        addParameter("zone_max_z",String.valueOf(Integer.MAX_VALUE));
        addParameter("resource_name","any");
        addParameter("start_toggled","true");
        addParameter("not","false");
        addParameter("passive_persistence","false");
        addParameter("toggle_inactive_playable","false");
        addParameter("detection_range","16");
        addParameter("mob_targeting","true");
        addParameter("health","100");
        addParameter("horde_targeting_percentage","50");
        addParameter("horde_health_percentage","50");
        addParameter("mob_nbt","any");
        addParameter("infernal","any");
        addParameter("champion","any");
        addParameter("victory_id","not_set");
        addParameter("victory_timeout","20");
        addParameter("victory_percentage","100");
        addParameter("moon_phase","0");
        addParameter("light_type","any");
        addParameter("is_whitelist","true");
        addParameter("biome_category","any");
        addParameter("rain_type","any");
        addParameter("biome_temperature",String.valueOf(Float.MIN_VALUE));
        addParameter("check_lower_temp","false");
        addParameter("biome_rainfall",String.valueOf(Float.MIN_VALUE));
        addParameter("check_higher_rainfall","true");
        addParameter("check_for_sky","true");
        addParameter("check_above_level","false");
    }

    //accepted parameter set
    public static List<String> makeParameterSet(boolean isHolder, String ... parameters) {
        return makeParameterSet(true,isHolder,parameters);
    }

    public static List<String> makeParameterSet(boolean isAccepted, boolean isHolder, String ... parameters) {
        List<String> acceptedParameters = isAccepted ?
                new ArrayList<>(Arrays.asList("priority","fade_in","fade_out","trigger_delay", "song_delay","start_toggled", "not",
                        "persistence","start_delay","passive_persistence","toggle_inactive_playable")) : new ArrayList<>();
        if(isHolder) acceptedParameters.add("identifier");
        Collections.addAll(acceptedParameters, parameters);
        return acceptedParameters;
    }

    private static void loadDefaultTriggers() {
        addTrigger("loading",false,makeParameterSet(false),(trigger,player) -> false);
        addTrigger("menu",false,makeParameterSet(false),(trigger,player) -> false);
        addTrigger("generic",false,makeParameterSet(false),(trigger,player) -> false);
        addTrigger("difficulty",false,makeParameterSet(true,"level"),
                Arrays.asList("identifier","level"),new ArrayList<>(),(trigger,player) -> {
                    Minecraft mc = Minecraft.getInstance();
                    return trigger.difficultyHelper(mc.level.getDifficulty(), mc.level.getLevelData().isHardcore());
                });
        addTrigger("time",false,makeParameterSet(true,"time_bundle",
                        "start_hour","end_hour","moon_phase", "lowest_day_number","highest_day_number"),
                Collections.singletonList("identifier"),Arrays.asList("time_bundle","start_hour"),(trigger,player) -> {
                    World world = player.level;
                    double time = (double) world.dayTime() / 24000.0;
                    if (time > 1) time = time - (long) time;
                    boolean pass;
                    String bundle = trigger.getParameter("time_bundle");
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
                    return pass && trigger.timeTriggerExtras(world.getGameTime(),world.getMoonPhase() + 1);
                });
        addTrigger("light",false,makeParameterSet(true,"level","light_type"),
                Arrays.asList("identifier","level"),new ArrayList<>(),(trigger,player) ->
                        trigger.averageLight(trigger.roundedPos(player),trigger.getParameter("light_type"), player.level)
                                <= trigger.getParameterInt("level"));
        addTrigger("height",false,makeParameterSet(true,"level","check_for_sky","check_above_level"),
                Arrays.asList("identifier","level"),new ArrayList<>(),(trigger,player) ->
                        trigger.handleHeight((int) player.getY(),player.level.canSeeSky(trigger.roundedPos(player))));
        addTrigger("elytra",false,makeParameterSet(false),
                (trigger,player) -> player.getFallFlyingTicks() > 0);
        addTrigger("fishing",false,makeParameterSet(false),
                (trigger,player) -> Objects.nonNull(player.fishing) && player.fishing.isInWaterOrBubble());
        addTrigger("raining",false,makeParameterSet(false),
                (trigger,player) -> player.level.isRaining());
        addTrigger("snowing",true,makeParameterSet(false),
                (trigger,player) -> ChannelManager.getChannel(trigger.channel).getSyncStatus().isTriggerActive(trigger));
        addTrigger("storming",false,makeParameterSet(false),
                (trigger,player) -> player.level.isThundering());
        addTrigger("lowhp",false,makeParameterSet(false,"level"),
                (trigger,player) -> trigger.handleHP(player.getHealth(), player.getMaxHealth()));
        addTrigger("dead",false,makeParameterSet(false),
                (trigger,player) -> player.getHealth() <= 0f || player.isDeadOrDying());
        addTrigger("spectator",false,makeParameterSet(false),(trigger,player) -> player.isSpectator());
        addTrigger("creative",false,makeParameterSet(false),(trigger,player) -> player.isCreative());
        addTrigger("riding",false,makeParameterSet(true,"resource_name"),
                Collections.singletonList("identifier"),new ArrayList<>(),
                (trigger,player) -> trigger.checkRiding(trigger.getResource(),player));
        addTrigger("underwater",false,makeParameterSet(false),(trigger,player) ->
                (player.level.getBlockState(trigger.roundedPos(player)).getMaterial() == Material.WATER ||
                        player.level.getBlockState(trigger.roundedPos(player)).getMaterial() == Material.WATER_PLANT ||
                        player.level.getBlockState(trigger.roundedPos(player)).getMaterial() == Material.REPLACEABLE_WATER_PLANT)
                        && (player.level.getBlockState(trigger.roundedPos(player).above()).getMaterial() == Material.WATER ||
                        player.level.getBlockState(trigger.roundedPos(player).above()).getMaterial() == Material.WATER_PLANT ||
                        player.level.getBlockState(trigger.roundedPos(player).above()).getMaterial() == Material.REPLACEABLE_WATER_PLANT));
        addTrigger("pet",false,makeParameterSet(false,"detection_range"),(trigger,player) -> {
            boolean pass = false;
            int range = trigger.getParameterInt("detection_range");
            for (LivingEntity ent : player.level.getEntitiesOfClass(LivingEntity.class,
                    new AxisAlignedBB(player.getX() - range, player.getY() - ((float) range /2), player.getZ() - range,
                            player.getX() + range, player.getY() + ((float) range /2), player.getZ() + range))) {
                if (ent instanceof TameableEntity && ent.serializeNBT().getString("Owner").matches(player.getStringUUID()))
                    pass = true;
            }
            return pass;
        });
        addTrigger("drowning",false,makeParameterSet(false,"level"),
                (trigger,player) -> player.getAirSupply() < trigger.getParameterInt("level"));
        addTrigger("pvp",true,makeParameterSet(true), Collections.singletonList("identifier"),
                new ArrayList<>(),(trigger,player) -> ChannelManager.getChannel(trigger.channel).getSyncStatus().isTriggerActive(trigger));
        addTrigger("home",true,makeParameterSet(false,"detection_range"),
                (trigger,player) -> ChannelManager.getChannel(trigger.channel).getSyncStatus().isTriggerActive(trigger));
        addTrigger("dimension",false,makeParameterSet(true,"resource_name"),
                Arrays.asList("identifier","resource_name"),new ArrayList<>(),
                (trigger,player) -> trigger.checkResourceList(player.level.dimension().location().toString(),trigger.getResource(), false));
        addTrigger("biome",true,makeParameterSet(true,"resource_name","biome_category",
                        "rain_type","biome_temperature","check_lower_temp","biome_rainfall","check_higher_rainfall"),
                Collections.singletonList("identifier"),Arrays.asList("resource_name","biome_category","rain_type",
                        "biome_temperature","biome_rainfall"),
                (trigger, player) -> ChannelManager.getChannel(trigger.channel).getSyncStatus().isTriggerActive(trigger));
        addTrigger("structure",true,makeParameterSet(true,"resource_name"),
                Arrays.asList("identifier","resource_name"),new ArrayList<>(),
                (trigger,player) -> ChannelManager.getChannel(trigger.channel).getSyncStatus().isTriggerActive(trigger));
        addTrigger("mob",true,makeParameterSet(true,"resource_name","level",
                        "detection_range","mob_targeting","health","horde_targeting_percentage", "horde_health_percentage",
                        "mob_nbt","infernal","champion","victory_id","victory_percentage"),
                Arrays.asList("identifier","level","resource_name"),new ArrayList<>(),
                (trigger,player) -> ChannelManager.getChannel(trigger.channel).getSyncStatus().isTriggerActive(trigger));
        addTrigger("zones",false,makeParameterSet(true,"start_delay","zone_min_x",
                        "zone_max_x","zone_min_y","zone_max_y","zone_min_z","zone_max_z"),
                Collections.singletonList("identifier"),Arrays.asList("start_delay","zone_min_x","zone_max_x","zone_min_y",
                        "zone_max_y","zone_min_z","zone_max_z"),(trigger, player) -> {
                    BlockPos pos = player.blockPosition();
                    return trigger.zoneHelper(pos.getX(),pos.getY(),pos.getZ());
                });
        addTrigger("effect",false,makeParameterSet(true,"resource_name"),
                Arrays.asList("identifier","resource_name"),new ArrayList<>(),(trigger,player) -> {
                    boolean pass = false;
                    MusicPicker.EFFECT_LIST.clear();
                    for (EffectInstance p : player.getActiveEffects()) {
                        if(Objects.nonNull(p.getEffect().getRegistryName())) {
                            MusicPicker.EFFECT_LIST.add(p.getEffect().getRegistryName().toString());
                            if (trigger.checkResourceList(p.getEffect().getRegistryName().toString(), trigger.getResource(), false))
                                pass = true;
                        }
                    }
                    return pass;
                });
        addTrigger("victory",true,makeParameterSet(true,"victory_timeout"),
                Arrays.asList("identifier","persistence"),new ArrayList<>(),
                (trigger,player) -> ChannelManager.getChannel(trigger.channel).getSyncStatus().isTriggerActive(trigger));
        addTrigger("gui",false,makeParameterSet(true,"resource_name"),
                Arrays.asList("identifier","resource_name"),new ArrayList<>(),(trigger,player) -> {
                    Minecraft mc = Minecraft.getInstance();
                    String resource = trigger.getResource();
                    return (Objects.nonNull(mc.screen) &&
                            (trigger.checkResourceList(mc.screen.getClass().getName(),resource,false)) ||
                            (resource.matches("CREDITS") && mc.screen instanceof WinGameScreen));
                });
        addTrigger("advancement",false,makeParameterSet(true,"resource_name"),
                Arrays.asList("identifier","resource_name","persistence"),new ArrayList<>(),(trigger,player) -> {
                    String resource = trigger.getResource();
                    boolean pass = (ClientEvents.GAINED_NEW_ADVANCEMENT && trigger.checkResourceList(ClientEvents.LAST_ADVANCEMENT,resource,false)) ||
                            resource.matches("any");
                    if(pass) ClientEvents.GAINED_NEW_ADVANCEMENT = false;
                    return pass;
                });
        addTrigger("statistic",false,makeParameterSet(true,"resource_name","level"),
                Arrays.asList("identifier","resource_name"),new ArrayList<>(),(trigger,player) ->
                        trigger.checkStat(trigger.getResource(),trigger.getParameterInt("level")));
        addTrigger("command",false,makeParameterSet(true),
                Arrays.asList("identifier","persistence"),new ArrayList<>(),(trigger,player) -> {
                    boolean pass = ClientEvents.commandHelper(trigger);
                    if(pass) ClientEvents.commandFinish(trigger);
                    return pass;
                });
        addTrigger("raid",true,makeParameterSet(true,"level"),
                Collections.singletonList("identifier"),new ArrayList<>(),
                (trigger, player) -> ChannelManager.getChannel(trigger.channel).getSyncStatus().isTriggerActive(trigger));
        addTrigger("gamestage",false,Collections.singletonList("gamestages"),
                makeParameterSet(true,"resource_name","is_whitelist"),
                Arrays.asList("identifier","resource_name"),new ArrayList<>(),
                (trigger,player) -> trigger.whitelistHelper(GameStageHelper.hasAnyOf(
                        player,trigger.parseGamestageList(trigger.getResource()))));
        addTrigger("bloodmoon",false, Collections.singletonList("enhancedcelestials"),makeParameterSet(false),
                (trigger,player) -> {
                    LunarContext lunarContext = ((EnhancedCelestialsWorldData) player.level).getLunarContext();
                    return Objects.nonNull(lunarContext) && lunarContext.getCurrentEvent() instanceof BloodMoon;
                });
        addTrigger("harvestmoon",false, Collections.singletonList("enhancedcelestials"),makeParameterSet(false),
                (trigger,player) -> {
                    LunarContext lunarContext = ((EnhancedCelestialsWorldData) player.level).getLunarContext();
                    return Objects.nonNull(lunarContext) && lunarContext.getCurrentEvent() instanceof HarvestMoon;
                });
        addTrigger("bluemoon",false, Collections.singletonList("enhancedcelestials"),makeParameterSet(false),
                (trigger,player) -> {
                    LunarContext lunarContext = ((EnhancedCelestialsWorldData) player.level).getLunarContext();
                    return Objects.nonNull(lunarContext) && lunarContext.getCurrentEvent() instanceof BlueMoon;
                });
        addTrigger("moon",false, Collections.singletonList("enhancedcelestials"),
                makeParameterSet(true,"resource_name"),
                Arrays.asList("identifier","resource_name"),new ArrayList<>(),
                (trigger,player) -> {
                    LunarContext lunarContext = ((EnhancedCelestialsWorldData) player.level).getLunarContext();
                    if(Objects.nonNull(lunarContext) && lunarContext.getCurrentEvent() instanceof Moon)
                        return lunarContext.getCurrentEvent().getKey().contains(trigger.getResource());
                    return false;
                });
        addTrigger("rainintensity",false,Collections.singletonList("dsurround"),
                makeParameterSet(true,"level"), Collections.singletonList("identifier"),new ArrayList<>(),
                (trigger,player) -> WorldUtils.getRainStrength(player.level, 1F)>(((float)trigger.getParameterInt("level"))/100f));
        addTrigger("acidrain",false, Collections.singletonList("betterweather"),makeParameterSet(false),
                (trigger,player) -> {
                    BetterWeatherWorldData weatherdata = (BetterWeatherWorldData) player.level;
                    return Objects.nonNull(weatherdata.getWeatherEventContext()) &&
                            weatherdata.getWeatherEventContext().getCurrentEvent() instanceof AcidRain;
                });
        addTrigger("blizzard",false, Collections.singletonList("betterweather"),makeParameterSet(false),
                (trigger,player) -> {
                    BetterWeatherWorldData weatherdata = (BetterWeatherWorldData) player.level;
                    return Objects.nonNull(weatherdata.getWeatherEventContext()) &&
                            weatherdata.getWeatherEventContext().getCurrentEvent() instanceof Blizzard;
                });
        addTrigger("cloudy",false, Collections.singletonList("betterweather"),makeParameterSet(false),
                (trigger,player) -> {
                    BetterWeatherWorldData weatherdata = (BetterWeatherWorldData) player.level;
                    return Objects.nonNull(weatherdata.getWeatherEventContext()) &&
                            weatherdata.getWeatherEventContext().getCurrentEvent() instanceof Cloudy;
                });
        addTrigger("lightrain",false, Collections.singletonList("betterweather"),makeParameterSet(false),
                (trigger,player) -> {
                    BetterWeatherWorldData weatherdata = (BetterWeatherWorldData) player.level;
                    return Objects.nonNull(weatherdata.getWeatherEventContext()) &&
                            weatherdata.getWeatherEventContext().getCurrentEvent() instanceof Rain &&
                            !(weatherdata.getWeatherEventContext().getCurrentEvent() instanceof AcidRain);
                });
        addTrigger("season",false,Collections.singletonList("sereneseasons"),
                makeParameterSet(true,"level"),Arrays.asList("identifier","level"), new ArrayList<>(),
                (trigger,player) -> trigger.seasonHelper(SeasonHelper.getSeasonState(player.level).getSeason()));
    }

    public static void addParameter(String name, String defaultValue) throws IllegalArgumentException {
        if(DEFAULT_PARAMETER_MAP.containsKey(name)) throw new IllegalArgumentException("Parameter with name "+name+"already exists!");
        DEFAULT_PARAMETER_MAP.put(name,defaultValue);
    }

    //vanilla + no required parameters
    public static void addTrigger(String name, boolean isServerSide, List<String> acceptedParameters,
                                  BiFunction<Trigger, ClientPlayerEntity, Boolean> activationFunction) throws IllegalArgumentException {
        addTrigger(name,isServerSide,new ArrayList<>(),acceptedParameters,new ArrayList<>(),new ArrayList<>(),activationFunction);
    }

    //no required parameters
    public static void addTrigger(String name, boolean isServerSide,List<String> requiredMods,List<String> acceptedParameters,
                                  BiFunction<Trigger, ClientPlayerEntity, Boolean> activationFunction) throws IllegalArgumentException {
        addTrigger(name,isServerSide,requiredMods,acceptedParameters,new ArrayList<>(),new ArrayList<>(),activationFunction);
    }

    //vanilla trigger
    public static void addTrigger(String name, boolean isServerSide, List<String> acceptedParameters,
                                  List<String> requiredParameters, List<String> choiceRequiredParameters,
                                  BiFunction<Trigger, ClientPlayerEntity, Boolean> activationFunction) throws IllegalArgumentException {
        addTrigger(name,isServerSide,new ArrayList<>(),acceptedParameters,requiredParameters,choiceRequiredParameters,activationFunction);
    }

    public static void addTrigger(String name, boolean isServerSide, List<String> requiredMods,
                                  List<String> acceptedParameters, List<String> requiredParameters, List<String> choiceRequiredParameters,
                                  BiFunction<Trigger, ClientPlayerEntity, Boolean> activationFunction) throws IllegalArgumentException {
        if (ALL_TRIGGERS.contains(name))
            throw new IllegalArgumentException("Trigger with name " + name + "already exists!");
        ALL_TRIGGERS.add(name);
        if(!isServerSide || !ConfigRegistry.CLIENT_SIDE_ONLY) {
            if (requiredMods.isEmpty()) ACCEPTED_TRIGGERS.add(name);
            else {
                for (String modid : requiredMods) {
                    if (ModList.get().isLoaded(modid)) {
                        ACCEPTED_TRIGGERS.add(name);
                        break;
                    }
                }
            }
        }
        if (ACCEPTED_TRIGGERS.contains(name)) {
            if(isServerSide)
                SERVER_TRIGGERS.add(name);
            ACCEPTED_PARAMETERS.put(name, acceptedParameters);
            REQUIRED_PARAMETERS.put(name, requiredParameters);
            CHOICE_REQUIRED_PARAMETERS.put(name, choiceRequiredParameters);
            TRIGGER_CONDITIONS.put(name, activationFunction);
        }
    }

    public static HashSet<String> getAcceptedTriggers() {
        return ACCEPTED_TRIGGERS;
    }

    public static boolean isServerSide(String name) {
        return SERVER_TRIGGERS.contains(name);
    }

    public static List<String> getAcceptedParameters(String trigger) {
        return ACCEPTED_PARAMETERS.get(trigger);
    }

    public static boolean isParameterAccepted(String trigger, String parameter) {
        return ACCEPTED_PARAMETERS.containsKey(trigger) && ACCEPTED_PARAMETERS.get(trigger).contains(parameter);
    }

    public static String getDefaultParameter(String parameter) {
        return DEFAULT_PARAMETER_MAP.get(parameter);
    }

    public static boolean isLoaded(String trigger) {
        return ACCEPTED_TRIGGERS.contains(trigger);
    }

    private final String channel;
    private final String name;
    private final HashMap<String, String> parameters;
    private boolean isToggled;

    public Trigger(String name, String channel) {
        this.name = name;
        this.channel = channel;
        this.parameters = buildDefaultParameters(name);
        this.isToggled = false;
    }

    private HashMap<String, String> buildDefaultParameters(String trigger) {
        HashMap<String, String> ret = new HashMap<>();
        for(String parameter : ACCEPTED_PARAMETERS.get(trigger)) ret.put(parameter,DEFAULT_PARAMETER_MAP.get(parameter));
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
        if(hasID()) return this.name+"-"+getParameter("identifier");
        return getName();
    }

    public void setParameter(String parameter, String value) {
        if(parameter.matches("id")) parameter = "identifier";
        this.parameters.put(parameter, value);
    }

    private boolean isDefault(String parameter) {
        return this.parameters.get(parameter).matches(DEFAULT_PARAMETER_MAP.get(parameter));
    }

    public boolean hasID() {
        return isParameterAccepted(this.name,"identifier");
    }

    public String getParameter(String parameter) {
        return this.parameters.get(parameter);
    }

    public boolean getParameterBool(String parameter) {
        return Boolean.parseBoolean(getParameter(parameter));
    }

    public int getParameterInt(String parameter) {
        return MusicTriggers.randomInt(parameter,getParameter(parameter),
                Integer.parseInt(DEFAULT_PARAMETER_MAP.get(parameter)));
    }

    public float getParameterFloat(String parameter) {
        return MusicTriggers.randomFloat(parameter,getParameter(parameter),
                Float.parseFloat(DEFAULT_PARAMETER_MAP.get(parameter)));
    }

    public boolean runActivationFunction(ClientPlayerEntity player) {
        return TRIGGER_CONDITIONS.containsKey(getName()) && isActive(TRIGGER_CONDITIONS.get(getName()).apply(this,player));
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

    public String getResource() {
        return getParameter("resource_name");
    }

    public boolean zoneHelper(int x, int y, int z) {
        return x>getParameterInt("zone_min_x") && x<getParameterInt("zone_max_x") &&
                y>getParameterInt("zone_min_y") && y<getParameterInt("zone_max_y") &&
                z>getParameterInt("zone_min_z") && z<getParameterInt("zone_max_z");
    }

    public boolean difficultyHelper(Difficulty difficulty, boolean hardcore) {
        int diff = getParameterInt("level");
        return (diff==4 && hardcore) || (diff==3 && difficulty==Difficulty.HARD) ||
                (diff==2 && difficulty==Difficulty.NORMAL) || (diff==1 && difficulty==Difficulty.EASY) ||
                (diff==0 && difficulty==Difficulty.PEACEFUL);
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

    public BlockPos roundedPos(ClientPlayerEntity p) {
        return new BlockPos((Math.round(p.getX() * 2) / 2.0), (Math.round(p.getY() * 2) / 2.0), (Math.round(p.getZ() * 2) / 2.0));
    }


    public double averageLight(BlockPos p, String lightType, World world) {
        if(lightType.matches("block") || lightType.matches("sky"))
            return lightType.matches("block") ?
                    world.getBrightness(LightType.BLOCK, p) : world.getBrightness(LightType.SKY, p);
        return world.getRawBrightness(p, 0);
    }

    public boolean checkResourceList(String type, String resourceList, boolean match) {
        for(String resource : stringBreaker(resourceList,";")) {
            if(match && type.matches(resource)) return true;
            else if(!match && type.contains(resource)) return true;
        }
        return false;
    }

    public boolean checkStatResourceList(String type, String resourceList, String stat) {
        for(String resource : stringBreaker(resourceList,";")) {
            if(resource.contains(stat) && type.contains(resource.substring(stat.length()+1))) return true;
        }
        return false;
    }

    public List<String> parseGamestageList(String resourceList) {
        return Arrays.asList(resourceList.split(";"));
    }

    public boolean checkRiding(String resource, ClientPlayerEntity player) {
        if(Objects.nonNull(player.getVehicle())) return false;
        else if(resource.matches("any")) return true;
        else if(checkResourceList(Objects.requireNonNull(player.getVehicle()).getName().getString(),resource,true)) return true;
        else if (Objects.isNull(ForgeRegistries.ENTITIES.getKey(player.getVehicle().getType()))) return false;
        return checkResourceList(Objects.requireNonNull(ForgeRegistries.ENTITIES.getKey(player.getVehicle().getType())).toString(),resource,false);
    }

    public boolean checkStat(String statName, int level) {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player!=null && mc.getConnection()!=null) {
            Objects.requireNonNull(mc.getConnection()).send(new CClientStatusPacket(CClientStatusPacket.State.REQUEST_STATS));
            for (Stat<ResourceLocation> stat : Stats.CUSTOM) {
                if (checkResourceList(stat.getValue().toString(), statName, false) &&
                        mc.player.getStats().getValue(stat) > level)
                    return true;
            }
            if (statName.contains("mined")) {
                for (Stat<Block> stat : Stats.BLOCK_MINED) {
                    if (stat.getValue().getRegistryName() != null &&
                            checkStatResourceList(stat.getValue().getRegistryName().toString(), statName, "mined") &&
                            mc.player.getStats().getValue(Stats.BLOCK_MINED.get(stat.getValue())) > level)
                        return true;
                }
            }
            if (statName.contains("crafted")) {
                for (Stat<Item> stat : Stats.ITEM_CRAFTED) {
                    if (stat.getValue().getRegistryName() != null &&
                            checkStatResourceList(stat.getValue().getRegistryName().toString(), statName, "crafted") &&
                            mc.player.getStats().getValue(Stats.ITEM_CRAFTED.get(stat.getValue())) > level)
                        return true;
                }
            }
            if (statName.contains("used")) {
                for (Stat<Item> stat : Stats.ITEM_USED) {
                    if (stat.getValue().getRegistryName() != null &&
                            checkStatResourceList(stat.getValue().getRegistryName().toString(), statName, "used") &&
                            mc.player.getStats().getValue(Stats.ITEM_USED.get(stat.getValue())) > level)
                        return true;
                }
            }
            if (statName.contains("broken")) {
                for (Stat<Item> stat : Stats.ITEM_BROKEN) {
                    if (stat.getValue().getRegistryName() != null &&
                            checkStatResourceList(stat.getValue().getRegistryName().toString(), statName, "broken") &&
                            mc.player.getStats().getValue(Stats.ITEM_BROKEN.get(stat.getValue())) > level)
                        return true;
                }
            }
            if (statName.contains("picked_up")) {
                for (Stat<Item> stat : Stats.ITEM_PICKED_UP) {
                    if (stat.getValue().getRegistryName() != null &&
                            checkStatResourceList(stat.getValue().getRegistryName().toString(), statName, "picked_up") &&
                            mc.player.getStats().getValue(Stats.ITEM_PICKED_UP.get(stat.getValue())) > level)
                        return true;
                }
            }
            if (statName.contains("dropped")) {
                for (Stat<Item> stat : Stats.ITEM_DROPPED) {
                    if (stat.getValue().getRegistryName() != null &&
                            checkStatResourceList(stat.getValue().getRegistryName().toString(), statName, "dropped") &&
                            mc.player.getStats().getValue(Stats.ITEM_DROPPED.get(stat.getValue())) > level)
                        return true;
                }
            }
            if (statName.contains("killed")) {
                for (Stat<EntityType<?>> stat : Stats.ENTITY_KILLED) {
                    if (stat.getValue().getRegistryName() != null &&
                            checkStatResourceList(stat.getValue().getRegistryName().toString(), statName, "killed") &&
                            mc.player.getStats().getValue(Stats.ENTITY_KILLED.get(stat.getValue())) > level)
                        return true;
                }
            }
            if (statName.contains("killed_by")) {
                for (Stat<EntityType<?>> stat : Stats.ENTITY_KILLED_BY) {
                    if (stat.getValue().getRegistryName() != null &&
                            checkStatResourceList(stat.getValue().getRegistryName().toString(), statName, "killed_by") &&
                            mc.player.getStats().getValue(Stats.ENTITY_KILLED_BY.get(stat.getValue())) > level)
                        return true;
                }
            }
        }
        return false;
    }

    public boolean isToggled() {
        return this.isToggled;
    }

    public void setToggle(boolean state) {
        this.isToggled = state;
    }
}
