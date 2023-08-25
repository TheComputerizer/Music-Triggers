package mods.thecomputerizer.musictriggers.client.gui.instance;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.ClientEvents;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.musictriggers.client.gui.*;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.util.RandomSource;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Instance {

    private static int preferredSortType = 1;
    private static boolean changedSort = false;
    private Screen mainParent;
    private final List<String> registeredSounds;
    private final HashMap<GuiType, AbstractConfig> moduleMap;
    private final Debug debugInstance;
    private final Registration registrationInstance;
    private final ChannelHolder channelHolder;
    private boolean hasChanges;
    private boolean needsReload;

    public static void setPreferredSort(int sort) {
        preferredSortType = sort;
        changedSort = true;
    }

    public static int getPreferredSort() {
        return preferredSortType;
    }

    public static boolean changedPreferredSort() {
        boolean ret = changedSort;
        changedSort = false;
        return ret;
    }

    public static GuiSuperType createGui(@Nullable Screen parent) {
        return new GuiRadial(parent,null, GuiType.MAIN, new Instance(ConfigDebug.copyToGui(), ConfigRegistry.copyToGui(),
                ChannelManager.createGuiData()));
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

    public void writeAndReload(GuiSuperType screen) {
        this.debugInstance.write(null);
        this.registrationInstance.write(null);
        if(!ChannelManager.isClientConfig() || !this.needsReload) {
            MusicTriggers.logExternally(Level.INFO,"In-game changes detected for non-channel files - Refreshing debug information");
            ChannelManager.refreshDebug();
        }
        else {
            MusicTriggers.logExternally(Level.INFO,"In-game changes detected for channel files - Reloading audio system");
            this.channelHolder.write(null);
            ClientEvents.initReload();
        }
        if(Objects.isNull(this.mainParent)) screen.onClose();
        else Minecraft.getInstance().setScreen(this.mainParent);
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
            getChannel(channel).openChannelScreen(pageScreen,channel,id,this.registeredSounds);
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

    public GuiSelection createMultiSelectTriggersScreen(GuiSuperType parent, @Nullable String channel,
                                                        Consumer<List<GuiSelection.Element>> multiSelectHandler) {
        return Objects.isNull(channel) ? null :
                this.channelHolder.getChannel(channel).createMultiSelectTriggerScreen(parent, multiSelectHandler);
    }

    public GuiSelection createChannelSelectParameterScreen(GuiSuperType parent, GuiParameters.Parameter channelParameter) {
        return this.channelHolder.createChannelSelectParameterScreen(parent, channelParameter);
    }

    public List<String> findAllRegisteredSounds() {
        RandomSource rand = RandomSource.create();
        List<String> ret = new ArrayList<>();
        for(WeighedSoundEvents regAccess : Minecraft.getInstance().getSoundManager().registry.values()) {
            for (Weighted<Sound> soundAccessor : regAccess.list) {
                String location = soundAccessor.getSound(rand).getPath().toString();
                if(!ret.contains(location)) ret.add(location);
            }
        }
        Collections.sort(ret);
        return ret;
    }

    public void setMainParent(Screen parent) {
        this.mainParent = parent;
    }

    public Screen getMainParent() {
        return this.mainParent;
    }
}
