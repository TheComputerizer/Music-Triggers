package mods.thecomputerizer.musictriggers.client.gui.instance;

import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.*;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Commands extends AbstractChannelConfig {
    private final Map<String, List<Trigger>> commandMap;
    public Commands(File configFile, String channelName, Map<String, List<Trigger>> commandMap) {
        super(configFile,channelName);
        this.commandMap = commandMap;
    }

    @Override
    public List<GuiParameters.Parameter> getParameters(GuiType type) {
        return new ArrayList<>();
    }

    @Override
    public List<GuiPage.Icon> getPageIcons(String channel) {
        return Collections.singletonList(ButtonType.COMMANDS.getIconButton("commands",false));
    }

    @Override
    protected List<String> headerLines() {
        return Arrays.asList("# Please refer to the wiki page located at https://github.com/TheComputerizer/Music-Triggers/wiki/The-Basics",
                "# or the discord server located at https://discord.gg/FZHXFYp8fc",
                "# for any specific questions you might have regarding commands","");
    }

    @Override
    protected void write(String newFilePath) {
        File file = FileUtil.generateNestedFile("config/MusicTriggers/"+newFilePath+".toml",true);
        List<String> lines = new ArrayList<>(headerLines());
        for(String literal : this.commandMap.keySet()) {
            lines.add(this.commandMap.size()>1 ? "[[command]]" : "[command]");
            lines.add(LogUtil.injectParameters("literal = \"{}\"",literal));
            lines.add(LogUtil.injectParameters("triggers = \"{}\"",
                    TextUtil.compileCollection(this.commandMap.get(literal).stream().map(Trigger::getNameWithID)
                            .collect(Collectors.toList()))));
            lines.add("");
        }
        FileUtil.writeLinesToFile(file,lines,false);
    }

    public List<GuiSelection.Element> getCommandInstances(GuiSelection selectionScreen, String channelName) {
        return this.commandMap.entrySet().stream()
                .map(entry -> new GuiSelection.Element(selectionScreen, channelName, "command",
                        Translate.guiGeneric(false,"selection","command"), Collections.singletonList(entry.getKey()),
                        false, 0, (channel, title) -> Minecraft.getMinecraft().displayGuiScreen(
                        new GuiParameters(selectionScreen, GuiType.COMMAND_INFO, selectionScreen.getInstance(),
                                "command_info", "command", commandParameters(entry))),
                        (channel, title) -> removeCommand(entry.getKey())))
                .collect(Collectors.toList());
    }

    public List<GuiParameters.Parameter> commandParameters(Map.Entry<String, List<Trigger>> entry) {
        return Arrays.asList(new GuiParameters.Parameter("command","triggers",null,entry.getValue()
                .stream().map(Trigger::getNameWithID).collect(Collectors.toList()), (element) ->
                this.commandMap.put(entry.getKey(), element.stream()
                        .map(name -> Trigger.createEmptyWithIDForGui(this.getChannelName(),name)).distinct()
                        .collect(Collectors.toList()))),
                new GuiParameters.Parameter("command","literal",null,entry.getKey(),
                        (element) -> this.commandMap.put(element,entry.getValue())));
    }

    public void clickAddButton(GuiSuperType parent) {
        addCommand();
        parent.parentUpdate();
    }

    public void addCommand() {
        this.commandMap.put("temp"+this.commandMap.entrySet().size(),new ArrayList<>());
    }

    public void removeCommand(String literal) {
        this.commandMap.remove(literal);
    }
}
