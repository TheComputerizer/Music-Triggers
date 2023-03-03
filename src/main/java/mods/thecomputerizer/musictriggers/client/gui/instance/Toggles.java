package mods.thecomputerizer.musictriggers.client.gui.instance;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.gui.*;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.util.TriConsumer;

import java.io.File;
import java.util.*;

public class Toggles extends AbstractChannelConfig {
    private final Holder fileData;

    public Toggles(String channelName, Holder fileData) {
        super(channelName);
        this.fileData = MusicTriggers.clone(fileData);
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
        List<String> lines = new ArrayList<>();
        List<String> fromData = this.fileData.toLines();
        if(fromData.stream().noneMatch(headerLines()::contains)) lines.addAll(headerLines());
        lines.addAll(this.fileData.toLines());
        FileUtil.writeLinesToFile(file,lines,false);
    }

    public List<GuiSelection.Element> getTogglesElements() {
        List<GuiSelection.Element> elements = new ArrayList<>();
        int index = 0;
        for (Table toggle : this.fileData.getTablesByName("toggle")) {
            elements.add(new GuiSelection.MonoElement("toggle", index,
                    Translate.guiGeneric(false, "selection", "toggle"),
                    Translate.hoverLinesToggle(toggle),
                    (parent) -> Minecraft.getMinecraft().displayGuiScreen(
                            new GuiSelection(parent,GuiType.SELECTION_GENERIC,parent.getInstance(),
                                    Translate.guiGeneric(false, "selection", "toggle","instance"),true,true,
                                    () -> getToggleInstance(toggle),toggleInstanceButtons(parent,toggle))),
                    (id) -> this.fileData.removeTable(toggle)));
            index++;
        }
        return elements;
    }

    public ButtonSuperType[] toggleInstancesButtons(GuiSuperType grandfather) {
        List<ButtonSuperType> buttons = new ArrayList<>();
        String displayName = Translate.guiGeneric(false, "button", "add_toggle");
        int width = Minecraft.getMinecraft().fontRenderer.getStringWidth(displayName)+8;
        List<String> hoverText = Translate.singletonHover("button","add_toggle","hover");
        TriConsumer<GuiSuperType, ButtonSuperType, Integer> onClick = (parent, button, type) -> {
            this.fileData.addTable(null,"toggle");
            parent.parentUpdate();
        };
        buttons.add(grandfather.createBottomButton(displayName,width,1,hoverText,onClick));
        return buttons.toArray(new ButtonSuperType[]{});
    }

    public List<GuiSelection.Element> getToggleInstance(Table toggle) {
        List<GuiSelection.Element> elements = new ArrayList<>(getTriggerElements(toggle));
        int nextIndex = elements.size();
        elements.addAll(getTargetElements(toggle,nextIndex));
        return elements;
    }

    private List<GuiSelection.Element> getTriggerElements(Table toggle) {
        List<GuiSelection.Element> elements = new ArrayList<>();
        int index = 0;
        for (Table from : toggle.getTablesByName("from")) {
            elements.add(new GuiSelection.MonoElement("from", index,
                    from.getValOrDefault("condition",-1).toString(),
                    Translate.hoverLinesTrigger(from),
                    (parent) -> Minecraft.getMinecraft().displayGuiScreen(
                            new GuiParameters(parent, GuiType.PARAMETER_GENERIC,
                                    parent.getInstance(), "toggle_info", "toggle",
                                    triggerParameters(from),getChannelName())),
                    (id) -> this.fileData.removeTable(from)));
            index++;
        }
        return elements;
    }

    private List<GuiSelection.Element> getTargetElements(Table toggle, int index) {
        List<GuiSelection.Element> elements = new ArrayList<>();
        for (Table to : toggle.getTablesByName("to")) {
            elements.add(new GuiSelection.MonoElement("to", index,
                    to.getValOrDefault("condition","missing_condition"),
                    Translate.hoverLinesTarget(to),
                    (parent) -> Minecraft.getMinecraft().displayGuiScreen(
                            new GuiParameters(parent, GuiType.PARAMETER_GENERIC,
                                    parent.getInstance(), "toggle_info", "target",
                                    targetParameters(to), getChannelName())),
                    (id) -> this.fileData.removeTable(to)));
            index++;
        }
        return elements;
    }

