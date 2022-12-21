package mods.thecomputerizer.musictriggers.client.gui.instance;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.*;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.util.TriConsumer;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChannelInstance {
    private final String channelName;
    private final HashMap<GuiType, AbstractChannelConfig> moduleMap;
    private String soundCategory;
    private final Main mainInstance;
    private String mainPath;
    private final Transitions transitionsInstance;
    private String transitionsPath;
    private final Commands commandsInstance;
    private String commandsPath;
    private final Toggles togglesInstance;
    private String togglesPath;
    private final Redirect redirectInstance;
    private String redirectPath;
    private String songsFolderPath;
    private boolean pausedByJukeBox;
    private boolean overridesNormalMusic;

    public ChannelInstance(String channelName, String category, Main main, String mainPath, Transitions transitions,
                           String transitionsPath, Commands commands, String commandsPath, Toggles toggles,
                           String togglesPath, Redirect redirect, String redirectPath, String songsFolder, boolean paused,
                           boolean overrides) {
        this.channelName = channelName;
        this.soundCategory = category;
        this.mainInstance = main;
        this.mainPath = mainPath;
        this.transitionsInstance = transitions;
        this.transitionsPath = transitionsPath;
        this.commandsInstance = commands;
        this.commandsPath = commandsPath;
        this.togglesInstance = toggles;
        this.togglesPath = togglesPath;
        this.redirectInstance = redirect;
        this.redirectPath = redirectPath;
        this.songsFolderPath = songsFolder;
        this.pausedByJukeBox = paused;
        this.overridesNormalMusic = overrides;
        this.moduleMap = createModuleMap();
    }

    private HashMap<GuiType, AbstractChannelConfig> createModuleMap() {
        HashMap<GuiType, AbstractChannelConfig> ret = new HashMap<>();
        ret.put(GuiType.MAIN_CONFIG,this.mainInstance);
        ret.put(GuiType.TRANSITIONS,this.transitionsInstance);
        ret.put(GuiType.COMMANDS,this.commandsInstance);
        ret.put(GuiType.TOGGLES,this.togglesInstance);
        ret.put(GuiType.REDIRECT,this.redirectInstance);
        return ret;
    }

    public AbstractChannelConfig getModule(GuiType type) {
        return this.moduleMap.get(type);
    }

    public List<GuiPage.Icon> getPageIcons(String channel) {
        List<GuiPage.Icon> ret = new ArrayList<>();
        ret.add(ButtonType.CHANNEL_INFO.getIconButton("edit",false));
        List<AbstractChannelConfig> ordered = Arrays.asList(this.mainInstance,this.transitionsInstance,this.commandsInstance,
                this.togglesInstance,this.redirectInstance);
        for(AbstractChannelConfig config : ordered) ret.addAll(config.getPageIcons(channel));
        return ret;
    }

    public List<GuiParameters.Parameter> channelInfoParameters() {
        return Arrays.asList(new GuiParameters.Parameter("channel_info","sound_category",null,this.soundCategory,
                        (element) -> this.soundCategory = element),
                new GuiParameters.Parameter("channel_info","main_path",null,this.mainPath,
                        (element) -> this.mainPath = element),
                new GuiParameters.Parameter("channel_info","transitions_path",null,this.transitionsPath,
                        (element) -> this.transitionsPath = element),
                new GuiParameters.Parameter("channel_info","commands_path",null,this.commandsPath,
                        (element) -> this.commandsPath = element),
                new GuiParameters.Parameter("channel_info","toggles_path",null,this.togglesPath,
                        (element) -> this.togglesPath = element),
                new GuiParameters.Parameter("channel_info","redirect_path",null,this.redirectPath,
                        (element) -> this.redirectPath = element),
                new GuiParameters.Parameter("channel_info","audio_folder",null,this.songsFolderPath,
                        (element) -> this.songsFolderPath = element),
                new GuiParameters.Parameter("channel_info","paused_by_jukebox",null,this.pausedByJukeBox,
                        (element) -> this.pausedByJukeBox = element),
                new GuiParameters.Parameter("channel_info","overrides_default_music",null,this.overridesNormalMusic,
                        (element) -> this.overridesNormalMusic = element));
    }

    public List<GuiSelection.Element> getElementGroup(GuiSelection selectionScreen, String channelName,
                                                      String group, String extra) {
        if(group.matches("main")) return this.mainInstance.getSongInstances(selectionScreen, channelName);
        else if(group.matches("triggers")) return this.mainInstance.getTriggerInstances(selectionScreen, channelName, extra);
        else if(group.matches("transitions")) return this.transitionsInstance.getTitleCardInstances(selectionScreen, channelName);
        else if(group.matches("commands")) return this.commandsInstance.getCommandInstances(selectionScreen, channelName);
        else if(group.matches("toggles")) return this.togglesInstance.getToggleInstances(selectionScreen, channelName);
        else if(group.matches("toggle_instance")) return this.togglesInstance.getToggleInstance(selectionScreen, extra);
        else if(group.matches("toggle_condition")) return this.togglesInstance.getConditionElements(selectionScreen, extra);
        else if(group.matches("songs")) return getPotentialSongs(selectionScreen, channelName);
        else if(group.matches("potential_triggers")) return getPotentialTriggers(selectionScreen, channelName, group);
        else if(group.matches("images")) return getPotentialImages(selectionScreen, channelName);
        return redirectInstance.getRegisteredSoundInstances(selectionScreen,channelName);
    }

    public List<GuiSelection.Element> getPotentialSongs(GuiSelection selectionScreen, String channel) {
        return Stream.of(makeSongList(getFolderSongs(),selectionScreen,channel,0),
                        makeSongList(this.redirectInstance.internalSongs(),selectionScreen,channel,1),
                        makeSongList(this.redirectInstance.externalSongs(),selectionScreen,channel,2))
                .flatMap(Collection::stream)
                .sorted(Comparator.comparingInt(GuiSelection.Element::getIndex))
                .collect(Collectors.toList());
    }

    public List<GuiSelection.Element> transitionsSpecialCase(GuiSelection selectionScreen, String viewMode) {
        if(viewMode.matches("titles"))
            return transitionsInstance.getTitleCardInstances(selectionScreen,this.channelName);
        return transitionsInstance.getImageCardInstances(selectionScreen,this.channelName);
    }

    private List<GuiSelection.Element> makeSongList(Map<String, String> songMap, GuiSelection selectionScreen,
                                                    String channelName, int priorityIndex) {
        return songMap.entrySet().stream()
                .map(entry -> new GuiSelection.Element(selectionScreen, channelName, entry.getKey(),
                        Translate.guiGeneric(false,"selection",entry.getKey()), Collections.singletonList(entry.getValue()),
                        false, priorityIndex, (channel, songID) -> {
                    this.mainInstance.addSong(entry.getKey());
                    selectionScreen.getParent().parentUpdate();
                    Minecraft.getMinecraft().displayGuiScreen(selectionScreen.getParent());
                }, null))
                .collect(Collectors.toList());
    }

    private Map<String, String> getFolderSongs() {
        File[] files = new File(this.songsFolderPath).listFiles();
        if(files!=null)
            return Arrays.stream(files)
                    .collect(Collectors.toMap(file -> noEXT(file.getName()),file -> this.songsFolderPath));
        return new HashMap<>();
    }

    private String noEXT(String filename) {
        return filename.substring(0, filename.lastIndexOf('.'));
    }

    public List<GuiSelection.Element> getPotentialImages(GuiSelection selectionScreen, String channel) {
        return findImageResources().stream()
                .map(image -> new GuiSelection.Element(selectionScreen,channel,"image",
                        Translate.guiGeneric(false,"selection","image"),
                        Collections.singletonList(LogUtil.injectParameters("assets/{}/textures", Constants.MODID)),
                        false,0, (channelName, id) -> {
                    this.transitionsInstance.addImageCard(image);
                    selectionScreen.getParent().parentUpdate();
                    Minecraft.getMinecraft().displayGuiScreen(selectionScreen.getParent());
                },null))
                .collect(Collectors.toList());
    }

    public List<String> findImageResources() {
        String initial_path = LogUtil.injectParameters("assets/{}/textures", Constants.MODID);
        Path found_path = null;
        try {
            URL url = MusicTriggers.class.getResource(initial_path);
            if (url != null) {
                URI uri = url.toURI();
                if (uri.getScheme().matches("file")) {
                    url = MusicTriggers.class.getResource(initial_path);
                    if (url != null) found_path = Paths.get(url.toURI());
                } else found_path = FileSystems.newFileSystem(uri,new HashMap<>()).getPath(initial_path);
            }
        } catch (Exception e) {
            MusicTriggers.logExternally(Level.ERROR,"Unable to get calculate base path for image cards with error: ",e);
        }
        if(found_path!=null) {
            File[] images = (new File(found_path.toUri())).listFiles();
            if(images!=null)
                return Arrays.stream(images).map(File::getName).filter(name -> name.contains(".")).collect(Collectors.toList());
            else MusicTriggers.logExternally(Level.ERROR,"The path for image cards was successfully found, but the files were unable to be obtained");
        } else MusicTriggers.logExternally(Level.ERROR,"The base path for image cards did not seem to exist");
        return new ArrayList<>();
    }

    public List<GuiSelection.Element> getPotentialTriggers(GuiSelection selectionScreen, String channel, String songID) {
        return Trigger.getAcceptedTriggers().stream()
                .map(trigger -> new GuiSelection.Element(selectionScreen, channel, trigger,
                        Translate.triggerName(trigger), potentialTriggerHover(trigger),
                        false, 0, (channelName, triggerID) -> {
                    this.mainInstance.addTrigger(songID,triggerID);
                    selectionScreen.getParent().parentUpdate();
                    Minecraft.getMinecraft().displayGuiScreen(selectionScreen.getParent());
                }, null))
                .collect(Collectors.toList());
    }

    private List<String> potentialTriggerHover(String triggerName) {
        List<String> translated = new ArrayList<>();
        boolean needsID = Trigger.acceptsID(triggerName);
        if(needsID)
            translated.add(Translate.guiGeneric(false,"selection","trigger","needs_id"));
        if(!this.mainInstance.hasTrigger(triggerName))
            translated.add(Translate.guiGeneric(false,"selection","trigger","not_registered"));
        else {
            if(needsID)
                translated.add(Translate.condenseIdentifiers(
                        this.mainInstance.allIdentifiersOfTrigger(triggerName), "selection","trigger",
                        "registered_id"));
            else translated.add(Translate.guiGeneric(false,"selection","trigger","is_registered"));
        }
        return translated;
    }

    public List<Trigger> getTriggers(String song) {
        return this.mainInstance.getTriggers(song);
    }

    public boolean isSongUsed(String song) {
        return this.mainInstance.isSongUsed(song);
    }

    public void openRedirectGui(GuiSuperType parent) {
        Minecraft.getMinecraft().displayGuiScreen(this.redirectInstance.makeGui(parent));
    }

    public TriConsumer<GuiSuperType, String, String> clickAddButton(String channelName) {
        return (parent, group, extra) -> {
            if(group.matches("main")) Minecraft.getMinecraft().displayGuiScreen(new GuiSelection(parent,
                    GuiType.SELECTION_GENERIC,parent.getInstance(), channelName,"songs",null,
                    Translate.selectionTitle("songs",channelName),null));
            else if(group.matches("triggers")) Minecraft.getMinecraft().displayGuiScreen(new GuiSelection(parent,
                    GuiType.SELECTION_GENERIC,parent.getInstance(), channelName,"potential_triggers",null,
                    Translate.selectionTitle("potential_triggers",channelName),null));
            else if(group.matches("transitions")) this.transitionsInstance.clickAddButton(parent,group,extra);
            else if(group.matches("commands")) this.commandsInstance.clickAddButton(parent);
            else this.togglesInstance.clickAddButton(parent,group,extra);
        };
    }

    public List<String> write() {
        List<String> lines = new ArrayList<>();
        lines.add(LogUtil.injectParameters("[{}]\n",this.channelName));
        lines.add(LogUtil.injectParameters("\tsound_category = \"{}\"\n",this.soundCategory));
        lines.add(LogUtil.injectParameters("\tmain = \"{}\"\n",this.mainPath));
        lines.add(LogUtil.injectParameters("\ttransitions = \"{}\"\n",this.transitionsPath));
        lines.add(LogUtil.injectParameters("\tcommands = \"{}\"\n",this.commandsPath));
        lines.add(LogUtil.injectParameters("\ttoggles = \"{}\"\n",this.togglesPath));
        lines.add(LogUtil.injectParameters("\tredirect = \"{}\"\n",this.redirectPath));
        lines.add(LogUtil.injectParameters("\tsongs_folder = \"{}\"\n",this.songsFolderPath));
        lines.add(LogUtil.injectParameters("\tpaused_by_jukebox = \"{}\"\n",this.pausedByJukeBox));
        lines.add(LogUtil.injectParameters("\toverrides_normal_music = \"{}\"\n",this.overridesNormalMusic));
        lines.add("");
        this.mainInstance.write(this.mainPath);
        this.transitionsInstance.write(this.transitionsPath);
        this.commandsInstance.write(this.commandsPath);
        this.togglesInstance.write(this.togglesPath);
        this.redirectInstance.write(this.redirectPath);
        return lines;
    }
}
