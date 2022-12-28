package mods.thecomputerizer.musictriggers.client.gui.instance;

import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.*;
import mods.thecomputerizer.musictriggers.config.ConfigToggles;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Toggles extends AbstractChannelConfig {
    private final Map<Integer, ConfigToggles.Toggle> toggleMap;

    public Toggles(File configFile, String channelName, Map<Integer, ConfigToggles.Toggle> toggleMap) {
        super(configFile,channelName);
        this.toggleMap = toggleMap;
    }

    @Override
    public List<GuiParameters.Parameter> getParameters(GuiType type) {
        return new ArrayList<>();
    }

    @Override
    public List<GuiPage.Icon> getPageIcons(String channel) {
        return Collections.singletonList(ButtonType.TOGGLES.getIconButton("toggles", false));
    }

    @Override
    protected List<String> headerLines() {
        return Arrays.asList("# Please refer to the wiki page located at https://github.com/TheComputerizer/Music-Triggers/wiki/The-Basics",
                "# or the discord server located at https://discord.gg/FZHXFYp8fc",
                "# for any specific questions you might have regarding toggles","");
    }

    @Override
    protected void write(String newFilePath) {
        File file = FileUtil.generateNestedFile("config/MusicTriggers/"+newFilePath+".toml",true);
        List<String> lines = new ArrayList<>(headerLines());
        for(ConfigToggles.Toggle toggle : this.toggleMap.values())
            lines.addAll(toggle.getAsTomlLines(this.toggleMap.size()>1));
        FileUtil.writeLinesToFile(file,lines,false);
    }

    public List<GuiSelection.Element> getToggleInstances(GuiSelection selectionScreen, String channelName) {
        return this.toggleMap.entrySet().stream()
                .map(entry -> new GuiSelection.Element(selectionScreen, channelName, "toggles",
                        Translate.guiGeneric(false, "selection", "toggle"),
                        hoverLinesToggle(entry.getValue(),entry.getKey()),false, entry.getKey(),
                        (channel, id) -> new GuiSelection(selectionScreen,GuiType.SELECTION_GENERIC,
                                selectionScreen.getInstance(),channel,"toggle_instance",""+entry.getKey(),
                                Translate.guiGeneric(false,"selection","toggles","instance")
                                        +" "+entry.getKey(),
                                selectionScreen.getInstance().getChannel(channelName).clickAddButton(channelName,null)),
                        (channel, id) -> removeToggle(entry.getKey())))
                .collect(Collectors.toList());
    }

    public List<GuiSelection.Element> getToggleInstance(GuiSelection selectionScreen, String extra) {
        int id = Integer.parseInt(extra);
        return Stream.of(getTriggerElements(selectionScreen,id),getTargetElements(selectionScreen,id))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public void clickAddButton(GuiSuperType parent, String group, String extra) {
        if (group.matches("toggles"))
            addToggle();
        else if (canAddCondition(Integer.parseInt(extra)))
                Minecraft.getInstance().setScreen(new GuiSelection(parent,GuiType.SELECTION_GENERIC,
                        parent.getInstance(),getChannelName(),"toggle_condition",extra,
                        Translate.guiGeneric(false,"selection","group","toggle_condition"),null));

        parent.parentUpdate();
    }

    private List<GuiSelection.Element> getTriggerElements(GuiSelection selectionScreen, int index) {
        ConfigToggles.Toggle toggle = this.toggleMap.get(index);
        return toggle.getTriggerConditions().entrySet().stream()
                .map(entry -> new GuiSelection.Element(selectionScreen, getChannelName(), "trigger",
                        Translate.guiGeneric(false, "selection", "toggle","trigger"), hoverLinesTrigger(entry, index),
                        false, index, (channel, title) -> Minecraft.getInstance().setScreen(
                        new GuiParameters(selectionScreen, GuiType.TOGGLE_INFO, selectionScreen.getInstance(),
                                "toggle_info", "toggle",
                                triggerParameters(index, entry.getKey(), entry.getValue(),
                                        toggle.getPlayOnce()))),
                        (channel, title) -> removeTrigger(index,entry.getKey())))
                .collect(Collectors.toList());
    }

    private List<GuiSelection.Element> getTargetElements(GuiSelection selectionScreen, int index) {
        ConfigToggles.Toggle toggle = this.toggleMap.get(index);
        return toggle.getTargetConditions().entrySet().stream()
                .map(entry -> new GuiSelection.Element(selectionScreen, getChannelName(), "target",
                        Translate.guiGeneric(false, "selection", "target"),
                        hoverLinesTarget(entry.getKey(),entry.getValue(), index),
                        false, 0, (channel, title) -> Minecraft.getInstance().setScreen(
                        new GuiParameters(selectionScreen, GuiType.TOGGLE_INFO, selectionScreen.getInstance(),
                                "toggle_info", "toggle",
                                targetParameters(index, entry.getKey(),entry.getValue()))),
                        (channel, title) -> removeTarget(index,entry.getKey())))
                .collect(Collectors.toList());
    }

    public List<GuiSelection.Element> getConditionElements(GuiSelection selectionScreen, String extra) {
        int id = Integer.parseInt(extra);
        ConfigToggles.Toggle toggle = this.toggleMap.get(id);
        List<String> triggerCon = Arrays.asList("1","2","3");
        List<String> targetCon = Arrays.asList("true","false","switch");
        List<GuiSelection.Element> ret = triggerCon.stream()
                .filter(con -> toggle.getTriggerConditions().containsKey(Integer.parseInt(con)))
                .map(con -> new GuiSelection.Element(selectionScreen,getChannelName(),"trigger_condition",
                        Translate.guiGeneric(false,"selection","toggle_condition","trigger"),
                        hoverLinesCondition(true,con), false, 0, (channel, elementID) -> {
                    addTrigger(id,Integer.parseInt(con));
                    Minecraft.getInstance().setScreen(selectionScreen.getParent());
                }, null))
                .distinct().collect(Collectors.toList());
        ret.addAll(targetCon.stream()
                .filter(con -> toggle.getTargetConditions().containsKey(con))
                .map(con -> new GuiSelection.Element(selectionScreen,getChannelName(),"target_condition",
                        Translate.guiGeneric(false,"selection","toggle_condition","target"),
                        hoverLinesCondition(false,con), false, 1, (channel, elementID) -> {
                    addTarget(id,con);
                    Minecraft.getInstance().setScreen(selectionScreen.getParent());
                }, null))
                .distinct().collect(Collectors.toList()));
        return ret.stream().sorted(Comparator.comparingInt(GuiSelection.Element::getIndex)).collect(Collectors.toList());
    }

    private List<String> hoverLinesCondition(boolean trigger, String condition) {
        if(trigger)
            return Collections.singletonList(Translate.guiGeneric(false, "selection", "toggle_condition",
                    "trigger" + condition));
        return Collections.singletonList(Translate.guiGeneric(false, "selection", "toggle_condition",
                "target", condition));
    }

    private boolean canAddCondition(int index) {
        ConfigToggles.Toggle toggle = this.toggleMap.get(index);
        return toggle.getTargetConditions().entrySet().size()<3 || toggle.getTriggerConditions().entrySet().size()<3;
    }

    public List<GuiParameters.Parameter> triggerParameters(int index, int condition, List<Trigger> triggers, int playOnce) {
        ConfigToggles.Toggle toggle = this.toggleMap.get(index);
        return Arrays.asList(new GuiParameters.Parameter("toggle", "trigger_play_once", null, playOnce,
                        toggle::setPlayOnce),
                new GuiParameters.Parameter("toggle", "trigger_triggers", null, triggers.stream()
                        .map(Trigger::getNameWithID).distinct().collect(Collectors.toList()),
                        (element) -> toggle.getTriggerConditions().put(condition,element.stream()
                                .map(name -> Trigger.createEmptyWithIDForGui(this.getChannelName(),name))
                                .distinct().collect(Collectors.toList()))));
    }

    public List<GuiParameters.Parameter> targetParameters(int index, String condition, List<Trigger> triggers) {
        ConfigToggles.Toggle toggle = this.toggleMap.get(index);
        return Collections.singletonList(new GuiParameters.Parameter("toggle", "target_triggers", null,
                triggers.stream().map(Trigger::getNameWithID).distinct().collect(Collectors.toList()),
                (element) -> toggle.getTargetConditions().put(condition,element.stream()
                        .map(name -> Trigger.createEmptyWithIDForGui(this.getChannelName(),name))
                        .distinct().collect(Collectors.toList()))));
    }

    private List<String> hoverLinesToggle(ConfigToggles.Toggle toggle, int index) {
        return Arrays.asList(Translate.guiGeneric(false,"selection","toggles","load_order")+" "+index,
                toggle.getTriggerConditions().entrySet().size()+" "+
                        Translate.guiGeneric(false,"selection","toggles","num_triggers"),
                toggle.getTargetConditions().entrySet().size()+" "+
                        Translate.guiGeneric(false,"selection","toggles","num_targets"));
    }

    private List<String> hoverLinesTrigger(Map.Entry<Integer, List<Trigger>> triggerEntry, int index) {
        return Arrays.asList(langLoadOrder() + index, langCondition(true, triggerEntry.getKey(), null),
                langTriggers() + Translate.toggleTriggers(triggerEntry.getValue().stream().map(Trigger::getNameWithID)
                        .distinct().collect(Collectors.toList())));
    }

    private List<String> hoverLinesTarget(String key, List<Trigger> val, int index) {
        return Arrays.asList(langLoadOrder() + index, langCondition(false, 0, key),
                langTriggers() + Translate.toggleTriggers(val.stream().map(Trigger::getNameWithID).distinct()
                        .collect(Collectors.toList())));
    }

    private String langCondition(boolean trigger, int triggerCon, String targetCon) {
        if (trigger)
            return Translate.guiGeneric(false, "toggle", "trigger", "condition") + " " +
                    Translate.guiGeneric(false, "toggle", "trigger" + triggerCon);
        return Translate.guiGeneric(false, "toggle", "target", "condition") + " " +
                Translate.guiGeneric(false, "toggle", "target", targetCon);
    }

    private String langTriggers() {
        return Translate.guiGeneric(false, "toggle", "triggers") + " ";
    }

    private String langLoadOrder() {
        return Translate.guiGeneric(false, "toggle", "load_order") + " ";
    }

    public void addToggle() {
        this.toggleMap.put(this.toggleMap.entrySet().size(),new ConfigToggles.Toggle());
    }

    public void removeToggle(int index) {
        this.toggleMap.remove(index);
    }

    public void addTrigger(int index, int condition) {
        this.toggleMap.get(index).addTriggersToCondition(condition,new ArrayList<>());
    }

    public void addTarget(int index, String condition) {
        this.toggleMap.get(index).addTargetsToCondition(condition,new ArrayList<>());
    }

    public void removeTrigger(int index, int condition) {
        this.toggleMap.get(index).getTriggerConditions().put(condition,new ArrayList<>());
    }

    public void removeTarget(int index, String condition) {
        this.toggleMap.get(index).getTargetConditions().put(condition,new ArrayList<>());

    }
}
