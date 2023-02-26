package mods.thecomputerizer.musictriggers.client.gui.instance;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.*;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Comment;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Main extends AbstractChannelConfig {
    private final Holder fileData;

    public Main(String channelName, Holder fileData) {
        super(channelName);
        this.fileData = MusicTriggers.clone(fileData);
    }

    @Override
    public List<GuiParameters.Parameter> getParameters(GuiType type) {
        return new ArrayList<>();
    }

    @Override
    public List<GuiPage.Icon> getPageIcons(String channel) {
        return Collections.singletonList(ButtonType.MAIN.getIconButton("main",false));
    }

    @Override
    protected List<String> headerLines() {
        return Arrays.asList("# Please refer to the wiki page located at https://github.com/TheComputerizer/Music-Triggers/wiki/The-Basics",
                "# or the discord server located at https://discord.gg/FZHXFYp8fc",
                "# for any specific questions you might have regarding the main config file","");
    }

    @Override
    protected void write(String newFilePath) {
        File file = FileUtil.generateNestedFile("config/MusicTriggers/"+newFilePath+".toml",true);
        List<String> lines = new ArrayList<>();
        List<String> fromData = this.fileData.toLines();
        if(fromData.stream().noneMatch(headerLines()::contains)) lines.addAll(headerLines());
        lines.addAll(this.fileData.toLines());
        FileUtil.writeLinesToFile(file,lines,false);
    }

    public Holder getFileData() {
        return this.fileData;
    }

    public Table getOrCreateTable(@Nullable Table parent, String name) {
        Table table = Objects.nonNull(parent) ? parent.getTableByName(name) : this.fileData.getTableByName(name);
        return Objects.isNull(table) ? this.fileData.addTable(parent, name) : table;
    }

    /**
     * Returns the trigger names as trigger-identifier
     */
    public List<String> getAllTriggers() {
        return getOrCreateTable(null,"triggers").getTableNames();
    }

    public List<String> getAllTriggerNames() {
        return getAllTriggers().stream().map(trigger -> trigger.contains("-") ?
                trigger.substring(0,trigger.indexOf('-')-1) : trigger).distinct()
                .filter(name -> !name.matches("universal")).collect(Collectors.toList());
    }

    public List<String> allIdentifiersOfTrigger(String name) {
        return getAllTriggers().stream().map(trigger -> trigger.contains("-") && trigger.split("-")[0].matches(name) ?
                trigger.substring(trigger.indexOf('-')+1) : null).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public boolean hasTrigger(String name) {
        return getAllTriggerNames().contains(name);
    }

    public List<String> getAllSongNames() {
        return getOrCreateTable(null,"songs").getTableNames().stream()
                .filter(name -> !name.matches("universal")).distinct().collect(Collectors.toList());
    }

    public GuiSelection createMultiSelectTriggerScreen(GuiSuperType parent, Consumer<List<GuiSelection.Element>> multiSelectHandler) {
        return new GuiSelection(parent,GuiType.SELECTION_GENERIC, parent.getInstance(),
                Translate.guiGeneric(false,"selection","multi_triggers"),false,true,true,
                this::multiSelectTriggerElements,multiSelectHandler);
    }

    public List<GuiSelection.Element> multiSelectTriggerElements() {
        List<GuiSelection.Element> elements = new ArrayList<>();
        int index = 0;
        for(Table trigger : getOrCreateTable(null,"triggers").getChildren().values()) {
            elements.add(new GuiSelection.MonoElement("multi_trigger",index, Translate.songInstance(trigger.getName()),
                    Translate.triggerElementHover(trigger)));
            index++;
        }
        return elements;
    }

    public List<GuiSelection.Element> mainElements() {
        return Arrays.asList(new GuiSelection.MonoElement("registered_triggers",0,
                Translate.guiGeneric(false,"selection","group","registered_triggers"),
                Translate.singletonHoverExtra(Translate.condenseList(getAllTriggerNames()),
                        "selection","group","registered_triggers","hover"),
                (parent) -> Minecraft.getMinecraft().displayGuiScreen(new GuiSelection(parent,GuiType.SELECTION_GENERIC,
                        parent.getInstance(),Translate.guiGeneric(false,"selection","group","registered_triggers"),
                        true,true,this::triggerInstanceElements,triggerInstanceButtons(parent))),null),
                new GuiSelection.MonoElement("registered_songs",0,
                        Translate.guiGeneric(false,"selection","group","registered_songs"),
                        Translate.singletonHoverExtra(Translate.condenseList(getAllSongNames()),
                                "selection","group","registered_songs","hover"),
                        (parent) -> Minecraft.getMinecraft().displayGuiScreen(new GuiSelection(parent,GuiType.SELECTION_GENERIC,
                                parent.getInstance(),Translate.guiGeneric(false,"selection","group","registered_songs"),
                                true,true,this::songInstancesElements,songInstancesButtons(parent))),null));
    }

    public List<GuiSelection.Element> triggerInstanceElements() {
        List<GuiSelection.Element> elements = new ArrayList<>();
        elements.add(new GuiSelection.MonoElement("universal_trigger",0,
                Translate.guiGeneric(false, "selection", "universal_trigger"),
                new ArrayList<>(), (parent) -> Minecraft.getMinecraft().displayGuiScreen(
                new GuiParameters(parent, GuiType.PARAMETER_GENERIC, parent.getInstance(),
                        "universal_trigger", Translate.guiGeneric(false, "selection", "universal_trigger"),
                        universalTriggerParameters(),getChannelName())), null));
        elements.addAll(getOrCreateTable(null, "triggers").getContents().stream()
                .filter(type -> (type instanceof Table && (!((Table)type).getName().matches("universal"))) ||
                        type instanceof Comment).map(type -> {
                    if (type instanceof Table) {
                        Table trigger = (Table) type;
                        String triggerWithNumber = trigger.getArrIndex()+"-"+trigger.getName();
                        return new GuiSelection.MonoElement(triggerWithNumber,trigger.getAbsoluteIndex()+1,
                                Translate.songInstance(trigger.getName()), Translate.triggerElementHover(trigger),
                                (parent) -> Minecraft.getMinecraft().displayGuiScreen(
                                new GuiParameters(parent,GuiType.PARAMETER_GENERIC, parent.getInstance(),
                                        "trigger_info",Translate.guiGeneric(false, "selection", "trigger_info"),
                                        triggerInfoParameters(trigger),getChannelName())), (id) -> this.fileData.removeTable(trigger));
                    } else {
                        Comment comment = (Comment) type;
                        return new GuiSelection.MonoElement("comment",comment.getAbsoluteIndex()+1,
                                Translate.guiGeneric(false, "selection", "comment"),
                                (id) -> this.fileData.remove(comment));
                    }
                }).sorted(Comparator.comparingInt(GuiSelection.Element::getIndex))
                .collect(Collectors.toList()));
        return elements;
    }

    private List<GuiParameters.Parameter> universalTriggerParameters() {
        Table universal = getOrCreateTable(getOrCreateTable(null, "triggers"), "universal");
        return Arrays.asList(new GuiParameters.Parameter("universal", "fade_in",
                        universal.getOrCreateVar("fade_in", "0")),
                new GuiParameters.Parameter("universal", "fade_out",
                        universal.getOrCreateVar("fade_out", "0")),
                new GuiParameters.Parameter("universal", "persistence",
                        universal.getOrCreateVar("persistence", "0")),
                new GuiParameters.Parameter("universal", "trigger_delay",
                        universal.getOrCreateVar("trigger_delay", "0")),
                new GuiParameters.Parameter("universal", "song_delay",
                        universal.getOrCreateVar("song_delay", "0")),
                new GuiParameters.Parameter("universal", "start_delay",
                        universal.getOrCreateVar("start_delay", "0")));
    }

    public List<GuiParameters.Parameter> triggerInfoParameters(Table trigger) {
        return Trigger.getAcceptedParameters(trigger.getName())
                .stream().map(parameter -> new GuiParameters.Parameter("trigger_info",parameter,trigger.getName()
                        ,trigger.getOrCreateVar(parameter,Trigger.getDefaultParameter(parameter))))
                .collect(Collectors.toList());
    }

    public ButtonSuperType[] triggerInstanceButtons(GuiSuperType grandfather) {
        List<ButtonSuperType> buttons = new ArrayList<>();
        String displayName = Translate.guiGeneric(false, "button", "add_trigger");
        int width = Minecraft.getMinecraft().fontRenderer.getStringWidth(displayName)+8;
        List<String> hoverText = Translate.singletonHover("button","add_trigger","hover");
        TriConsumer<GuiSuperType, ButtonSuperType, Integer> onClick = (parent, button, type) ->
                Minecraft.getMinecraft().displayGuiScreen(new GuiSelection(parent,GuiType.SELECTION_GENERIC, parent.getInstance(),
                Translate.guiGeneric(false,"selection","group","potential_triggers"),false,true,
                this::getPotentialTriggers,potentialTriggerButtons(parent)));
        buttons.add(grandfather.createBottomButton(displayName,width,1,hoverText,onClick));
        return buttons.toArray(new ButtonSuperType[]{});
    }

    public List<GuiSelection.Element> getPotentialTriggers() {
        List<GuiSelection.Element> elements = new ArrayList<>();
        int index = 0;
        for(String trigger : Trigger.getAcceptedTriggers()) {
            elements.add(new GuiSelection.MonoElement(trigger,index,Translate.guiGeneric(false,"trigger",trigger),
                    Translate.potentialTriggerHover(trigger, hasTrigger(trigger),allIdentifiersOfTrigger(trigger))));
            index++;
        }
        return elements;
    }

    public ButtonSuperType[] potentialTriggerButtons(GuiSuperType grandfather) {
        List<ButtonSuperType> buttons = new ArrayList<>();
        String displayName = Translate.guiGeneric(false, "button", "confirm_triggers");
        int width = Minecraft.getMinecraft().fontRenderer.getStringWidth(displayName)+8;
        List<String> hoverText = Translate.singletonHover("button","confirm_multiselect","hover");
        TriConsumer<GuiSuperType, ButtonSuperType, Integer> onClick = (parent, button, type) -> {
            parent.parentUpdate();
            grandfather.parentUpdate();
            Minecraft.getMinecraft().displayGuiScreen(grandfather);
        };
        buttons.add(grandfather.createBottomButton(displayName,width,1,hoverText,onClick));
        return buttons.toArray(new ButtonSuperType[]{});
    }

    public List<GuiSelection.Element> songInstancesElements() {
        List<GuiSelection.Element> elements = new ArrayList<>();
        elements.add(new GuiSelection.MonoElement("universal_song",0,
                Translate.guiGeneric(false, "selection", "universal_song"),
                new ArrayList<>(), (parent) -> Minecraft.getMinecraft().displayGuiScreen(
                new GuiParameters(parent, GuiType.PARAMETER_GENERIC, parent.getInstance(),
                        "universal_song", Translate.guiGeneric(false, "selection", "universal_song"),
                        universalSongParameters(),getChannelName())), null));
        elements.addAll(getOrCreateTable(null, "songs").getContents().stream()
                .filter(type -> (type instanceof Table && (!((Table)type).getName().matches("universal"))) ||
                        type instanceof Comment).map(type -> {
                    if (type instanceof Table) {
                        Table song = (Table) type;
                        String songWithNumber = song.getArrIndex()+"-"+song.getName();
                        return new GuiSelection.MonoElement(songWithNumber,song.getAbsoluteIndex()+1,
                                Translate.songInstance(song.getName()), Translate.songHover(song.getAbsoluteIndex(),
                                song.getValOrDefault("triggers",new ArrayList<>())),
                                (parent) -> Minecraft.getMinecraft().displayGuiScreen(
                                new GuiSelection(parent, GuiType.PARAMETER_GENERIC, parent.getInstance(),
                                        Translate.withSongInstance(song.getName(), "selection", "group", "song_instance"),
                                        false,false,() -> songInstanceElements(song))),
                                (id) -> this.fileData.removeTable(song));
                    } else {
                        Comment comment = (Comment) type;
                        return new GuiSelection.MonoElement("comment",comment.getAbsoluteIndex()+1,
                                Translate.guiGeneric(false, "selection", "comment"),
                                (id) -> this.fileData.remove(comment));
                    }
                }).sorted(Comparator.comparingInt(GuiSelection.Element::getIndex))
                .collect(Collectors.toList()));
        return elements;
    }

    private List<GuiParameters.Parameter> universalSongParameters() {
        Table universal = getOrCreateTable(getOrCreateTable(null,"songs"),"universal");
        return Arrays.asList(new GuiParameters.Parameter("universal","volume",
                        universal.getOrCreateVar("volume",1f)),
                new GuiParameters.Parameter("universal","pitch",
                        universal.getOrCreateVar("pitch",1f)),
                new GuiParameters.Parameter("universal","play_once",
                        universal.getOrCreateVar("play_once",0)),
                new GuiParameters.Parameter("universal","must_finish",
                        universal.getOrCreateVar("must_finish",false)));
    }

    public ButtonSuperType[] songInstancesButtons(GuiSuperType grandfather) {
        List<ButtonSuperType> buttons = new ArrayList<>();
        String displayName = Translate.guiGeneric(false, "button", "add_song");
        int width = Minecraft.getMinecraft().fontRenderer.getStringWidth(displayName)+8;
        List<String> hoverText = Translate.singletonHover("button","add_song","hover");
        TriConsumer<GuiSuperType, ButtonSuperType, Integer> onClick = (parent, button, type) ->
                Minecraft.getMinecraft().displayGuiScreen(new GuiSelection(parent,GuiType.SELECTION_GENERIC, parent.getInstance(),
                Translate.guiGeneric(false,"selection","group","potential_songs"),false,true,
                () -> parent.getInstance().getChannel(getChannelName()).getPotentialSongs(parent)));
        buttons.add(grandfather.createBottomButton(displayName,width,1,hoverText,onClick));
        return buttons.toArray(new ButtonSuperType[]{});
    }

    public List<GuiSelection.Element> songInstanceElements(Table song) {
        List<GuiSelection.Element> ret = new ArrayList<>();
        ret.add(new GuiSelection.MonoElement("song_info",0,
                Translate.guiGeneric(false, "selection", "song_info"), Translate.songInfoHover(song),
                (parent) -> Minecraft.getMinecraft().displayGuiScreen(
                new GuiParameters(parent, GuiType.PARAMETER_GENERIC, parent.getInstance(), "song_info",
                        song.getName(), songInfoParameters(song),getChannelName())), null));
        ret.add(new GuiSelection.MonoElement("loops",1,
                Translate.guiGeneric(false, "selection", "loops"),
                Translate.singletonHoverExtra("" + song.getTablesByName("loop").size(), "selection", "loop", "set_loops"),
                (parent) -> Minecraft.getMinecraft().displayGuiScreen(
                new GuiSelection(parent, GuiType.SELECTION_GENERIC, parent.getInstance(),
                        Translate.withSongInstance(song.getName(), "selection", "group", "loops"),true,
                        true, () -> getLoopInstances(song),
                        loopInstanceButtons(parent,song))), null));
        return ret.stream().filter((Objects::nonNull))
                .sorted(Comparator.comparingInt(GuiSelection.Element::getIndex)).collect(Collectors.toList());
    }

    public List<GuiParameters.Parameter> songInfoParameters(Table audio) {
        return Arrays.asList(new GuiParameters.Parameter("song_info","load_order",
                        this.fileData.getOrCreateVar(audio,"load_order",audio.getMaxIndex(false,false))),
                new GuiParameters.Parameter("song_info","triggers",
                        this.fileData.getOrCreateVar(audio,"triggers",new ArrayList<String>())),
                new GuiParameters.Parameter("song_info","volume",
                        this.fileData.getOrCreateVar(audio,"volume",1f)),
                new GuiParameters.Parameter("song_info","pitch",
                        this.fileData.getOrCreateVar(audio,"pitch",1f)),
                new GuiParameters.Parameter("song_info","chance",
                        this.fileData.getOrCreateVar(audio,"chance",100)),
                new GuiParameters.Parameter("song_info","play_once",
                        this.fileData.getOrCreateVar(audio,"play_once",0)),
                new GuiParameters.Parameter("song_info","must_finish",
                        this.fileData.getOrCreateVar(audio,"must_finish",false)));
    }

    public ButtonSuperType[] loopInstanceButtons(GuiSuperType grandfather, Table song) {
        List<ButtonSuperType> buttons = new ArrayList<>();
        String displayName = Translate.guiGeneric(false, "button", "add_loop");
        int width = Minecraft.getMinecraft().fontRenderer.getStringWidth(displayName)+8;
        List<String> hoverText = Translate.singletonHover("button","add_loop","hover");
        TriConsumer<GuiSuperType, ButtonSuperType, Integer> onClick =
                (parent, button, type) -> this.fileData.addTable(song,"loop");
        buttons.add(grandfather.createBottomButton(displayName,width,1,hoverText,onClick));
        return buttons.toArray(new ButtonSuperType[]{});
    }

    public List<GuiSelection.Element> getLoopInstances(Table song) {
        List<GuiSelection.Element> elements = new ArrayList<>();
        int index = 0;
        for(Table loop : song.getTablesByName("loop")) {
            elements.add(new GuiSelection.MonoElement("loop",index,Translate.guiGeneric(false, "selection", "loop"),
                    Translate.loopHover(loop),(parent) -> Minecraft.getMinecraft().displayGuiScreen(
                            new GuiParameters(parent, GuiType.PARAMETER_GENERIC, parent.getInstance(),
                    "loop_info", song.getName(),loopParameters(loop),getChannelName())),(id) -> this.fileData.removeTable(loop)));
            index++;
        }
        return elements;
    }

    public List<GuiParameters.Parameter> loopParameters(Table loop) {
        return Arrays.asList(new GuiParameters.Parameter("loop_info","from",
                        this.fileData.getOrCreateVar(loop,"from",0L)),
                new GuiParameters.Parameter("loop_info","to",
                        this.fileData.getOrCreateVar(loop,"to",0L)),
                new GuiParameters.Parameter("loop_info","num_loops",
                        this.fileData.getOrCreateVar(loop,"num_loops",0)));
    }
}
