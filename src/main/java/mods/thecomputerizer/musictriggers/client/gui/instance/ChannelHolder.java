package mods.thecomputerizer.musictriggers.client.gui.instance;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.client.data.Universal;
import mods.thecomputerizer.musictriggers.client.gui.ButtonType;
import mods.thecomputerizer.musictriggers.client.gui.GuiPage;
import mods.thecomputerizer.musictriggers.client.gui.GuiParameters;
import mods.thecomputerizer.musictriggers.client.gui.GuiType;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;

import java.io.File;
import java.util.*;

public class ChannelHolder extends AbstractConfig {
    private final Jukebox jukeboxInstance;
    private final Map<String, ChannelInstance> channelInstances;

    public ChannelHolder(File configFile, Jukebox customRecordsChannel, Map<String, ChannelInstance> channelInstances) {
        super(configFile);
        this.jukeboxInstance = customRecordsChannel;
        this.channelInstances = channelInstances;
    }

    public boolean hasChannel(String channelName) {
        return this.channelInstances.containsKey(channelName);
    }

    public ChannelInstance getChannel(String name) {
        return this.channelInstances.get(name);
    }

    public List<String> allChannelNames() {
        return new ArrayList<>(this.channelInstances.keySet());
    }

    @Override
    public List<GuiParameters.Parameter> getParameters(GuiType type) {
        return new ArrayList<>();
    }

    @Override
    public List<GuiPage.Icon> getPageIcons(String channel) {
        List<GuiPage.Icon> ret = new ArrayList<>();
        if(this.jukeboxInstance !=null) ret.addAll(this.jukeboxInstance.getPageIcons(null));
        ret.addAll(this.channelInstances.keySet().stream().map(this::getPageIcon).toList());
        return ret;
    }

    @Override
    protected List<String> headerLines() {
        return Arrays.asList("# This mod does not create custom sound categories on it's own, but categories registered from other mods will still work.",
                "# These are the normal categories: master, music, record, weather, block, hostile, neutral, player, ambient, voice",
                "# Note that you can nest config files in folders by specifying the folder before it like folder/filename.",
                "");
    }

    @Override
    protected void write(String path) {
        List<String> lines = new ArrayList<>(headerLines());
        for (ChannelInstance channel : this.channelInstances.values())
            lines.addAll(channel.write());
        FileUtil.writeLinesToFile(getOriginalFile(),lines,false);
    }

    public GuiPage.Icon getPageIcon(String channelName) {
        return ButtonType.CHANNEL.getIconButton(channelName,true);
    }

    public GuiPage.Icon makeNewChannel(String channelName) {
        String configPath = Constants.CONFIG_DIR.getPath()+"/";
        File base = new File(Constants.CONFIG_DIR,channelName);
        String basePath = base.getPath()+"/";
        this.channelInstances.put(channelName,new ChannelInstance(channelName,"music",
                new Main(new File(base,"main"),channelName,new Universal(null),new HashMap<>()),
                basePath+"main",
                new Transitions(new File(base,"transitions"), channelName,new HashMap<>(), new HashMap<>()),
                basePath+"transitions",
                new Commands(new File(base,"commands"), channelName,new HashMap<>()),basePath+"commands",
                new Toggles(new File(base,"toggles"), channelName,new HashMap<>()),basePath+"toggles",
                new Redirect(new File(base,"redirect"), channelName,new HashMap<>(), new HashMap<>()),
                basePath+"redirect",configPath+"songs",true,true));
        return getPageIcon(channelName);
    }

    public void deleteChannel(String channelName) {
        this.channelInstances.remove(channelName);
    }
}
