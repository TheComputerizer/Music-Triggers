package mods.thecomputerizer.musictriggers.client.gui.instance;

import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.*;
import mods.thecomputerizer.musictriggers.config.ConfigTransitions;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Transitions extends AbstractChannelConfig {
    private final Map<Integer, ConfigTransitions.Title> titleCards;
    private final Map<Integer, ConfigTransitions.Image> imageCards;
    private final List<ConfigTransitions.Title> newTitles;
    private final List<ConfigTransitions.Image> newImages;

    public Transitions(File configFile, String channelName, Map<Integer, ConfigTransitions.Title> titleCards,
                       Map<Integer, ConfigTransitions.Image> imageCards) {
        super(configFile,channelName);
        this.titleCards = titleCards;
        this.imageCards = imageCards;
        this.newTitles = new ArrayList<>();
        this.newImages = new ArrayList<>();
    }

    @Override
    public List<GuiParameters.Parameter> getParameters(GuiType type) {
        return new ArrayList<>();
    }

    @Override
    public List<GuiPage.Icon> getPageIcons(String channel) {
        return Collections.singletonList(ButtonType.TRANSITIONS.getIconButton("transitions",false));
    }

    @Override
    protected List<String> headerLines() {
        return Arrays.asList("# Please refer to the wiki page located at https://github.com/TheComputerizer/Music-Triggers/wiki/The-Basics",
                "# or the discord server located at https://discord.gg/FZHXFYp8fc",
                "# for any specific questions you might have regarding transitions","");
    }

    @Override
    protected void write(String newFilePath) {
        File file = FileUtil.generateNestedFile("config/MusicTriggers/"+newFilePath+".toml",true);
        List<String> lines = new ArrayList<>(headerLines());
        for(ConfigTransitions.Title title : this.titleCards.values())
            lines.addAll(title.getAsTomlLines(this.titleCards.size()>1));
        for(ConfigTransitions.Image image : this.imageCards.values())
            lines.addAll(image.getAsTomlLines(this.imageCards.size()>1));
        FileUtil.writeLinesToFile(file,lines,false);
    }

    public List<GuiSelection.Element> getTitleCardInstances(GuiSelection selectionScreen, String channelName) {
        return this.titleCards.keySet().stream()
                .map(index -> new GuiSelection.Element(selectionScreen, channelName, "title",
                        Translate.guiGeneric(false,"selection","title"), null, false,
                        index, (channel, title) -> Minecraft.getInstance().setScreen(
                        new GuiParameters(selectionScreen, GuiType.TITLE_INFO, selectionScreen.getInstance(),
                                "title_info", "" + index, titleCardParameters(index))),
                        (channel, title) -> removeTitleCard(index)))
                .sorted(Comparator.comparingInt(GuiSelection.Element::getIndex))
                .collect(Collectors.toList());
    }

    public List<GuiSelection.Element> getImageCardInstances(GuiSelection selectionScreen, String channelName) {
        return this.imageCards.keySet().stream()
                .map(index -> new GuiSelection.Element(selectionScreen, channelName, "image",
                        Translate.guiGeneric(false,"selection","image"), null, false,
                        index, (channel, image) -> Minecraft.getInstance().setScreen(
                        new GuiParameters(selectionScreen, GuiType.IMAGE_INFO, selectionScreen.getInstance(),
                                "image_info", "" + index, imageCardParameters(index))),
                        (channel, title) -> removeImageCard(index)))
                .sorted(Comparator.comparingInt(GuiSelection.Element::getIndex))
                .collect(Collectors.toList());
    }

    public List<GuiParameters.Parameter> titleCardParameters(int index) {
        ConfigTransitions.Title title = this.titleCards.get(index);
        return Arrays.asList(new GuiParameters.Parameter("title","triggers",null,title.getTriggers().stream()
                        .map(Trigger::getNameWithID).distinct().collect(Collectors.toList()),
                        element -> title.setTriggers(element.stream()
                                .map(name -> Trigger.createEmptyWithIDForGui(this.getChannelName(),name))
                                .distinct().collect(Collectors.toList()))),
                new GuiParameters.Parameter("title","subtitles",null,title.getTitles(),
                        title::setTitles),
                new GuiParameters.Parameter("title","subtitles",null,title.getSubTitles(),
                        title::setSubTitles),
                new GuiParameters.Parameter("title","title_color",null,title.getTitlecolor(),
                        title::setTitlecolor),
                new GuiParameters.Parameter("title","subtitle_color",null,title.getSubtitlecolor(),
                        title::setSubtitlecolor),
                new GuiParameters.Parameter("title","play_once",null,title.getPlayonce(),
                        title::setPlayonce),
                new GuiParameters.Parameter("title","vague",null,title.getVague(),
                        title::setVague));
    }

    public List<GuiParameters.Parameter> imageCardParameters(int index) {
        ConfigTransitions.Image image = this.imageCards.get(index);
        return Arrays.asList(new GuiParameters.Parameter("image","triggers",null,image.getTriggers().stream()
                        .map(Trigger::getNameWithID).distinct().collect(Collectors.toList()),
                        element -> image.setTriggers(element.stream()
                                .map(name -> Trigger.createEmptyWithIDForGui(this.getChannelName(),name))
                                .distinct().collect(Collectors.toList()))),
                new GuiParameters.Parameter("image","name",null,image.getName(),
                        image::setName),
                new GuiParameters.Parameter("image","vertical",null,image.getVertical(),
                        image::setVertical),
                new GuiParameters.Parameter("image","horizontal",null,image.getHorizontal(),
                        image::setHorizontal),
                new GuiParameters.Parameter("image","scalex",null,image.getScaleX(),
                        image::setScaleX),
                new GuiParameters.Parameter("image","scaley",null,image.getScaleY(),
                        image::setScaleY),
                new GuiParameters.Parameter("image","time",null,image.getTime(),
                        image::setTime),
                new GuiParameters.Parameter("image","locationx",null,image.getLocationX(),
                        image::setLocationX),
                new GuiParameters.Parameter("image","locationy",null,image.getLocationY(),
                        image::setLocationY),
                new GuiParameters.Parameter("image","fade_in",null,image.getFadeIn(),
                        image::setFadeIn),
                new GuiParameters.Parameter("image","fade_out",null,image.getFadeOut(),
                        image::setFadeOut),
                new GuiParameters.Parameter("image","play_once",null,image.getPlayonce(),
                        image::setPlayonce),
                new GuiParameters.Parameter("image","vague",null,image.getVague(),
                        image::setVague),
                new GuiParameters.Parameter("image","split",null,image.getSplit(),
                        image::setSplit));
    }

    public void clickAddButton(GuiSuperType parent, String group, String extra) {
        GuiSelection selectionScreen = (GuiSelection) parent;
        if (selectionScreen.isTitleView()) {
            addTitleCard();
            selectionScreen.parentUpdate();
        } else Minecraft.getInstance().setScreen(new GuiSelection(parent, GuiType.SELECTION_GENERIC,
                parent.getInstance(), getChannelName(), "images", null,
                Translate.selectionTitle("images", getChannelName()), null));
    }

    public void addTitleCard() {
        int next = this.titleCards.keySet().size();
        ConfigTransitions.Title title = new ConfigTransitions.Title();
        this.titleCards.put(next,title);
        this.newTitles.add(title);
    }

    public void removeTitleCard(int id) {
        ConfigTransitions.Title title = this.titleCards.get(id);
        this.titleCards.remove(id);
        this.newTitles.remove(title);
    }

    public void addImageCard(String name) {
        int next = this.imageCards.keySet().size();
        ConfigTransitions.Image image = new ConfigTransitions.Image();
        image.setName(name);
        this.imageCards.put(next,image);
        this.newImages.add(image);
    }

    public void removeImageCard(int id) {
        ConfigTransitions.Image image = this.imageCards.get(id);
        this.imageCards.remove(id);
        this.newImages.remove(image);
    }
}
