package mods.thecomputerizer.musictriggers.client.gui.instance;

import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.gui.*;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.util.TriConsumer;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Redirect extends AbstractChannelConfig {
    private final Map<String,String> urlMap;
    private final Map<String, ResourceLocation> resourceLocationMap;

    public Redirect(String channelName, Map<String,String> urlMap,
                    Map<String, ResourceLocation> resourceLocationMap) {
        super(channelName);
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
                "");
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

    public List<GuiSelection.Element> getRedirectElements() {
        List<GuiSelection.Element> elements = new ArrayList<>();
        int index = 0;
        for(Map.Entry<String, String> externalEntry : this.urlMap.entrySet()) {
            elements.add(new GuiSelection.DualElement(externalEntry.getKey(),externalEntry.getValue(),index,
                    Translate.singletonHover("selection","redirect","external"),
                    Translate.singletonHover("selection","redirect","external"),
                    (newKey) -> this.urlMap.entrySet().remove(externalEntry),(key, val) -> {
                this.urlMap.entrySet().remove(externalEntry);
                this.urlMap.put(key,val);
            }));
            index++;
        }
        for(Map.Entry<String, ResourceLocation> internalEntry : this.resourceLocationMap.entrySet()) {
            elements.add(new GuiSelection.DualElement(internalEntry.getKey(),internalEntry.getValue().toString(),index,
                    Translate.singletonHover("selection","redirect","internal"),
                    Translate.singletonHover("selection","redirect","internal"),
                    (newKey) -> this.resourceLocationMap.entrySet().remove(internalEntry),(key, val) -> {
                this.resourceLocationMap.entrySet().remove(internalEntry);
                this.resourceLocationMap.put(key,new ResourceLocation(val));
            }));
            index++;
        }
        return elements;
    }

    public ButtonSuperType[] redirectButtons(GuiSuperType grandfather, List<String> registeredSounds) {
        List<ButtonSuperType> buttons = new ArrayList<>();
        String displayName = Translate.guiGeneric(false, "button", "add_internal");
        int width = Minecraft.getInstance().font.width(displayName)+8;
        List<String> hoverText = Translate.singletonHover("button","add_internal","hover");
        TriConsumer<GuiSuperType, ButtonSuperType, Integer> onClick = (parent, button, type) -> {
            Minecraft.getInstance().setScreen(new GuiSelection(parent,GuiType.SELECTION_GENERIC,
                    parent.getInstance(), Translate.guiGeneric(false,"selection","song_resources"),
                    false,true,() -> getRegisteredSoundElements(parent,registeredSounds)));
            parent.parentUpdate();
        };
        buttons.add(grandfather.createBottomButton(displayName,width,1,hoverText,onClick));
        displayName = Translate.guiGeneric(false, "button", "add_external");
        width = Minecraft.getInstance().font.width(displayName)+8;
        hoverText = Translate.singletonHover("button","add_external","hover");
        onClick = (parent, button, type) -> {
            int i = 0;
            String temp = "temp" + i;
            while (this.urlMap.containsKey(temp)) {
                i++;
                temp = "temp" + i;
            }
            this.urlMap.put(temp,"url");
            parent.parentUpdate();
        };
        buttons.add(grandfather.createBottomButton(displayName,width,1,hoverText,onClick));
        return buttons.toArray(new ButtonSuperType[]{});
    }

    public List<GuiSelection.Element> getRegisteredSoundElements(GuiSuperType grandfather, List<String> registeredSounds) {
        List<GuiSelection.Element> elements = new ArrayList<>();
        int index = 0;
        for(String sound : registeredSounds) {
            elements.add(new GuiSelection.MonoElement("registered_sound",index,sound,new ArrayList<>(),
                    (parent) -> {
                        int i = 0;
                        String temp = "temp" + i;
                        while (this.resourceLocationMap.containsKey(temp)) {
                            i++;
                            temp = "temp" + i;
                        }
                        this.resourceLocationMap.put(temp, new ResourceLocation(sound));
                        grandfather.parentUpdate();
                        Minecraft.getInstance().setScreen(grandfather);
                    }));
            index++;
        }
        return elements;
    }

    public Map<String,String> externalSongs() {
        return this.urlMap;
    }

    public Map<String, String> internalSongs() {
        return this.resourceLocationMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString()));
    }
}
