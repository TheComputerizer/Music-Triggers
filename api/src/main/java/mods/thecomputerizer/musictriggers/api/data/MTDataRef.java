package mods.thecomputerizer.musictriggers.api.data;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.global.Debug;
import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterHelper;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterList;
import mods.thecomputerizer.theimpossiblelibrary.api.io.FileHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.TomlWritingException;
import mods.thecomputerizer.theimpossiblelibrary.api.util.Sorting;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static mods.thecomputerizer.theimpossiblelibrary.api.util.Sorting.ALPHABETICAL;

/**
 Static references to all builtin table names, parameter names, and parameter types parsed from config files.
 Used for remapping in the config versioning system and populating default parameter values.
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public final class MTDataRef {
    
    /*-------------------------------------------Inner-------------------------------------------*/
    
    /**
     Channel is a placeholder name.
     */
    public static final TableRef CHANNEL_INFO = new TableRef("channel",Arrays.asList(
            new ParameterRef<>("commands","commands"),
            new ParameterRef<>("explicitly_overrides",false),
            new ParameterRef<>("jukebox","jukebox"),
            new ParameterRef<>("local_folder",MTRef.CONFIG_PATH+"/songs"),
            new ParameterRef<>("main","main"),
            new ParameterRef<>("overrides_music",true),
            new ParameterRef<>("pauses_overrides",false),
            new ParameterRef<>("redirect","redirect"),
            new ParameterRef<>("renders","renders"),
            new ParameterRef<>("sound_category","music")));
    public static final TableRef COMMAND = new TableRef("command",Arrays.asList(
            new ParameterRef<>("literal","literally"),
            new ParameterRef<>("triggers",new ArrayList<>())));
    public static final TableRef DEBUG = new TableRef("debug",Arrays.asList(
            new ParameterRef<>("allow_timestamps",false),
            new ParameterRef<>("block_sound_effects",false),
            new ParameterRef<>("blocked_sound_categories",Collections.singletonList("minecraft;music")),
            new ParameterRef<>("client_only",false),
            new ParameterRef<>("enable_debug_info",false),
            new ParameterRef<>("enable_discs",true),
            new ParameterRef<>("encoding_quality",10),
            new ParameterRef<>("independent_audio_pools",true),
            new ParameterRef<>("interrupted_sound_categories",Collections.singletonList("music")),
            new ParameterRef<>("pause_unless_focused",true),
            new ParameterRef<>("play_normal_music",false),
            new ParameterRef<>("resampling_quality","HIGH"),
            new ParameterRef<>("reverse_priority",false),
            new ParameterRef<>("show_channel_info",true),
            new ParameterRef<>("show_position_info",true),
            new ParameterRef<>("show_song_info",true),
            new ParameterRef<>("show_trigger_info",true),
            new ParameterRef<>("slow_tick_factor",5f),
            new ParameterRef<>("tick_rate",20),
            new ParameterRef<>("write_default_values",Arrays.asList(
                    "channel","from","image","interrupt_handler","main","songs","title","toggle","triggers",
                    "universal_audio","universal_triggers"))));
    public static final TableRef FROM = new TableRef("from",Arrays.asList(
            new ParameterRef<>("channel","not_set"),
            new ParameterRef<>("condition","active"),
            new ParameterRef<>("triggers",new ArrayList<>())));
    public static final TableRef INTERRUPT_HANDLER = new TableRef("interrupt_handler",Arrays.asList(
            new ParameterRef<>("priority",Integer.MAX_VALUE),
            new ParameterRef<>("trigger_whitelist",new ArrayList<>())));
    public static final TableRef LINK = new TableRef("link",Arrays.asList(
            new ParameterRef<>("channel","not_set"),
            new ParameterRef<>("inherit_time",true),
            new ParameterRef<>("linked_triggers",new ArrayList<>()),
            new ParameterRef<>("required_triggers",new ArrayList<>()),
            new ParameterRef<>("resume_after_link",true)));
    public static final TableRef LOOP = new TableRef("loop",Arrays.asList(
            new ParameterRef<>("from",0),
            new ParameterRef<>("loop_count",0),
            new ParameterRef<>("to",0)));
    public static final TableRef TO = new TableRef("to",Arrays.asList(
            new ParameterRef<>("channel","not_set"),
            new ParameterRef<>("condition","switch"),
            new ParameterRef<>("triggers",new ArrayList<>())));
    public static final TableRef UNIVERSAL_AUDIO = new TableRef("universal_audio",Arrays.asList(
            new ParameterRef<>("pitch",1d),
            new ParameterRef<>("play_once",0),
            new ParameterRef<>("speed",1d),
            new ParameterRef<>("volume",1f)
    ),INTERRUPT_HANDLER);
    public static final TableRef UNIVERSAL_TRIGGERS = new TableRef("universal_triggers",Arrays.asList(
            new ParameterRef<>("active_cooldown","0"),
            new ParameterRef<>("fade_in","0"),
            new ParameterRef<>("fade_out","0"),
            new ParameterRef<>("persistence","0"),
            new ParameterRef<>("ticks_before_active","0"),
            new ParameterRef<>("ticks_before_audio","0"),
            new ParameterRef<>("ticks_between_audio","0")));
    
    /*-----------------------------------------Top-Level-----------------------------------------*/
    
    /**
     Audio is a placeholder name.
     */
    public static final TableRef AUDIO = new TableRef("audio",Arrays.asList(
            new ParameterRef<>("chance",100),
            new ParameterRef<>("location","_"),
            new ParameterRef<>("pitch",1d),
            new ParameterRef<>("play_once",0),
            new ParameterRef<>("play_x",1),
            new ParameterRef<>("resume_on_play",false),
            new ParameterRef<>("rotation_speed",0d),
            new ParameterRef<>("speed",1d),
            new ParameterRef<>("start_at",0),
            new ParameterRef<>("volume",1f)
    ),INTERRUPT_HANDLER,LOOP);
    public static final TableRef IMAGE_CARD = buildRenderCard("image",
            new ParameterRef<>("animated",false),
            new ParameterRef<>("fps",20),
            new ParameterRef<>("name","_"));
    public static final TableRef TITLE_CARD = buildRenderCard("title",
            new ParameterRef<>("subtitle_color","white"),
            new ParameterRef<>("subtitle_scale",0.75f),
            new ParameterRef<>("subtitles",new ArrayList<>()),
            new ParameterRef<>("title_color","red"),
            new ParameterRef<>("titles",new ArrayList<>()));
    public static final TableRef TOGGLE = new TableRef("toggle",
            Collections.singleton(new ParameterRef<>("play_once",false)),FROM,TO);
    public static final Set<TableRef> TRIGGERS = buildTables(
            buildTrigger("acidrain",false),
            buildTrigger("advancement",true,
                         new ParameterRef<>("display_matcher","exact"),
                         new ParameterRef<>("display_name",Collections.singletonList("any")),
                         new ParameterRef<>("resource_matcher","partial"),
                         new ParameterRef<>("resource_name",Collections.singletonList("any"))),
            buildTrigger("adventure",false),
            buildTrigger("biome",true,
                         new ParameterRef<>("biome_rainfall",Float.MIN_VALUE),
                         new ParameterRef<>("biome_tag",Collections.singletonList("any")),
                         new ParameterRef<>("biome_tag_matcher","exact"),
                         new ParameterRef<>("biome_temperature",Float.MIN_VALUE),
                         new ParameterRef<>("display_matcher","exact"),
                         new ParameterRef<>("display_name",Collections.singletonList("any")),
                         new ParameterRef<>("rain_type","any"),
                         new ParameterRef<>("rainfall_greater_than",true),
                         new ParameterRef<>("resource_matcher","partial"),
                         new ParameterRef<>("resource_name",Collections.singletonList("any")),
                         new ParameterRef<>("temperature_greater_than",true)),
            buildTrigger("blizzard",false),
            buildTrigger("blockentity",true,
                         new ParameterRef<>("detection_range",16),
                         new ParameterRef<>("detection_y_ratio",0.5f),
                         new ParameterRef<>("display_matcher","exact"),
                         new ParameterRef<>("display_name",Collections.singletonList("any")),
                         new ParameterRef<>("resource_matcher","partial"),
                         new ParameterRef<>("resource_name",Collections.singletonList("any"))),
            buildTrigger("bloodmoon",false),
            buildTrigger("cloudy",false),
            buildTrigger("command",true),
            buildTrigger("creative",false),
            buildTrigger("dead",false),
            buildTrigger("difficulty",true,
                         new ParameterRef<>("level",0)),
            buildTrigger("dimension",true,
                         new ParameterRef<>("display_matcher","exact"),
                         new ParameterRef<>("display_name",Collections.singletonList("any")),
                         new ParameterRef<>("resource_matcher","partial"),
                         new ParameterRef<>("resource_name",Collections.singletonList("any"))),
            buildTrigger("drowning",false,
                         new ParameterRef<>("level",100)),
            buildTrigger("effect",true,
                         new ParameterRef<>("display_matcher","exact"),
                         new ParameterRef<>("display_name",Collections.singletonList("any")),
                         new ParameterRef<>("resource_matcher","partial"),
                         new ParameterRef<>("resource_name",Collections.singletonList("any"))),
            buildTrigger("elytra",false),
            buildTrigger("fishing",false),
            buildTrigger("gamestage",true,
                         new ParameterRef<>("display_matcher","exact"),
                         new ParameterRef<>("display_name",Collections.singletonList("any")),
                         new ParameterRef<>("is_whitelist",true),
                         new ParameterRef<>("resource_matcher","partial"),
                         new ParameterRef<>("resource_name",Collections.singletonList("any"))),
            buildTrigger("generic",false),
            buildTrigger("gui",true,
                         new ParameterRef<>("display_matcher","exact"),
                         new ParameterRef<>("display_name",Collections.singletonList("any")),
                         new ParameterRef<>("resource_matcher","partial"),
                         new ParameterRef<>("resource_name",Collections.singletonList("any"))),
            buildTrigger("harvestmoon",false),
            buildTrigger("height",true,
                         new ParameterRef<>("check_above_level",true),
                         new ParameterRef<>("check_for_sky",true),
                         new ParameterRef<>("level",7)),
            buildTrigger("home",false,
                         new ParameterRef<>("detection_range",16),
                         new ParameterRef<>("detection_y_ratio",0.5f)),
            buildTrigger("huricane",false,
                         new ParameterRef<>("detection_range",16)),
            buildTrigger("inventory",true,
                         new ParameterRef<>("items",Collections.singletonList("empty")),
                         new ParameterRef<>("slots",Collections.singletonList("any"))),
            buildTrigger("light",true,
                         new ParameterRef<>("level",7),
                         new ParameterRef<>("light_type","any")),
            buildTrigger("lightrain",false),
            buildTrigger("loading",false),
            buildTrigger("lowhp",false,
                         new ParameterRef<>("health_percentage",30)),
            buildTrigger("menu",false),
            buildTrigger("mob",true,
                         new ParameterRef<>("champion",Collections.singletonList("any")),
                         new ParameterRef<>("detection_range",16),
                         new ParameterRef<>("detection_y_ratio",0.5f),
                         new ParameterRef<>("display_matcher","exact"),
                         new ParameterRef<>("display_name",Collections.singletonList("any")),
                         new ParameterRef<>("horde_health_percentage",50f),
                         new ParameterRef<>("horde_targeting_percentage",50f),
                         new ParameterRef<>("health",100f),
                         new ParameterRef<>("infernal",Collections.singletonList("any")),
                         new ParameterRef<>("max_entities",Integer.MAX_VALUE),
                         new ParameterRef<>("min_entities",1),
                         new ParameterRef<>("mob_nbt",Collections.singletonList("any")),
                         new ParameterRef<>("mob_targeting",true),
                         new ParameterRef<>("resource_matcher","partial"),
                         new ParameterRef<>("resource_name",Collections.singletonList("any")),
                         new ParameterRef<>("victory_id","not_set"),
                         new ParameterRef<>("victory_percentage",100f)),
            buildTrigger("moon",true,
                         new ParameterRef<>("display_matcher","exact"),
                         new ParameterRef<>("display_name",Collections.singletonList("any")),
                         new ParameterRef<>("resource_matcher","partial"),
                         new ParameterRef<>("resource_name",Collections.singletonList("any"))),
            buildTrigger("pet",false,
                         new ParameterRef<>("detection_range",16),
                         new ParameterRef<>("detection_y_ratio",0.5f)),
            buildTrigger("pvp",true),
            buildTrigger("raid",true,
                         new ParameterRef<>("wave",0)),
            buildTrigger("raining",false),
            buildTrigger("rainintensity",true,
                         new ParameterRef<>("level",50f)),
            buildTrigger("riding",true,
                         new ParameterRef<>("display_matcher","exact"),
                         new ParameterRef<>("display_name",Collections.singletonList("any")),
                         new ParameterRef<>("resource_matcher","partial"),
                         new ParameterRef<>("resource_name",Collections.singletonList("any"))),
            buildTrigger("sandstorm",false,
                         new ParameterRef<>("detection_range",16)),
            buildTrigger("season",true,
                         new ParameterRef<>("season",0)),
            buildTrigger("snowing",false),
            buildTrigger("spectator",false),
            buildTrigger("starshower",false),
            buildTrigger("statistic",true,
                         new ParameterRef<>("display_matcher","exact"),
                         new ParameterRef<>("display_name",Collections.singletonList("any")),
                         new ParameterRef<>("level",0),
                         new ParameterRef<>("resource_matcher","partial"),
                         new ParameterRef<>("resource_name",Collections.singletonList("any"))),
            buildTrigger("storming",false),
            buildTrigger("structure",true,
                         new ParameterRef<>("display_matcher","exact"),
                         new ParameterRef<>("display_name",Collections.singletonList("any")),
                         new ParameterRef<>("resource_matcher","partial"),
                         new ParameterRef<>("resource_name",Collections.singletonList("any"))),
            buildTrigger("time",true,
                         new ParameterRef<>("end_hour",0f),
                         new ParameterRef<>("highest_day_number",Integer.MAX_VALUE),
                         new ParameterRef<>("lowest_day_number",0),
                         new ParameterRef<>("moon_phase",0),
                         new ParameterRef<>("start_hour",0f),
                         new ParameterRef<>("time_bundle","any")),
            buildTrigger("tornado",true,
                         new ParameterRef<>("detection_range",16),
                         new ParameterRef<>("level",0)),
            buildTrigger("underwater",false),
            buildTrigger("victory",true,
                         new ParameterRef<>("victory_timeout",20)),
            buildTrigger("zones",true,
                         new ParameterRef<>("zone_min_x",Integer.MIN_VALUE),
                         new ParameterRef<>("zone_min_y",Integer.MIN_VALUE),
                         new ParameterRef<>("zone_min_z",Integer.MIN_VALUE),
                         new ParameterRef<>("zone_max_x",Integer.MAX_VALUE),
                         new ParameterRef<>("zone_max_y",Integer.MAX_VALUE),
                         new ParameterRef<>("zone_max_z",Integer.MAX_VALUE)),
            UNIVERSAL_TRIGGERS);
    
    /*-------------------------------------------Files-------------------------------------------*/
    
    public static final TableRef COMMANDS = new TableRef("commands",COMMAND);
    public static final TableRef GLOBAL = new TableRef("global",Collections.singleton(
            new ParameterRef<>("toggles_path","toggles")),
                                                       new TableRef("channels",CHANNEL_INFO),DEBUG);
    public static final TableRef MAIN = new TableRef("main",
            new TableRef("songs",AUDIO,UNIVERSAL_AUDIO), new TableRef("triggers",TRIGGERS));
    public static final TableRef RENDERS = new TableRef("renders",IMAGE_CARD,TITLE_CARD);
    public static final TableRef TOGGLES = new TableRef("toggles",TOGGLE);
    public static final Map<String,TableRef> FILE_MAP = buildFileMap();
    
    /*------------------------------------------Methods------------------------------------------*/
    
    private static Map<String,TableRef> buildFileMap() {
        Map<String,TableRef> map = new HashMap<>();
        map.put(COMMANDS.name,COMMANDS);
        map.put(GLOBAL.name,GLOBAL);
        map.put(MAIN.name,MAIN);
        map.put(RENDERS.name,RENDERS);
        map.put(TOGGLES.name,TOGGLES);
        return Collections.unmodifiableMap(map);
    }
    
    private static Set<TableRef> buildTables(TableRef ... tables) {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(tables)));
    }
    
    private static TableRef buildRenderCard(String name, ParameterRef<?> ... extraParameters) {
        List<ParameterRef<?>> parameters = new ArrayList<>(Arrays.asList(
                new ParameterRef<>("fade_in",20),
                new ParameterRef<>("fade_out",20),
                new ParameterRef<>("horizontal_alignment","center"),
                new ParameterRef<>("opacity",1f),
                new ParameterRef<>("play_once",false),
                new ParameterRef<>("scale_x",1f),
                new ParameterRef<>("scale_y",1f),
                new ParameterRef<>("time",100),
                new ParameterRef<>("vague",false),
                new ParameterRef<>("vertical_alignment","center"),
                new ParameterRef<>("x",-1),
                new ParameterRef<>("y",-1)));
        parameters.addAll(Arrays.asList(extraParameters));
        return new TableRef(name,parameters);
    }
    
    private static TableRef buildTrigger(String name, boolean holder, ParameterRef<?> ... extraParameters) {
        List<ParameterRef<?>> parameters = new ArrayList<>(Arrays.asList(
                new ParameterRef<>("active_cooldown",0),
                new ParameterRef<>("fade_in",0),
                new ParameterRef<>("fade_out",0),
                new ParameterRef<>("max_tracks",0),
                new ParameterRef<>("not",false),
                new ParameterRef<>("passive_persistence",0),
                new ParameterRef<>("persistence",0),
                new ParameterRef<>("priority",defaultPriority(name)),
                new ParameterRef<>("start_as_disabled",false),
                new ParameterRef<>("ticks_before_active",0),
                new ParameterRef<>("ticks_before_audio",0),
                new ParameterRef<>("ticks_between_audio",0),
                new ParameterRef<>("toggle_inactive_playable",false),
                new ParameterRef<>("toggle_save_status",0)));
        if(holder) parameters.add(new ParameterRef<>("identifier","not_set"));
        parameters.addAll(Arrays.asList(extraParameters));
        return new TableRef(name,parameters,LINK);
    }
    
    private static boolean canWriteDefaults(String type) {
        Debug debug = ChannelHelper.getGlobalData().getDebug();
        return Objects.isNull(debug) || debug.getParameterAsList("write_default_values").contains(type);
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
            case "adventure":
            case "creative":
            case "difficulty":
            case "gamestage":
            case "spectator":
            default: return 0;
        }
    }
    
    public static @Nullable TableRef findTriggerRef(String trigger) {
        for(TableRef ref : TRIGGERS)
            if(ref.name.equals(trigger)) return ref;
        return null;
    }
    
    public static Set<ParameterRef<?>> getParameterRefs(String trigger) {
        TableRef ref = findTriggerRef(trigger);
        return Objects.nonNull(ref) ? ref.parameters : Collections.emptySet();
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
        }
        
        /**
         Returns true if any entries or tables were added
         */
        public boolean addMissingDefaults(Toml table, LoggableAPI logger) {
            if(!canWriteDefaults(this.name)) return false;
            table.setSorters(this.sorters);
            AtomicBoolean added = new AtomicBoolean(false);
            for(ParameterRef<?> parameter : this.parameters) {
                if(!table.hasEntry(parameter.name)) {
                    table.addEntry(parameter.name, parameter.defaultValue);
                    added.set(true);
                }
            }
            switch(this.name) {
                case "channels": {
                    List<Toml> tables = table.getAllTables();
                    if(tables.isEmpty()) {
                        try {
                            if(CHANNEL_INFO.addMissingDefaults(table.addTable("example",false),logger))
                                added.set(true);
                        } catch(TomlWritingException ex) {
                            logger.logError("Unable to generate example channel!",ex);
                        }
                    } else tables.forEach(t -> {
                        if(CHANNEL_INFO.addMissingDefaults(t,logger)) added.set(true);
                    });
                    break;
                }
                case "songs": {
                    table.getAllTables().forEach(t -> {
                        if((t.getName().equals("universal") ? UNIVERSAL_AUDIO : AUDIO).addMissingDefaults(t,logger))
                            added.set(true);
                    });
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
                                added.set(true);
                            } else childTable = table.getTable(name);
                            if(child.addMissingDefaults(childTable,logger)) added.set(true);
                        } catch(TomlWritingException ex) {
                            logger.logError("Failed to add missing default children",ex);
                        }
                    }
                    break;
                }
            }
            return added.get();
        }
        
        public @Nullable TableRef findChild(String name) {
            if(Objects.isNull(name)) return null;
            for(TableRef ref : this.children)
                if(name.equals(ref.name)) return ref;
            return null;
        }
        
        public @Nullable ParameterRef<?> findParameter(String name) {
            if(Objects.isNull(name)) return null;
            for(ParameterRef<?> ref : this.parameters)
                if(name.equals(ref.name)) return ref;
            return null;
        }
        
        @SuppressWarnings({"RedundantCast","DataFlowIssue","unchecked"})
        public <V> V getOrDefault(Toml table, String name) {
            return table.getOrSetValue(name,((ParameterRef<V>)findParameter(name)).defaultValue);
        }
    }
    
    @Getter
    public static final class ParameterRef<T> {
        
        private final String name;
        private final T defaultValue;
        
        private ParameterRef(String name, T defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }
        
        @SuppressWarnings("unchecked")
        public Parameter<?> toParameter() {
            if(this.defaultValue instanceof List<?>)
                return new ParameterList<>(String.class,new ArrayList<>((List<String>)this.defaultValue)); //TODO Should this really be restricted to lists of strings?
            return ParameterHelper.parameterize((Class<? super T>)this.defaultValue.getClass(),this.defaultValue);
        }
    }
}