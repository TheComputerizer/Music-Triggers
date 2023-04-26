package mods.thecomputerizer.musictriggers.client.data;

import CoroUtil.util.Vec3;
import de.ellpeck.nyx.capabilities.NyxWorld;
import de.ellpeck.nyx.lunarevents.BloodMoon;
import de.ellpeck.nyx.lunarevents.HarvestMoon;
import de.ellpeck.nyx.lunarevents.StarShower;
import lumien.bloodmoon.Bloodmoon;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.ClientEvents;
import mods.thecomputerizer.musictriggers.client.MusicPicker;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.Level;
import org.orecruncher.dsurround.client.weather.Weather;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;
import weather2.api.WeatherDataHelper;

import java.util.*;
import java.util.function.BiFunction;

import static mods.thecomputerizer.musictriggers.MusicTriggers.stringBreaker;

@SuppressWarnings({"BooleanMethodIsAlwaysInverted"})
public class Trigger {

    private static final HashSet<String> ALL_TRIGGERS = new HashSet<>();
    private static final HashSet<String> ACCEPTED_TRIGGERS = new HashSet<>();
    private static final HashSet<String> SERVER_TRIGGERS = new HashSet<>();
    private static final HashMap<String, String> DEFAULT_PARAMETER_MAP = new HashMap<>();
    private static final HashMap<String, List<String>> ACCEPTED_PARAMETERS = new HashMap<>();
    private static final HashMap<String, List<String>> REQUIRED_PARAMETERS = new HashMap<>();
    private static final HashMap<String, List<String>> CHOICE_REQUIRED_PARAMETERS = new HashMap<>();
    private static final HashMap<String, BiFunction<Trigger, EntityPlayerSP, Boolean>> TRIGGER_CONDITIONS = new HashMap<>();

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
            Minecraft mc = Minecraft.getMinecraft();
            return trigger.difficultyHelper(mc.world.getDifficulty(), mc.world.getWorldInfo().isHardcoreModeEnabled());
        });
        addTrigger("time",false,makeParameterSet(true,"time_bundle",
                        "start_hour","end_hour","moon_phase", "lowest_day_number","highest_day_number"),
                Collections.singletonList("identifier"),Arrays.asList("time_bundle","start_hour"),(trigger,player) -> {
            World world = player.getEntityWorld();
            double time = (double) world.getWorldTime() / 24000.0;
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
            return pass && trigger.timeTriggerExtras(world.getTotalWorldTime(),world.getMoonPhase() + 1);
        });
        addTrigger("light",false,makeParameterSet(true,"level","light_type"),
                Arrays.asList("identifier","level"),new ArrayList<>(),(trigger,player) ->
                trigger.averageLight(trigger.roundedPos(player),trigger.getParameter("light_type"), player.getEntityWorld())
                        <= trigger.getParameterInt("level"));
        addTrigger("height",false,makeParameterSet(true,"level","check_for_sky","check_above_level"),
                Arrays.asList("identifier","level"),new ArrayList<>(),(trigger,player) ->
                trigger.handleHeight((int) player.posY,player.getEntityWorld().canSeeSky(trigger.roundedPos(player))));
        addTrigger("elytra",false,makeParameterSet(false),
                (trigger,player) -> player.getTicksElytraFlying() > 0);
        addTrigger("fishing",false,makeParameterSet(false),
                (trigger,player) -> player.fishEntity != null && player.fishEntity.isOverWater());
        addTrigger("raining",false,makeParameterSet(false),
                (trigger,player) -> player.getEntityWorld().isRaining());
        addTrigger("snowing",false,makeParameterSet(false),(trigger,player) -> {
            BlockPos pos = trigger.roundedPos(player);
            return player.getEntityWorld().isRaining() &&
                    player.getEntityWorld().getBiomeProvider().getTemperatureAtHeight(
                            player.getEntityWorld().getBiome(pos).getTemperature(pos),
                            player.getEntityWorld().getPrecipitationHeight(pos).getY()) < 0.15f;
        });
        addTrigger("storming",false,makeParameterSet(false),
                (trigger,player) -> player.getEntityWorld().isThundering());
        addTrigger("lowhp",false,makeParameterSet(false,"level"),
                (trigger,player) -> trigger.handleHP(player.getHealth(), player.getMaxHealth()));
        addTrigger("dead",false,makeParameterSet(false),
                (trigger,player) -> player.getHealth() <= 0f || player.isDead);
        addTrigger("spectator",false,makeParameterSet(false),(trigger,player) -> player.isSpectator());
        addTrigger("creative",false,makeParameterSet(false),(trigger,player) -> player.isCreative());
        addTrigger("riding",false,makeParameterSet(true,"resource_name"),
                Collections.singletonList("identifier"),new ArrayList<>(),
                (trigger,player) -> trigger.checkRiding(trigger.getResource(),player));
        addTrigger("underwater",false,makeParameterSet(false),(trigger,player) ->
                player.getEntityWorld().getBlockState(trigger.roundedPos(player)).getMaterial() == Material.WATER &&
                        player.getEntityWorld().getBlockState(trigger.roundedPos(player).up()).getMaterial() == Material.WATER);
        addTrigger("pet",false,makeParameterSet(false,"detection_range"),(trigger,player) -> {
            boolean pass = false;
            int range = trigger.getParameterInt("detection_range");
            for (EntityLiving ent : player.getEntityWorld().getEntitiesWithinAABB(EntityLiving.class,
                    new AxisAlignedBB(player.posX - range, player.posY - ((float) range /2), player.posZ - range,
                            player.posX + range, player.posY + ((float) range /2), player.posZ + range))) {
                if (ent instanceof EntityTameable && ent.serializeNBT().getString("Owner").matches(player.getName()))
                    pass = true;
            }
            return pass;
        });
        addTrigger("drowning",false,makeParameterSet(false,"level"),
                (trigger,player) -> player.getAir() < trigger.getParameterInt("level"));
        addTrigger("pvp",true,makeParameterSet(true), Collections.singletonList("identifier"),
                new ArrayList<>(),(trigger,player) -> ChannelManager.getChannel(trigger.channel).getSyncStatus().isTriggerActive(trigger));
        addTrigger("home",true,makeParameterSet(false,"detection_range"),
                (trigger,player) -> ChannelManager.getChannel(trigger.channel).getSyncStatus().isTriggerActive(trigger));
        addTrigger("dimension",false,makeParameterSet(true,"resource_name"),
                Arrays.asList("identifier","resource_name"),new ArrayList<>(),
                (trigger,player) -> trigger.checkDimensionList(player.dimension,trigger.getResource()));
        addTrigger("biome",false,makeParameterSet(true,"resource_name","biome_category",
                        "rain_type","biome_temperature","check_lower_temp","biome_rainfall","check_higher_rainfall"),
                Collections.singletonList("identifier"),Arrays.asList("resource_name","biome_category","rain_type",
                        "biome_temperature","biome_rainfall"),(trigger, player) ->
                trigger.checkBiome(player.getEntityWorld().getBiome(trigger.roundedPos(player)), trigger.getResource(),
                        trigger.getParameter("biome_category"),trigger.getParameter("rain_type"),
                        trigger.getParameterFloat("biome_temperature"),trigger.getParameterBool("check_lower_temp"),
                        trigger.getParameterFloat("biome_rainfall"),trigger.getParameterBool("check_higher_rainfall")));
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
            BlockPos pos = player.getPosition();
            return trigger.zoneHelper(pos.getX(),pos.getY(),pos.getZ());
        });
        addTrigger("effect",false,makeParameterSet(true,"resource_name"),
                Arrays.asList("identifier","resource_name"),new ArrayList<>(),(trigger,player) -> {
            boolean pass = false;
            MusicPicker.EFFECT_LIST.clear();
            for (PotionEffect p : player.getActivePotionEffects()) {
                MusicPicker.EFFECT_LIST.add(p.getEffectName());
                if (trigger.checkResourceList(p.getEffectName(),trigger.getResource(),false))
                    pass = true;
            }
            return pass;
        });
        addTrigger("victory",true,makeParameterSet(true,"victory_timeout"),
                Arrays.asList("identifier","persistence"),new ArrayList<>(),
                (trigger,player) -> ChannelManager.getChannel(trigger.channel).getSyncStatus().isTriggerActive(trigger));
        addTrigger("gui",false,makeParameterSet(true,"resource_name"),
                Arrays.asList("identifier","resource_name"),new ArrayList<>(),(trigger,player) -> {
            Minecraft mc = Minecraft.getMinecraft();
            String resource = trigger.getResource();
            return (Objects.nonNull(mc.currentScreen) &&
                    (trigger.checkResourceList(mc.currentScreen.getClass().getName(),resource,false)) ||
                    (resource.matches("CREDITS") && mc.currentScreen instanceof GuiWinGame));
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
                trigger.checkStat(trigger.getResource(),trigger.getParameterInt("level"),player));
        addTrigger("command",false,makeParameterSet(true),
                Arrays.asList("identifier","persistence"),new ArrayList<>(),(trigger,player) -> {
            boolean pass = ClientEvents.commandHelper(trigger);
            if(pass) ClientEvents.commandFinish(trigger);
            return pass;
        });
        addTrigger("gamestage",false,Collections.singletonList("gamestages"),
                makeParameterSet(true,"resource_name","is_whitelist"),
                Arrays.asList("identifier","resource_name","is_whitelist"),new ArrayList<>(),
                (trigger,player) -> trigger.whitelistHelper(GameStageHelper.clientHasAnyOf(
                        player,trigger.parseGamestageList(trigger.getResource()))));
        addTrigger("bloodmoon",false,Arrays.asList("bloodmoon","nyx"),makeParameterSet(false),
                (trigger,player) -> (Loader.isModLoaded("bloodmoon") &&
                Bloodmoon.proxy.isBloodmoon()) || (Loader.isModLoaded("nyx") &&
                NyxWorld.get(player.getEntityWorld()).currentEvent instanceof BloodMoon));
        addTrigger("harvestmoon",false, Collections.singletonList("nyx"),makeParameterSet(false),
                (trigger, player) -> NyxWorld.get(player.getEntityWorld()).currentEvent instanceof HarvestMoon);
        addTrigger("fallingstars",false, Collections.singletonList("nyx"),makeParameterSet(false),
                (trigger, player) -> NyxWorld.get(player.getEntityWorld()).currentEvent instanceof StarShower);
        addTrigger("rainintensity",false,Collections.singletonList("dsurround"),
                makeParameterSet(true,"level"), Collections.singletonList("identifier"),new ArrayList<>(),
                (trigger,player) -> Weather.getIntensityLevel()>(((float)trigger.getParameterInt("level"))/100f));
        addTrigger("tornado",false,Collections.singletonList("weather2"),
                makeParameterSet(true,"level","detection_range"),Arrays.asList("identifier","level"),
                new ArrayList<>(),(trigger,player) ->  Objects.nonNull(WeatherDataHelper.getWeatherManagerForClient()) &&
                Objects.nonNull(WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()),
                        trigger.getParameterInt("detection_range"))) &&
                WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()),
                        trigger.getParameterInt("detection_range")).levelCurIntensityStage >= trigger.getParameterInt("level"));
        addTrigger("hurricane",false,Collections.singletonList("weather2"),
                makeParameterSet(true,"detection_range"),
                (trigger,player) -> Objects.nonNull(WeatherDataHelper.getWeatherManagerForClient()) &&
                Objects.nonNull(WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()),
                        trigger.getParameterInt("detection_range"))) &&
                WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()),
                        trigger.getParameterInt("detection_range")).isHurricane());
        addTrigger("sandstorm",false,Collections.singletonList("weather2"),
                makeParameterSet(true,"detection_range"),(trigger,player) -> Objects.nonNull(WeatherDataHelper.getWeatherManagerForClient()) &&
                Objects.nonNull(WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()),
                        trigger.getParameterInt("detection_range"))) &&
                WeatherDataHelper.getWeatherManagerForClient().getClosestStormAny(new Vec3(player.getPosition()),
                        trigger.getParameterInt("detection_range")).isHurricane());
        addTrigger("season",false,Collections.singletonList("sereneseasons"),
                makeParameterSet(true,"level"),Arrays.asList("identifier","level"), new ArrayList<>(),
                (trigger,player) -> trigger.seasonHelper(SeasonHelper.getSeasonState(player.getEntityWorld()).getSeason()));
    }

    public static void addParameter(String name, String defaultValue) throws IllegalArgumentException {
        if(DEFAULT_PARAMETER_MAP.containsKey(name)) throw new IllegalArgumentException("Parameter with name "+name+"already exists!");
        DEFAULT_PARAMETER_MAP.put(name,defaultValue);
    }

    //vanilla + no required parameters
    public static void addTrigger(String name, boolean isServerSide, List<String> acceptedParameters,
                                  BiFunction<Trigger, EntityPlayerSP, Boolean> activationFunction) throws IllegalArgumentException {
        addTrigger(name,isServerSide,new ArrayList<>(),acceptedParameters,new ArrayList<>(),new ArrayList<>(),activationFunction);
    }

    //no required parameters
    public static void addTrigger(String name, boolean isServerSide,List<String> requiredMods,List<String> acceptedParameters,
                                  BiFunction<Trigger, EntityPlayerSP, Boolean> activationFunction) throws IllegalArgumentException {
        addTrigger(name,isServerSide,requiredMods,acceptedParameters,new ArrayList<>(),new ArrayList<>(),activationFunction);
    }

    //vanilla trigger
    public static void addTrigger(String name, boolean isServerSide, List<String> acceptedParameters,
                                  List<String> requiredParameters, List<String> choiceRequiredParameters,
                                  BiFunction<Trigger, EntityPlayerSP, Boolean> activationFunction) throws IllegalArgumentException {
        addTrigger(name,isServerSide,new ArrayList<>(),acceptedParameters,requiredParameters,choiceRequiredParameters,activationFunction);
    }

    public static void addTrigger(String name, boolean isServerSide, List<String> requiredMods,
                                  List<String> acceptedParameters, List<String> requiredParameters, List<String> choiceRequiredParameters,
                                  BiFunction<Trigger, EntityPlayerSP, Boolean> activationFunction) throws IllegalArgumentException {
        if (ALL_TRIGGERS.contains(name))
            throw new IllegalArgumentException("Trigger with name " + name + "already exists!");
        ALL_TRIGGERS.add(name);
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
        for(String parameter : ACCEPTED_PARAMETERS.get(trigger)) ret.put(parameter, DEFAULT_PARAMETER_MAP.get(parameter));
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

    public boolean runActivationFunction(EntityPlayerSP player) {
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

    public boolean checkBiome(Biome b, String name, String category, String rainType, float temperature, boolean cold, float rainfall, boolean togglerainfall) {
        if(checkResourceList(Objects.requireNonNull(b.getRegistryName()).toString(),name, false) || name.matches("any")) {
            if(checkResourceList(b.getTempCategory().toString(),category,false) || category.matches("any")) {
                boolean pass = false;
                if(rainfall==Float.MIN_VALUE) pass = true;
                else if(b.getRainfall()>rainfall && togglerainfall) pass = true;
                else if(b.getRainfall()<rainfall && !togglerainfall) pass = true;
                if(pass) {
                    if (rainType.matches("any"))
                        return biomeTemp(b,temperature,cold);
                    else if (rainType.matches("none") && !b.canRain())
                        return biomeTemp(b,temperature,cold);
                    else if (b.canRain() && rainType.matches("rain"))
                        return biomeTemp(b,temperature,cold);
                    else if (b.isSnowyBiome() && rainType.matches("snow"))
                        return biomeTemp(b,temperature,cold);
                }
            }
        }
        return false;
    }

    private boolean biomeTemp(Biome b, float temperature, boolean cold) {
        float bt = b.getDefaultTemperature();
        if (temperature == Float.MIN_VALUE) return true;
        else if (bt >= temperature && !cold) return true;
        else return bt <= temperature && cold;
    }

    public boolean checkResourceList(String type, String resourceList, boolean match) {
        for(String resource : stringBreaker(resourceList,";")) {
            if(match && type.matches(resource)) return true;
            else if(!match && type.contains(resource)) return true;
        }
        return false;
    }

    @SuppressWarnings("ConstantConditions")
    public boolean checkDimensionList(int playerDim, String resourceList) {
        for(String resource : stringBreaker(resourceList,";")) if((String.valueOf(playerDim)).matches(resource)) return true;
        try {
            DimensionType dimension = DimensionType.getById(playerDim);
            return Objects.nonNull(dimension.name()) && checkResourceList(dimension.name(), resourceList, false);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public List<String> parseGamestageList(String resourceList) {
        return Arrays.asList(resourceList.split(";"));
    }

    public boolean checkRiding(String resource, EntityPlayerSP player) {
        if(!player.isRiding() || player.getRidingEntity()==null) return false;
        else if(resource.matches("any")) return true;
        else if(checkResourceList(Objects.requireNonNull(player.getRidingEntity()).getName(),resource,true)) return true;
        else if(EntityList.getKey(player.getRidingEntity())==null) return false;
        return checkResourceList(Objects.requireNonNull(EntityList.getKey(player.getRidingEntity())).toString(),resource,false);
    }

    public boolean checkStat(String stat, int level, EntityPlayerSP player) {
        if(Minecraft.getMinecraft().getConnection()!=null) {
            Objects.requireNonNull(Minecraft.getMinecraft().getConnection()).sendPacket(
                    new CPacketClientStatus(CPacketClientStatus.State.REQUEST_STATS));
            for (StatBase s : StatList.ALL_STATS) {
                return checkResourceList(s.statId, stat, false) && player.getStatFileWriter().readStat(s) > level;
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
