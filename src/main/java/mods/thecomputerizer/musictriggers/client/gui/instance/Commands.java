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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Commands extends AbstractChannelConfig {
    private final Holder fileData;
    public Commands(String channelName, Holder fileData) {
        super(channelName);
        this.fileData = MusicTriggers.clone(fileData);
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
        List<String> lines = new ArrayList<>();
        List<String> fromData = this.fileData.toLines();
        if(fromData.stream().noneMatch(headerLines()::contains)) lines.addAll(headerLines());
        lines.addAll(this.fileData.toLines());
        FileUtil.writeLinesToFile(file,lines,false);
    }

    public List<GuiSelection.Element> getCommandElements() {
        List<GuiSelection.Element> elements = new ArrayList<>();
        int index = 0;
        for(Table command : this.fileData.getTablesByName("command")) {
            elements.add(new GuiSelection.MonoElement("command",index,
                    Translate.guiGeneric(false,"selection","command"),
                    Collections.singletonList(command.getValOrDefault("literal","")),
                    (parent) -> Minecraft.getInstance().setScreen(
                            new GuiParameters(parent, GuiType.PARAMETER_GENERIC, parent.getInstance(),
                                    "command_info", "command", commandParameters(command),getChannelName())),
                    (id) -> this.fileData.removeTable(command)));
            index++;
        }
        return elements;
    }

    public List<GuiParameters.Parameter> commandParameters(Table table) {
        return Arrays.asList(new GuiParameters.Parameter("command","triggers",
                        this.fileData.getOrCreateVar(table,"triggers", new ArrayList<String>())),
                new GuiParameters.Parameter("command","literal",
                        this.fileData.getOrCreateVar(table,"literal", "")));
    }

    public ButtonSuperType[] commandInstanceButtons(GuiSuperType grandfather) {
        List<ButtonSuperType> buttons = new ArrayList<>();
        String displayName = Translate.guiGeneric(false, "button", "add_command");
        int width = Minecraft.getInstance().font.width(displayName)+8;
        List<String> hoverText = Translate.singletonHover("button","add_command","hover");
        TriConsumer<GuiSuperType, ButtonSuperType, Integer> onClick = (parent, button, type) -> {
            this.fileData.addTable(null,"command");
            parent.parentUpdate();
        };
        buttons.add(grandfather.createBottomButton(displayName,width,1,hoverText,onClick));
        return buttons.toArray(new ButtonSuperType[]{});
    }
}
