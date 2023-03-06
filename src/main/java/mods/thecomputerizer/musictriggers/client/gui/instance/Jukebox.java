package mods.thecomputerizer.musictriggers.client.gui.instance;

import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.gui.*;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.util.TriConsumer;

import java.io.File;
import java.util.*;

public class Jukebox extends AbstractChannelConfig {
    private final Map<String, String> recordMap;
    public Jukebox(String channelName, Map<String, String> recordMap) {
        super(channelName);
        this.recordMap = recordMap;
    }

    @Override
    public List<GuiParameters.Parameter> getParameters(GuiType type) {
        return new ArrayList<>();
    }

    @Override
    public List<GuiPage.Icon> getPageIcons(String channel) {
        return Collections.singletonList(ButtonType.JUKEBOX.getIconButton("jukebox",false));
    }

    @Override
    protected List<String> headerLines() {
        return Arrays.asList("Format this like name = key",
                "The key refers to a lang key in the format of record.musictriggers.key which ",
                "determines the description of the registered disc",
                "Any lines with Format in the name or = not present will not be read in",
                "Make sure each new entry is on a new line",
                "");
    }

    @Override
    protected void write(String newFilePath) {
        File file = FileUtil.generateNestedFile("config/MusicTriggers/"+newFilePath+".txt",true);
        List<String> lines = new ArrayList<>(headerLines());
        for(Map.Entry<String, String> recordEntry : this.recordMap.entrySet())
            lines.add(recordEntry.getKey()+" = "+recordEntry.getValue());
        FileUtil.writeLinesToFile(file,lines,false);
    }

    public List<GuiSelection.Element> getJukeboxElements() {
        List<GuiSelection.Element> elements = new ArrayList<>();
        int index = 0;
        for(Map.Entry<String, String> entry : this.recordMap.entrySet()) {
            elements.add(new GuiSelection.DualElement(entry.getKey(),entry.getValue(),index,
                    Translate.singletonHover("selection","jukebox","record"),
                    Translate.singletonHover("selection","jukebox","record"),
                    (newKey) -> this.recordMap.remove(entry.getKey()),(key, val) -> {
                this.recordMap.remove(entry.getKey());
                this.recordMap.put(key,val);
            }));
            index++;
        }
        return elements;
    }

    public ButtonSuperType[] jukeboxButtons(GuiSuperType grandfather) {
        List<ButtonSuperType> buttons = new ArrayList<>();
        String displayName = Translate.guiGeneric(false, "button", "add_jukebox");
        int width = Minecraft.getInstance().font.width(displayName)+8;
        List<String> hoverText = Translate.singletonHover("button","add_jukebox","hover");
        TriConsumer<GuiSuperType, ButtonSuperType, Integer> onClick = (parent, button, type) -> {
            int i = 0;
            String temp = "temp" + i;
            while (this.recordMap.containsKey(temp)) {
                i++;
                temp = "temp" + i;
            }
            this.recordMap.put(temp,"record");
            parent.parentUpdate();
        };
        buttons.add(grandfather.createBottomButton(displayName,width,1,hoverText,onClick));
        return buttons.toArray(new ButtonSuperType[]{});
    }
}
