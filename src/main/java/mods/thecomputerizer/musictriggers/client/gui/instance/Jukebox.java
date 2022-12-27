package mods.thecomputerizer.musictriggers.client.gui.instance;

import mods.thecomputerizer.musictriggers.client.gui.ButtonType;
import mods.thecomputerizer.musictriggers.client.gui.GuiPage;
import mods.thecomputerizer.musictriggers.client.gui.GuiParameters;
import mods.thecomputerizer.musictriggers.client.gui.GuiType;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;

import java.io.File;
import java.util.*;

public class Jukebox extends AbstractChannelConfig {
    private final Map<String, String> recordMap;
    public Jukebox(File configFile, String channelName, Map<String, String> recordMap) {
        super(configFile,channelName);
        this.recordMap = recordMap;
    }

    @Override
    public List<GuiParameters.Parameter> getParameters(GuiType type) {
        return new ArrayList<>();
    }

    @Override
    public List<GuiPage.Icon> getPageIcons(String channel) {
        return Collections.singletonList(ButtonType.CHANNEL.getIconButton("jukebox",false));
    }

    @Override
    protected List<String> headerLines() {
        return Arrays.asList("Format this like name = key",
                "The key refers to a lang key in the format of record.musictriggers.key which ",
                "determines the description of the registered disc",
                "Any lines with Format in the name or = not present will not be read in",
                "Make sure each new entry is on a new line",
                "Here is an example",
                "song1 = dragon",
                "");
    }

    @Override
    protected void write(String newFilePath) {
        File file = FileUtil.generateNestedFile("config/MusicTriggers/"+newFilePath+".toml",true);
        List<String> lines = new ArrayList<>(headerLines());
        for(Map.Entry<String, String> recordEntry : this.recordMap.entrySet())
            lines.add(recordEntry.getKey()+" = "+recordEntry.getValue());
        FileUtil.writeLinesToFile(file,lines,false);
    }
}
