package mods.thecomputerizer.musictriggers.client.gui.instance;

import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.gui.*;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChannelInstance {
    private final Table info;
    private final HashMap<GuiType, AbstractChannelConfig> moduleMap;
    private final Main mainInstance;
    private final Transitions transitionsInstance;
    private final Commands commandsInstance;
    private final Toggles togglesInstance;
    private final Redirect redirectInstance;
    private final Jukebox jukeboxInstance;

    public ChannelInstance(Table info, Main main, Transitions transitions, Commands commands, Toggles toggles,
                           Redirect redirect, Jukebox jukebox) {
        this.info = info;
        this.mainInstance = main;
        this.transitionsInstance = transitions;
        this.commandsInstance = commands;
        this.togglesInstance = toggles;
        this.redirectInstance = redirect;
        this.jukeboxInstance = jukebox;
        this.moduleMap = createModuleMap();
    }

    private HashMap<GuiType, AbstractChannelConfig> createModuleMap() {
        HashMap<GuiType, AbstractChannelConfig> ret = new HashMap<>();
        ret.put(GuiType.MAIN_CONFIG,this.mainInstance);
        ret.put(GuiType.TRANSITIONS,this.transitionsInstance);
        ret.put(GuiType.COMMANDS,this.commandsInstance);
        ret.put(GuiType.TOGGLES,this.togglesInstance);
        ret.put(GuiType.REDIRECT,this.redirectInstance);
        ret.put(GuiType.JUKEBOX,this.jukeboxInstance);
        return ret;
    }

    public AbstractChannelConfig getModule(GuiType type) {
        return this.moduleMap.get(type);
    }

    public List<GuiPage.Icon> getPageIcons(String channel) {
        List<GuiPage.Icon> ret = new ArrayList<>();
        ret.add(ButtonType.CHANNEL_INFO.getIconButton("edit",false));
        List<AbstractChannelConfig> ordered = Arrays.asList(this.mainInstance,this.transitionsInstance,this.commandsInstance,
                this.togglesInstance,this.redirectInstance,this.jukeboxInstance);
        for(AbstractChannelConfig config : ordered) ret.addAll(config.getPageIcons(channel));
        return ret;
    }

    public GuiSelection createMultiSelectTriggerScreen(GuiSuperType parent, Consumer<List<GuiSelection.Element>> multiSelectHandler) {
        return this.mainInstance.createMultiSelectTriggerScreen(parent, multiSelectHandler);
    }

    public void openChannelScreen(GuiPage parent, String channel, String id, List<String> registeredSounds) {
        GuiSuperType next;
        if(id.matches("edit"))
            next = new GuiParameters(parent,GuiType.CHANNEL_INFO, parent.getInstance(),channel,
                    Translate.guiGeneric(false,"titles","channel_info") +": "+channel,
                    channelInfoParameters());
        else if(id.matches("main")) next = new GuiSelection(parent,GuiType.SELECTION_GENERIC, parent.getInstance(),
                Translate.guiGeneric(false,"selection","group","main")+" "+channel,false,false,
                this.mainInstance::mainElements);
        else if(id.matches("transitions")) next = new GuiSelection(parent,GuiType.SELECTION_GENERIC, parent.getInstance(),
                Translate.guiGeneric(false,"selection","group","transitions")+" "+channel,true,true,
                this.transitionsInstance::getVariableElements,this.transitionsInstance.transitionInstanceButtons(parent));
        else if(id.matches("commands")) next = new GuiSelection(parent,GuiType.SELECTION_GENERIC, parent.getInstance(),
                Translate.guiGeneric(false,"selection","group","commands")+" "+channel,true,true,
                this.commandsInstance::getCommandElements,this.commandsInstance.commandInstanceButtons(parent));
        else if(id.matches("toggles")) next = new GuiSelection(parent,GuiType.SELECTION_GENERIC, parent.getInstance(),
                Translate.guiGeneric(false,"selection","group","toggles")+" "+channel,true,true,
                this.togglesInstance::getTogglesElements,this.togglesInstance.toggleInstancesButtons(parent));
        else if(id.matches("redirect")) next = new GuiSelection(parent,GuiType.SELECTION_GENERIC, parent.getInstance(),
                Translate.guiGeneric(false,"selection","group","redirect")+" "+channel,true,true,
                this.redirectInstance::getRedirectElements,this.redirectInstance.redirectButtons(parent,registeredSounds));
        else {
            next = new GuiSelection(parent,GuiType.SELECTION_GENERIC, parent.getInstance(),
                    Translate.guiGeneric(false,"selection","group","jukebox")+" "+channel,true,true,
                    this.jukeboxInstance::getJukeboxElements,this.jukeboxInstance.jukeboxButtons(parent));
        }
        Minecraft.getInstance().setScreen(next);
    }

    public List<GuiParameters.Parameter> channelInfoParameters() {
        return Arrays.asList(new GuiParameters.Parameter("channel_info","sound_category",
                        this.info.getOrCreateVar("sound_category","music")),
                new GuiParameters.Parameter("channel_info","main_path",
                        this.info.getOrCreateVar("main",info.getName()+"/main")),
                new GuiParameters.Parameter("channel_info","transitions_path",
                        this.info.getOrCreateVar("transitions",info.getName()+"/transitions")),
                new GuiParameters.Parameter("channel_info","commands_path",
                        this.info.getOrCreateVar("commands",info.getName()+"/commands")),
                new GuiParameters.Parameter("channel_info","toggles_path",
                        this.info.getOrCreateVar("toggles",info.getName()+"/toggles")),
                new GuiParameters.Parameter("channel_info","redirect_path",
                        this.info.getOrCreateVar("redirect",info.getName()+"/redirect")),
                new GuiParameters.Parameter("channel_info","jukebox_path",
                        this.info.getOrCreateVar("jukebox",info.getName()+"/jukebox")),
                new GuiParameters.Parameter("channel_info","audio_folder",
                        this.info.getOrCreateVar("songs_folder","config/MusicTriggers/songs")),
                new GuiParameters.Parameter("channel_info","paused_by_jukebox",
                        this.info.getOrCreateVar("paused_by_jukebox",true)),
                new GuiParameters.Parameter("channel_info","overrides_default_music",
                        this.info.getOrCreateVar("overrides_normal_music",true)));
    }

    public List<GuiSelection.Element> getPotentialSongs(GuiSuperType grandfather) {
        return Stream.of(makeSongList(getFolderSongs(),grandfather,0),
                        makeSongList(this.redirectInstance.internalSongs(),grandfather,1),
                        makeSongList(this.redirectInstance.externalSongs(),grandfather,2))
                .flatMap(Collection::stream)
                .sorted(Comparator.comparingInt(GuiSelection.Element::getIndex))
                .collect(Collectors.toList());
    }

    private List<GuiSelection.Element> makeSongList(Map<String, String> songMap, GuiSuperType grandfather, int priorityIndex) {
        return songMap.entrySet().stream()
                .map(entry -> new GuiSelection.MonoElement(entry.getKey(),priorityIndex,
                        Translate.selectionSong(entry.getKey(),"selection",entry.getKey()),
                        Collections.singletonList(entry.getValue()),(parent) -> {
                    this.mainInstance.getFileData().addTable(this.mainInstance.getOrCreateTable(null,"songs"),entry.getKey());
                    grandfather.parentUpdate();
                    Minecraft.getInstance().setScreen(grandfather);
                })).collect(Collectors.toList());
    }

    private Map<String, String> getFolderSongs() {
        File[] files = new File(this.info.getValOrDefault("songs_folder","config/MusicTriggers/songs")).listFiles();
        if(files!=null)
            return Arrays.stream(files).collect(Collectors.toMap(file -> noEXT(file.getName()),
                    file -> this.info.getValOrDefault("songs_folder","config/MusicTriggers/songs")));
        return new HashMap<>();
    }

    private String noEXT(String filename) {
        return filename.substring(0, filename.lastIndexOf('.'));
    }

    public List<String> write() {
        List<String> lines = new ArrayList<>(this.info.toLines());
        lines.add("");
        this.mainInstance.write(this.info.getValOrDefault("main",this.info.getName()+"/main"));
        this.transitionsInstance.write(this.info.getValOrDefault("transitions",this.info.getName()+"/transitions"));
        this.commandsInstance.write(this.info.getValOrDefault("commands",this.info.getName()+"/commands"));
        this.togglesInstance.write(this.info.getValOrDefault("toggles",this.info.getName()+"/toggles"));
        this.redirectInstance.write(this.info.getValOrDefault("redirect",this.info.getName()+"/redirect"));
        this.jukeboxInstance.write(this.info.getValOrDefault("jukebox",this.info.getName()+"/jukebox"));
        return lines;
    }
}
