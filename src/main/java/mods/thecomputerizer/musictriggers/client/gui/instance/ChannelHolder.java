package mods.thecomputerizer.musictriggers.client.gui.instance;

import mods.thecomputerizer.musictriggers.client.gui.*;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class ChannelHolder extends AbstractConfig {

    private final File channelsFile;
    private final Map<String, ChannelInstance> channelInstances;

    public ChannelHolder(File channelsFile, Map<String, ChannelInstance> channelInstances) {
        this.channelsFile = channelsFile;
        this.channelInstances = channelInstances;
    }

    public boolean hasChannel(String channelName) {
        return this.channelInstances.containsKey(channelName);
    }

    public ChannelInstance getChannel(String name) {
        return this.channelInstances.get(name);
    }

    @Override
    public List<GuiParameters.Parameter> getParameters(GuiType type) {
        return new ArrayList<>();
    }

    @Override
    public List<GuiPage.Icon> getPageIcons(String channel) {
        return this.channelInstances.keySet().stream().map(this::getPageIcon).collect(Collectors.toList());
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
        FileUtil.writeLinesToFile(this.channelsFile,lines,false);
    }

    public GuiPage.Icon getPageIcon(String channelName) {
        return ButtonType.CHANNEL.getIconButton(channelName,true);
    }

    public GuiPage.Icon makeNewChannel(String channelName) {
        Table table = new Table(0,null,1,channelName);
        this.channelInstances.put(channelName,new ChannelInstance(table,
                new Main(channelName, Holder.makeEmpty()), new Transitions(channelName, Holder.makeEmpty()),
                new Commands(channelName,Holder.makeEmpty()), new Toggles(channelName,Holder.makeEmpty()),
                new Redirect(channelName,new HashMap<>(), new HashMap<>()), new Jukebox(channelName, new HashMap<>())));
        return getPageIcon(channelName);
    }

    public void deleteChannel(String channelName) {
        this.channelInstances.remove(channelName);
    }
}
