package mods.thecomputerizer.musictriggers.client.gui.instance;

import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.gui.*;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Redirect extends AbstractChannelConfig {
    private final Map<String,String> urlMap;
    private final Map<String, ResourceLocation> resourceLocationMap;

    public Redirect(File configFile, String channelName, Map<String,String> urlMap,
                    Map<String, ResourceLocation> resourceLocationMap) {
        super(configFile,channelName);
        this.urlMap = urlMap;
        this.resourceLocationMap = resourceLocationMap;
    }

    @Override
    public List<GuiParameters.Parameter> getParameters(GuiType type) {
        return new ArrayList<>();
    }

    @Override
    public List<GuiPage.Icon> getPageIcons(String channel) {
        return Collections.singletonList(ButtonType.REDIRECT.getIconButton("redirect",false));
    }

    @Override
    protected List<String> headerLines() {
        return Arrays.asList("Format this like name = url",
                "If you are trying to redirect to an already registered resource location Format it like name == location instead",
                "Any lines with Format in the name or = not present will not be read in",
                "Make sure each new entry is on a new line",
                "Here are 2 examples:",
                "thx = https://youtu.be/z3Q4WBpCXhs",
                "title == minecraft:sounds/music/menu/menu1.ogg","");
    }

    @Override
    protected void write(String newFilePath) {
        File file = FileUtil.generateNestedFile("config/MusicTriggers/"+newFilePath+".txt",true);
        List<String> lines = new ArrayList<>(headerLines());
        for(Map.Entry<String, ResourceLocation> internals : resourceLocationMap.entrySet())
            lines.add(LogUtil.injectParameters("{} == {}",internals.getKey(),internals.getValue()));
        for(Map.Entry<String, String> urls : urlMap.entrySet())
            lines.add(LogUtil.injectParameters("{} = {}",urls.getKey(),urls.getValue()));
        FileUtil.writeLinesToFile(file,lines,false);
    }

    public GuiRedirect makeGui(GuiSuperType parent) {
        return new GuiRedirect(parent,GuiType.REDIRECT,parent.getInstance(),this,internalSongs(),
                externalSongs());
    }

    public void openSoundFinderSelection(GuiRedirect parent) {
        Minecraft.getMinecraft().displayGuiScreen(new GuiSelection(parent,GuiType.SELECTION_GENERIC,
                parent.getInstance(),getChannelName(),"song_resources",null,
                Translate.guiGeneric(false,"selection","group","song_resources"), null));
    }

    public List<GuiSelection.Element> getRegisteredSoundInstances(GuiSelection selectionScreen, String channelName) {
        return selectionScreen.getInstance().getRegisteredSounds().stream()
                .map(sound -> new GuiSelection.Element(selectionScreen, channelName, sound,
                        Translate.guiGeneric(false,"selection","song_resources"), null,
                        false, 0, (channel, title) -> {
                            GuiRedirect redirect = (GuiRedirect)selectionScreen.getParent();
                            redirect.addInternal(sound);
                            Minecraft.getMinecraft().displayGuiScreen(redirect);
                        },null))
                .collect(Collectors.toList());
    }

    public boolean isSongEntryUsed(Instance instance, String song){
        return instance.isSongEntryUsed(getChannelName(),song);
    }

    public void save(Map<String, String> internalRedirectMap, Map<String, String> externalRedirectMap) {
        this.resourceLocationMap.clear();
        this.urlMap.clear();
        this.resourceLocationMap.putAll(internalRedirectMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> new ResourceLocation(entry.getValue()))));
        this.urlMap.putAll(externalRedirectMap);
    }

    public Map<String,String> externalSongs() {
        return this.urlMap;
    }

    public Map<String, String> internalSongs() {
        return this.resourceLocationMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString()));
    }
}
