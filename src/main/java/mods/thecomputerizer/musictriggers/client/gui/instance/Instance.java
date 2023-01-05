package mods.thecomputerizer.musictriggers.client.gui.instance;

import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.client.gui.*;
import mods.thecomputerizer.musictriggers.config.ConfigChannels;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class Instance {
    private final List<String> registeredSounds;
    private final HashMap<GuiType, AbstractConfig> moduleMap;
    private final Debug debugInstance;
    private final Registration registrationInstance;
    private final ChannelHolder channelHolder;
    private boolean hasChanges;
    private boolean needsReload;

    public static GuiSuperType createTestGui() {
        return new GuiRadial(null, GuiType.MAIN, new Instance(ConfigDebug.copyToGui(), ConfigRegistry.copyToGui(),
                ConfigChannels.copyToGui()));
    }

    private Instance(Debug debug, Registration registration, ChannelHolder channels) {
        this.debugInstance = debug;
        this.registrationInstance = registration;
        this.channelHolder = channels;
        this.moduleMap = createModuleMap();
        this.hasChanges = false;
        this.needsReload = false;
        this.registeredSounds = findAllRegisteredSounds();
    }

    private HashMap<GuiType, AbstractConfig> createModuleMap() {
        HashMap<GuiType, AbstractConfig> ret = new HashMap<>();
        ret.put(GuiType.DEBUG,this.debugInstance);
        ret.put(GuiType.REGISTRATION,this.registrationInstance);
        ret.put(GuiType.CHANNEL,this.channelHolder);
        return ret;
    }

    public void writeAndReload() {
        this.debugInstance.write(null);
        this.registrationInstance.write(null);
        if(this.needsReload) {
            this.channelHolder.write(null);
            EventsClient.initReload();
        }
        else ChannelManager.refreshDebug();
        Minecraft.getInstance().setScreen(null);
    }

    public void madeChanges(boolean needsReload) {
        this.hasChanges = true;
        if(needsReload) this.needsReload = true;
    }

    public boolean hasEdits() {
        return this.hasChanges;
    }

    public AbstractConfig getModule(GuiType type, @Nullable String channel) {
        if(Objects.isNull(channel)) return this.moduleMap.get(type);
        return this.channelHolder.getChannel(channel).getModule(type);
    }

    public List<GuiParameters.Parameter> getParameters(GuiType type, @Nullable String channel) {
        return getModule(type, channel).getParameters(type);
    }

    public List<GuiPage.Icon> getPageIcons(GuiType type) {
        List<AbstractConfig> ordered = Arrays.asList(this.debugInstance,this.registrationInstance,this.channelHolder);
        if(type==GuiType.EDIT) {
            return ordered.stream()
                    .map(module -> module.getPageIcons(null))
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public void pageClick(GuiType type, GuiSuperType screen, String id) {
        if(type==GuiType.EDIT) Minecraft.getInstance().setScreen(
                new GuiPage(screen,GuiType.CHANNEL,this, id,
                        this.channelHolder.getChannel(id).getPageIcons(id),false));
        if(type==GuiType.CHANNEL) {
            GuiPage pageScreen = (GuiPage)screen;
            String channel = pageScreen.getID();
            if(id.matches("edit"))
                Minecraft.getInstance().setScreen(new GuiParameters(screen,GuiType.CHANNEL_INFO,
                        screen.getInstance(),channel,
                        Translate.guiGeneric(false,"titles","channel_info") +": "+channel,
                        this.channelHolder.getChannel(channel).channelInfoParameters()));
            else if(id.matches("redirect")) getChannel(channel).openRedirectGui(screen);
            else {
                String extra = id.matches("transitions") ? "titles" : null;
                Minecraft.getInstance().setScreen(new GuiSelection(screen,GuiType.SELECTION_GENERIC,
                        screen.getInstance(),channel,id,extra,null,
                        this.channelHolder.getChannel(channel).clickAddButton(channel,null)));
            }
        }
    }

    public boolean channelExists(String name) {
        return this.channelHolder.hasChannel(name);
    }

    public ChannelInstance getChannel(String name) {
        return this.channelHolder.getChannel(name);
    }

    public GuiPage.Icon addChannel(String name) {
        return this.channelHolder.makeNewChannel(name);
    }

    public void deleteChannel(String channel) {
        this.channelHolder.deleteChannel(channel);
    }

    public List<String> channelNames() {
        return this.channelHolder.allChannelNames();
    }

    public List<GuiSelection.Element> getElementGroup(GuiSelection selectionScreen, String channel, String group, String extra) {
        return this.channelHolder.getChannel(channel).getElementGroup(selectionScreen, channel, group, extra);
    }

    public List<Trigger> getTriggers(String channel, String index) {
        return this.channelHolder.getChannel(channel).getTriggers(index);
    }

    public List<GuiSelection.Element> transitionsSpecialCase(GuiSelection selectionScreen, String channel, String viewMode) {
        return getChannel(channel).transitionsSpecialCase(selectionScreen, viewMode);
    }

    public boolean isSongEntryUsed(String channel, String song) {
        return getChannel(channel).isSongUsed(song);
    }

    public List<String> getRegisteredSounds() {
        return this.registeredSounds;
    }

    public List<String> findAllRegisteredSounds() {
        RandomSource random = SoundInstance.createUnseededRandom();
        return Minecraft.getInstance().getSoundManager().registry.values().stream()
                .map(accessor -> accessor.list)
                .flatMap(Collection::stream)
                .map(accessor -> accessor.getSound(random).getPath().toString())
                .distinct().sorted().collect(Collectors.toList());
    }
}