    public ButtonSuperType[] toggleInstanceButtons(GuiSuperType grandfather, Table toggle) {
        List<ButtonSuperType> buttons = new ArrayList<>();
        String displayName = Translate.guiGeneric(false, "button", "add_condition");
        int width = Minecraft.getMinecraft().fontRenderer.getStringWidth(displayName)+8;
        List<String> hoverText = Translate.singletonHover("button","add_condition","hover");
        TriConsumer<GuiSuperType, ButtonSuperType, Integer> onClick = (parent, button, type) -> {
            if(canAddCondition(toggle))
                Minecraft.getMinecraft().displayGuiScreen(new GuiSelection(parent, GuiType.SELECTION_GENERIC, parent.getInstance(),
                    Translate.guiGeneric(false, "selection", "toggle", "potential_conditions"),
                    false, false, () -> getConditionElements(parent, toggle)));
        };
        buttons.add(grandfather.createBottomButton(displayName,width,1,hoverText,onClick));
        return buttons.toArray(new ButtonSuperType[]{});
    }

    public List<GuiSelection.Element> getConditionElements(GuiSuperType grandfather, Table toggle) {
        List<GuiSelection.Element> elements = new ArrayList<>();
        int index = 0;
        List<Integer> triggerCon = Arrays.asList(1,2,3);
        List<String> targetCon = Arrays.asList("true","false","switch");
        List<Integer> registeredFromCon = new ArrayList<>();
        for(Table from : toggle.getTablesByName("from")) {
            int condition = from.getValOrDefault("condition",-1);
            if(!registeredFromCon.contains(condition) && triggerCon.contains(condition))
                registeredFromCon.add(condition);
        }
        List<String> registeredToCon = new ArrayList<>();
        for(Table to : toggle.getTablesByName("to")) {
            String condition = to.getValOrDefault("condition","no");
            if(!registeredToCon.contains(condition) && targetCon.contains(condition))
                registeredToCon.add(condition);
        }
        for(int con : triggerCon) {
            if(!registeredFromCon.contains(con))
                elements.add(new GuiSelection.MonoElement("trigger_condition",index,
                        Translate.guiGeneric(false,"selection","toggle_condition","trigger")+" "+con,
                        hoverLinesCondition(toggle),(parent) -> {
                    this.fileData.addVariable(this.fileData.addTable(toggle,"from"),"condition",con);
                    grandfather.parentUpdate();
                    Minecraft.getMinecraft().displayGuiScreen(grandfather);
                }));
            index++;
        }
        for(String con : targetCon) {
            if(!registeredToCon.contains(con))
                elements.add(new GuiSelection.MonoElement("target_condition",index,
                        Translate.guiGeneric(false,"selection","toggle_condition","target")+" "+con,
                        hoverLinesCondition(toggle),(parent) -> {
                    this.fileData.addVariable(this.fileData.addTable(toggle,"to"),"condition",con);
                    grandfather.parentUpdate();
                    Minecraft.getMinecraft().displayGuiScreen(grandfather);
                }));
            index++;
        }
        return elements;
    }

    private List<String> hoverLinesCondition(Table table) {
        if(table.hasVar("condition")) {
            if (table.getName().matches("from"))
                return Collections.singletonList(Translate.guiGeneric(false, "selection", "trigger_condition",
                        "trigger" + table.getValOrDefault("condition","missing")));
            return Collections.singletonList(Translate.guiGeneric(false, "selection", "toggle_condition",
                    "target", table.getValOrDefault("condition","missing")));
        } return new ArrayList<>();
    }

    private boolean canAddCondition(Table toggle) {
        return Objects.nonNull(toggle) &&
                (toggle.getTablesByName("to").size()<3 || toggle.getTablesByName("from").size()<3);
    }

    public List<GuiParameters.Parameter> triggerParameters(Table trigger) {
        return Arrays.asList(new GuiParameters.Parameter("toggle", "trigger_play_once",
                        this.fileData.getOrCreateVar(trigger,"play_once",false)),
                new GuiParameters.Parameter("toggle", "trigger_triggers",
                        this.fileData.getOrCreateVar(trigger,"triggers",new ArrayList<String>())));
    }

    public List<GuiParameters.Parameter> targetParameters(Table target) {
        return Collections.singletonList(new GuiParameters.Parameter("toggle", "target_triggers",
                this.fileData.getOrCreateVar(target,"triggers", new ArrayList<String>())));
    }
}
