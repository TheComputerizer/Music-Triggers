package mods.thecomputerizer.musictriggers.api.data;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.global.Debug;
import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterHelper;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterList;
import mods.thecomputerizer.theimpossiblelibrary.api.core.ArrayHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.io.FileHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.TomlWritingException;
import mods.thecomputerizer.theimpossiblelibrary.api.util.GenericUtils;
import mods.thecomputerizer.theimpossiblelibrary.api.util.Sorting;

import javax.annotation.Nullable;
import java.util.*;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static mods.thecomputerizer.musictriggers.api.MTRef.CONFIG_PATH;
import static mods.thecomputerizer.theimpossiblelibrary.api.util.Sorting.ALPHABETICAL;

/**
 Static references to all builtin table names, parameter names, and parameter types parsed from config files.
 Used for remapping in the config versioning system and populating default parameter values.
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public final class MTDataRef {
    
    public static final TableRef EVENT_RUNNER = new TableRef("event",Arrays.asList(
            buildParameter("end",MAX_VALUE),
            buildParameter("interval",0),
            buildString("name","activate","deactivate","play","playable","playing",
                        "queue","stop","stopped","tick_active","tick_playable","unplayable"),
            buildParameter("song","_"),
            buildParameter("start",0)
    ));
    
    /*-------------------------------------------Inner-------------------------------------------*/
    
    /**
     Channel is a placeholder name.
     */
    public static final TableRef CHANNEL_INFO = new TableRef("channel",Arrays.asList(
            buildParameter("commands","commands"),
            buildParameter("explicitly_overrides",false),
            buildParameter("has_paused_music",false),
            buildParameter("jukebox","jukebox"),
            buildParameter("local_folder",CONFIG_PATH+"/songs"),
            buildParameter("main","main"),
            buildParameter("overrides_music",true),
            buildParameter("paused_by_jukebox",true),
            buildParameter("pauses_overrides",false),
            buildParameter("redirect","redirect"),
            buildParameter("renders","renders"),
            buildParameter("sound_category","music")));
    public static final TableRef COMMAND = new TableRef("command",Arrays.asList(
            buildParameter("literal","literally"),
            buildParameter("triggers",new ArrayList<>())),EVENT_RUNNER);
    public static final TableRef DEBUG = new TableRef("debug",Arrays.asList(
            buildParameter("allow_timestamps",false),
            buildParameter("block_sound_effects",false),
            buildParameter("blocked_sound_categories",Collections.singletonList("minecraft;music")),
            buildParameter("client_only",false),
            buildParameter("enable_debug_info",false),
            buildParameter("enable_discs",true),
            buildInt("encoding_quality",10,ArrayHelper.intRange(1,10)),
            buildParameter("independent_audio_pools",true),
            buildParameter("interrupted_sound_categories",Collections.singletonList("music")),
            buildParameter("pause_unless_focused",true),
            buildParameter("play_normal_music",false),
            buildString("resampling_quality","HIGH","LOW","MEDIUM"),
            buildParameter("reverse_priority",false),
            buildParameter("show_channel_info",true),
            buildParameter("show_position_info",true),
            buildParameter("show_song_info",true),
            buildParameter("show_status_info",true),
            buildParameter("show_target_info",true),
            buildParameter("show_trigger_info",true),
            buildParameter("slow_tick_factor",5f),
            buildParameter("tick_rate",20),
            buildParameter("write_default_values",Arrays.asList(
                    "channel","from","image","interrupt_handler","main","songs","title","toggle","triggers",
                    "universal_audio","universal_triggers"))));
    public static final TableRef FROM = new TableRef("from",Arrays.asList(
            buildParameter("channel","not_set"),
            buildParameter("triggers",new ArrayList<>())),EVENT_RUNNER);
    public static final TableRef INTERRUPT_HANDLER = new TableRef("interrupt_handler",Arrays.asList(
            buildParameter("priority",MAX_VALUE),
            buildParameter("trigger_whitelist",new ArrayList<>())));
    public static final TableRef LINK = new TableRef("link",Arrays.asList(
            buildParameter("inherit_time",true),
            buildParameter("linked_triggers",new ArrayList<>()),
            buildParameter("required_triggers",new ArrayList<>()),
            buildParameter("resume_after_link",true),
            buildParameter("target_channel","not_set")));
    public static final TableRef LOOP = new TableRef("loop",Arrays.asList(
            buildParameter("from",0),
            buildParameter("loop_count",0),
            buildParameter("to",0)),EVENT_RUNNER);
    public static final TableRef TO = new TableRef("to",Arrays.asList(
            buildParameter("channel","not_set"),
            buildString("condition","switch","false","true"),
            buildParameter("triggers",new ArrayList<>())),EVENT_RUNNER);
    public static final TableRef UNIVERSAL_AUDIO = new TableRef("universal_audio",Arrays.asList(
            buildParameter("pitch",1d),
            buildInt("play_once",0,ArrayHelper.intRange(0,5)),
            buildParameter("speed",1d),
            buildParameter("volume",1f)
    ),EVENT_RUNNER,INTERRUPT_HANDLER);
    public static final TableRef UNIVERSAL_TRIGGERS = new TableRef("universal_triggers",Arrays.asList(
            buildParameter("active_cooldown","0"),
            buildParameter("fade_in","0"),
            buildParameter("fade_out","0"),
            buildParameter("persistence","0"),
            buildParameter("ticks_before_active","0"),
            buildParameter("ticks_before_audio","0"),
            buildParameter("ticks_between_audio","0")));
    
    /*-----------------------------------------Top-Level-----------------------------------------*/
    
    /**
     Audio is a placeholder name.
     */
    public static final TableRef AUDIO = new TableRef("audio",Arrays.asList(
            buildParameter("chance",100),
            buildParameter("location","_"),
            buildParameter("pitch",1d),
            buildInt("play_once",0,ArrayHelper.intRange(0,5)),
            buildParameter("play_x",1),
            buildParameter("resume_on_play",false),
            buildParameter("rotation_speed",0d),
            buildParameter("speed",1d),
            buildParameter("start_at",0),
            buildParameter("triggers",new ArrayList<>()),
            buildParameter("volume",1f),
            buildParameter("volume_when_paused",0.25f)
    ),INTERRUPT_HANDLER,LOOP);
    public static final TableRef IMAGE_CARD = buildRenderCard("image",
            buildParameter("animated",false),
            buildParameter("fps",20),
            buildParameter("name","_"));
    public static final TableRef TITLE_CARD = buildRenderCard("title",
            buildString("subtitle_color","white",colors()),
            buildParameter("subtitle_scale",0.75f),
            buildParameter("subtitles",new ArrayList<>()),
            buildString("title_color","red",colors()),
            buildParameter("titles",new ArrayList<>()));
    public static final TableRef TOGGLE = new TableRef("toggle",
            Collections.singleton(buildParameter("play_once",false)),FROM,TO);
    public static final Set<TableRef> TRIGGERS = buildTables(
            buildTrigger("acidrain",false),
            buildTrigger("advancement",true,
                         buildString("display_matcher","exact",matcher()),
                         buildParameter("display_name",Collections.singletonList("any")),
                         buildString("resource_matcher","partial",matcher()),
                         buildParameter("resource_name",Collections.singletonList("any"))),
            buildTrigger("adventure",false),
            buildTrigger("biome",true,
                         buildParameter("biome_rainfall",Float.MIN_VALUE),
                         buildParameter("biome_tag",Collections.singletonList("any")),
                         buildParameter("biome_tag_matcher","exact"),
                         buildParameter("biome_temperature",Float.MIN_VALUE),
                         buildString("display_matcher","exact",matcher()),
                         buildParameter("display_name",Collections.singletonList("any")),
                         buildString("rain_type","any","none","rain","snow"),
                         buildParameter("rainfall_greater_than",true),
                         buildString("resource_matcher","partial",matcher()),
                         buildParameter("resource_name",Collections.singletonList("any")),
                         buildParameter("temperature_greater_than",true)),
            buildTrigger("blizzard",false),
            buildTrigger("blockentity",true,
                         buildParameter("detection_range",16),
                         buildParameter("detection_y_ratio",0.5f),
                         buildString("display_matcher","exact",matcher()),
                         buildParameter("display_name",Collections.singletonList("any")),
                         buildString("resource_matcher","partial",matcher()),
                         buildParameter("resource_name",Collections.singletonList("any"))),
            buildTrigger("bloodmoon",false),
            buildTrigger("cloudy",false),
            buildTrigger("command",true),
            buildTrigger("creative",false),
            buildTrigger("dead",false),
            buildTrigger("difficulty",true,
                         buildInt("level",0,ArrayHelper.intRange(0,4))),
            buildTrigger("dimension",true,
                         buildString("display_matcher","exact",matcher()),
                         buildParameter("display_name",Collections.singletonList("any")),
                         buildString("resource_matcher","partial",matcher()),
                         buildParameter("resource_name",Collections.singletonList("any"))),
            buildTrigger("drowning",false,
                         buildParameter("level",100)),
            buildTrigger("effect",true,
                         buildString("display_matcher","exact",matcher()),
                         buildParameter("display_name",Collections.singletonList("any")),
                         buildString("resource_matcher","partial",matcher()),
                         buildParameter("resource_name",Collections.singletonList("any"))),
            buildTrigger("elytra",false),
            buildTrigger("fishing",false),
            buildTrigger("gamestage",true,
                         buildString("display_matcher","exact",matcher()),
                         buildParameter("display_name",Collections.singletonList("any")),
                         buildParameter("is_whitelist",true),
                         buildString("resource_matcher","partial",matcher()),
                         buildParameter("resource_name",Collections.singletonList("any"))),
            buildTrigger("generic",false),
            buildTrigger("gui",true,
                         buildString("display_matcher","exact",matcher()),
                         buildParameter("display_name",Collections.singletonList("any")),
                         buildString("resource_matcher","partial",matcher()),
                         buildParameter("resource_name",Collections.singletonList("any"))),
            buildTrigger("harvestmoon",false),
            buildTrigger("height",true,
                         buildParameter("check_above_level",false),
                         buildParameter("check_for_sky",true),
                         buildParameter("level",0)),
            buildTrigger("home",false,
                         buildParameter("detection_range",16),
                         buildParameter("detection_y_ratio",0.5f)),
            buildTrigger("huricane",false,
                         buildParameter("detection_range",16)),
            buildTrigger("inventory",true,
                         buildParameter("items",Collections.singletonList("empty")),
                         buildParameter("slots",Collections.singletonList("any"))),
            buildTrigger("light",true,
                         buildInt("level",7,ArrayHelper.intRange(0,15)),
                         buildParameter("light_type","any")),
            buildTrigger("lightrain",false),
            buildTrigger("loading",false),
            buildTrigger("lowhp",false,
                         buildParameter("health_percentage",30)),
            buildTrigger("menu",false),
            buildTrigger("mob",true,
                         buildParameter("champion",Collections.singletonList("any")),
                         buildParameter("detection_range",16),
                         buildParameter("detection_y_ratio",0.5f),
                         buildString("display_matcher","exact",matcher()),
                         buildParameter("display_name",Collections.singletonList("any")),
                         buildParameter("health",100f),
                         buildParameter("horde_health_percentage",50f),
                         buildParameter("horde_targeting_percentage",50f),
                         buildParameter("infernal",Collections.singletonList("any")),
                         buildParameter("max_entities",MAX_VALUE),
                         buildParameter("min_entities",1),
                         buildParameter("mob_nbt",Collections.singletonList("any")),
                         buildParameter("mob_targeting",true),
                         buildString("resource_matcher","partial",matcher()),
                         buildParameter("resource_name",Collections.singletonList("any")),
                         buildParameter("victory_id","not_set"),
                         buildParameter("victory_percentage",100f)),
            buildTrigger("moon",true,
                         buildString("display_matcher","exact",matcher()),
                         buildParameter("display_name",Collections.singletonList("any")),
                         buildString("resource_matcher","partial",matcher()),
                         buildParameter("resource_name",Collections.singletonList("any"))),
            buildTrigger("pet",false,
                         buildParameter("detection_range",16),
                         buildParameter("detection_y_ratio",0.5f)),
            buildTrigger("pvp",true),
            buildTrigger("raid",true,
                         buildInt("wave",0,ArrayHelper.intRange(0,8))),
            buildTrigger("raining",false),
            buildTrigger("rainintensity",true,
                         buildParameter("level",50f)),
            buildTrigger("riding",true,
                         buildString("display_matcher","exact",matcher()),
                         buildParameter("display_name",Collections.singletonList("any")),
                         buildString("resource_matcher","partial",matcher()),
                         buildParameter("resource_name",Collections.singletonList("any"))),
            buildTrigger("sandstorm",false,
                         buildParameter("detection_range",16)),
            buildTrigger("season",true,
                         buildInt("season",0,ArrayHelper.intRange(0,3))),
            buildTrigger("snowing",false),
            buildTrigger("spectator",false),
            buildTrigger("starshower",false),
            buildTrigger("statistic",true,
                         buildString("display_matcher","exact",matcher()),
                         buildParameter("display_name",Collections.singletonList("any")),
                         buildParameter("level",0),
                         buildString("resource_matcher","partial",matcher()),
                         buildParameter("resource_name",Collections.singletonList("any"))),
            buildTrigger("storming",false),
            buildTrigger("structure",true,
                         buildString("display_matcher","exact",matcher()),
                         buildParameter("display_name",Collections.singletonList("any")),
                         buildString("resource_matcher","partial",matcher()),
                         buildParameter("resource_name",Collections.singletonList("any"))),
            buildTrigger("time",true,
                         buildParameter("end_hour",0f),
                         buildParameter("highest_day_number",MAX_VALUE),
                         buildParameter("lowest_day_number",0),
                         buildInt("moon_phase",0,ArrayHelper.intRange(0,8)),
                         buildParameter("start_hour",0f),
                         buildParameter("time_bundle","any")),
            buildTrigger("tornado",true,
                         buildParameter("detection_range",16),
                         buildInt("level",0,ArrayHelper.intRange(0,5))),
            buildTrigger("underwater",false),
            buildTrigger("victory",true,
                         buildParameter("victory_timeout",20)),
            buildTrigger("zones",true,
                         buildParameter("zone_max_x",MAX_VALUE),
                         buildParameter("zone_max_y",MAX_VALUE),
                         buildParameter("zone_max_z",MAX_VALUE),
                         buildParameter("zone_min_x",MIN_VALUE),
                         buildParameter("zone_min_y",MIN_VALUE),
                         buildParameter("zone_min_z",MIN_VALUE)),
            UNIVERSAL_TRIGGERS);
    
    /*-------------------------------------------Files-------------------------------------------*/
    
    public static final TableRef COMMANDS = new TableRef("commands",COMMAND);
    public static final TableRef GLOBAL = new TableRef("global",Collections.singleton(
            buildParameter("toggles_path","toggles")),
            new TableRef("channels",CHANNEL_INFO),DEBUG);
    public static final TableRef MAIN = new TableRef("main",
            new TableRef("songs",AUDIO,UNIVERSAL_AUDIO), new TableRef("triggers",TRIGGERS));
    public static final TableRef RENDERS = new TableRef("renders",IMAGE_CARD,TITLE_CARD);
    public static final TableRef TOGGLES = new TableRef("toggles",TOGGLE);
    public static final Map<String,TableRef> TABLE_MAP = buildTableMap();
    
    /*------------------------------------------Methods------------------------------------------*/
    
    private static void addTable(Map<String,TableRef> map, TableRef ref) {
        map.put(ref.name,ref);
    }
    
    private static ParameterRef<Boolean> buildBoolean(String name, boolean defaultValue) {
        ParameterRef<Boolean> parameter = buildParameter(name,defaultValue);
        parameter.addPotentialValue(!defaultValue);
        return parameter;
    }
    
    private static ParameterRef<Float> buildFloat(String name, float defaultValue, float ... potentialValues) {
        ParameterRef<Float> parameter = buildParameter(name,defaultValue);
        for(float value : potentialValues) parameter.addPotentialValue(value);
        if(potentialValues.length>0) parameter.addPotentialValue(defaultValue);
        return parameter;
    }
    
    private static ParameterRef<Integer> buildInt(String name, int defaultValue, int ... potentialValues) {
        ParameterRef<Integer> parameter = buildParameter(name,defaultValue);
        for(int value : potentialValues) parameter.addPotentialValue(value);
        if(potentialValues.length>0) parameter.addPotentialValue(defaultValue);
        return parameter;
    }
    
    private static <T> ParameterRef<T> buildParameter(String name, T defaultValue) {
        return new ParameterRef<>(name,defaultValue);
    }
    
    private static TableRef buildRenderCard(String name, ParameterRef<?> ... extraParameters) {
        List<ParameterRef<?>> parameters = new ArrayList<>(Arrays.asList(
                buildParameter("fade_in",20),
                buildParameter("fade_out",20),
                buildString("horizontal_alignment","center","center","left","right"),
                buildParameter("opacity",1f),
                buildParameter("play_once",false),
                buildParameter("scale_x",1f),
                buildParameter("scale_y",1f),
                buildParameter("time",100),
                buildParameter("triggers",new ArrayList<>()),
                buildString("vertical_alignment","center","center","left","right"),
                buildParameter("x",-1),
                buildParameter("y",-1)));
        parameters.addAll(Arrays.asList(extraParameters));
        return new TableRef(name,parameters,EVENT_RUNNER);
    }
    
    private static ParameterRef<String> buildString(String name, String defaultValue, String ... potentialValues) {
        ParameterRef<String> parameter = buildParameter(name,defaultValue);
        for(String value : potentialValues) parameter.addPotentialValue(value);
        if(potentialValues.length>0) parameter.addPotentialValue(defaultValue);
        return parameter;
    }
    
    private static Set<TableRef> buildTables(TableRef ... tables) {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(tables)));
    }
    
    private static Map<String,TableRef> buildTableMap() {
        Map<String,TableRef> map = new HashMap<>();
        for(TableRef ref : new TableRef[]{AUDIO,CHANNEL_INFO,COMMAND,COMMANDS,DEBUG,EVENT_RUNNER,FROM,GLOBAL,IMAGE_CARD,
        INTERRUPT_HANDLER,LINK,LOOP,MAIN,RENDERS,TITLE_CARD,TO,TOGGLE,TOGGLES,UNIVERSAL_AUDIO,UNIVERSAL_TRIGGERS})
            addTable(map,ref);
        for(TableRef ref : TRIGGERS) addTable(map,ref);
        return Collections.unmodifiableMap(map);
    }
    
    private static TableRef buildTrigger(String name, boolean holder, ParameterRef<?> ... extraParameters) {
        List<ParameterRef<?>> parameters = new ArrayList<>(Arrays.asList(
                buildParameter("active_cooldown",0),
                buildParameter("fade_in",0),
                buildParameter("fade_out",0),
                buildParameter("max_tracks",0),
                buildParameter("not",false),
                buildParameter("passive_persistence",0),
                buildParameter("persistence",0),
                buildParameter("play_when_paused",false),
                buildParameter("priority",defaultPriority(name)),
                buildParameter("start_as_disabled",false),
                buildParameter("ticks_before_active",0),
                buildParameter("ticks_before_audio",0),
                buildParameter("ticks_between_audio",0),
                buildParameter("toggle_inactive_playable",false),
                buildParameter("toggle_save_status",0)));
        if(holder) parameters.add(buildParameter("identifier","not_set"));
        parameters.addAll(Arrays.asList(extraParameters));
        return new TableRef(name,parameters,LINK);
    }
    
    public static boolean canWriteDefaults(String type) {
        Debug debug = ChannelHelper.getGlobalData().getDebug();
        return type.equals("debug") || type.equals("global") ||
               debug.getParameterAsList("write_default_values").contains(type);
    }
    
    private static String[] colors() {
        return new String[]{"aqua","black","blue","dark_aqua","dark_blue","dark_gray","dark_green","dark_purple", 
                "dark_red","gold","gray","green","light_purple","red","white","yellow"};
    }
    
    private static int defaultPriority(String trigger) {
        switch(trigger) {
            case "dimension": return 100;
            case "season": return 200;
            case "statistic": return 300;
            case "pet": return 400;
            case "cloudy": return 500;
            case "biome": return 600;
            case "fishing": return 700;
            case "time": return 800;
            case "light": return 900;
            case "lightrain": return 1000;
            case "home": return 1100;
            case "elytra": return 1200;
            case "effect": return 1300;
            case "inventory": return 1400;
            case "riding": return 1500;
            case "acidrain": return 1600;
            case "starshower": return 1700;
            case "raining": return 1800;
            case "rainintensity": return 1900;
            case "snowing": return 2000;
            case "harvestmoon": return 2100;
            case "bluemoon": return 2200;
            case "moon": return 2300;
            case "storming": return 2400;
            case "bloodmoon": return 2500;
            case "blizzard": return 2600;
            case "hurricane": return 2700;
            case "sandstorm": return 2800;
            case "height": return 2900;
            case "tornado": return 3000;
            case "underwater": return 3100;
            case "structure": return 3200;
            case "zones": return 3300;
            case "blockentity": return 3400;
            case "advancement": return 3500;
            case "command": return 3600;
            case "gui": return 3700;
            case "mob": return 3800;
            case "pvp": return 3900;
            case "lowhp": return 4000;
            case "raid": return 4100;
            case "victory": return 4200;
            case "drowning": return 4300;
            case "dead": return 4400;
            default: return 0;
        }
    }
    
    public static @Nullable TableRef findTriggerRef(String trigger) {
        for(TableRef ref : TRIGGERS)
            if(ref.name.equals(trigger)) return ref;
        return null;
    }
    
    private static String[] matcher() {
        return new String[]{"exact","partial","regex"};
    }
    
    public static void writeToFile(Toml toml, String path) {
        List<String> lines = new ArrayList<>();
        toml.write(lines,0,true);
        FileHelper.writeLines(path+".toml",lines,false);
    }
    
    @Getter
    public static final class TableRef {
        
        private final String name;
        private final Set<ParameterRef<?>> parameters;
        private final Set<TableRef> children;
        private final Sorting[] sorters;
        private TableRef parent;
        
        private TableRef(String name, TableRef ... children) {
            this(name,new HashSet<>(Arrays.asList(children)));
        }
        
        private TableRef(String name, Set<TableRef> children) {
            this(name,Collections.emptySet(),children);
        }
        
        private TableRef(String name, Collection<ParameterRef<?>> parameters, TableRef ... children) {
            this(name,parameters,new HashSet<>(Arrays.asList(children)));
        }
        
        private TableRef(String name, Collection<ParameterRef<?>> parameters, Set<TableRef> children) {
            this.name = name;
            this.parameters = Collections.unmodifiableSet(new HashSet<>(parameters));
            this.children = Collections.unmodifiableSet(children);
            this.sorters = new Sorting[]{ALPHABETICAL}; //TODO Per table?
            for(TableRef child : this.children) child.parent = this;
        }
        
        /**
         Returns true if any entries or tables were added
         */
        public boolean addMissingDefaults(Toml table, LoggableAPI logger) {
            if(!canWriteDefaults(this.name)) return false;
            table.setSorters(this.sorters);
            boolean added = false;
            for(ParameterRef<?> parameter : this.parameters) {
                if(!table.hasEntry(parameter.name)) {
                    table.addEntry(parameter.name,parameter.defaultValue);
                    added = true;
                }
            }
            switch(this.name) {
                case "channels": {
                    List<Toml> tables = table.getAllTables();
                    if(tables.isEmpty()) {
                        try {
                            if(CHANNEL_INFO.addMissingDefaults(table.addTable("example",false),logger))
                                added = true;
                        } catch(TomlWritingException ex) {
                            logger.logError("Unable to generate example channel!",ex);
                        }
                    } else
                        for(Toml t : tables)
                            if(CHANNEL_INFO.addMissingDefaults(t,logger)) added = true;
                    break;
                }
                case "songs": {
                    for(Toml t : table.getAllTables())
                        if((t.getName().equals("universal") ? UNIVERSAL_AUDIO : AUDIO).addMissingDefaults(t,logger))
                            added = true;
                    break;
                }
                default: {
                    for(TableRef child : this.children) {
                        if(!canWriteDefaults(child.name)) continue;
                        try {
                            Toml childTable;
                            String name = child.name.equals("universal_triggers") ? "universal" : child.name;
                            if(!table.hasTable(name)) {
                                childTable = table.addTable(name,true);
                                added = true;
                            } else childTable = table.getTable(name);
                            if(child.addMissingDefaults(childTable,logger)) added = true;
                        } catch(TomlWritingException ex) {
                            logger.logError("Failed to add missing default children",ex);
                        }
                    }
                    break;
                }
            }
            return added;
        }
        
        public @Nullable TableRef findChild(String name) {
            if(Objects.isNull(name)) return null;
            for(TableRef ref : this.children)
                if(name.equals(ref.name)) return ref;
            return null;
        }
        
        public @Nullable ParameterRef<?> findParameter(String name) {
            for(ParameterRef<?> ref : this.parameters)
                if(ref.name.equals(name)) return ref;
            return null;
        }
        
        public Toml findToml(ChannelHelper helper) {
            switch(this.name) {
                case "debug": return ChannelHelper.getGlobalData().getDebug().toToml();
                case "toggles": return helper.togglesAsToml();
                default: return Toml.getEmpty();
            }
        }
        
        public boolean hasParent() {
            return Objects.nonNull(this.parent);
        }
    }
    
    @Getter
    public static final class ParameterRef<T> {
        
        private final String name;
        private final T defaultValue;
        private final Collection<T> potentialValues;
        
        private ParameterRef(String name, T defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
            this.potentialValues = new HashSet<>();
        }
        
        void addPotentialValue(T value) {
            this.potentialValues.add(value);
        }
        
        public boolean hasPotentialValues() {
            return !this.potentialValues.isEmpty();
        }
        
        public boolean isValid(Object value) {
            if(this.potentialValues.isEmpty()) return true;
            for(T potential : this.potentialValues)
                if(GenericUtils.matches(potential,value)) return true;
            return false;
        }
        
        @SuppressWarnings("unchecked")
        public Parameter<?> toParameter() {
            if(this.defaultValue instanceof List<?>)
                return new ParameterList<>(String.class,new ArrayList<>((List<String>)this.defaultValue)); //TODO Should this really be restricted to lists of strings?
            return ParameterHelper.parameterize((Class<? super T>)this.defaultValue.getClass(),this.defaultValue);
        }
    }
}