package mods.thecomputerizer.musictriggers.client.gui.instance;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.gui.*;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.util.TriConsumer;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class Transitions extends AbstractChannelConfig {
    private final Holder fileData;
    private int titleView;

    public Transitions(String channelName, Holder fileData) {
        super(channelName);
        this.fileData = MusicTriggers.clone(fileData);
        this.titleView = 1;
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
        List<String> lines = new ArrayList<>();
        List<String> fromData = this.fileData.toLines();
        if(fromData.stream().noneMatch(headerLines()::contains)) lines.addAll(headerLines());
        lines.addAll(this.fileData.toLines());
        FileUtil.writeLinesToFile(file,lines,false);
    }

    public List<GuiSelection.Element> getVariableElements() {
        return this.titleView==1 ? getTitleCardInstances() : getImageCardInstances();
    }

    public List<GuiSelection.Element> getTitleCardInstances() {
        List<GuiSelection.Element> elements = new ArrayList<>();
        for(Table title : this.fileData.getTablesByName("title"))
            elements.add(new GuiSelection.MonoElement("title",title.getAbsoluteIndex(),
                    Translate.guiGeneric(false,"selection","title_card"),
                    Translate.hoverLinesTitle(title),(parent) -> Minecraft.getMinecraft().displayGuiScreen(
                            new GuiParameters(parent,GuiType.PARAMETER_GENERIC, parent.getInstance(),"title",
                                    "title "+title.getAbsoluteIndex(),titleCardParameters(title),getChannelName())),
                    (id) -> this.fileData.removeTable(title)));
        return elements;
    }

    public List<GuiSelection.Element> getImageCardInstances() {
        List<GuiSelection.Element> elements = new ArrayList<>();
        for(Table image : this.fileData.getTablesByName("image"))
            elements.add(new GuiSelection.MonoElement("image",image.getAbsoluteIndex(),
                    Translate.guiGeneric(false,"selection","image_card"),
                    Translate.hoverLinesImage(image),(parent) -> Minecraft.getMinecraft().displayGuiScreen(
                    new GuiParameters(parent,GuiType.PARAMETER_GENERIC, parent.getInstance(),"title",
                            "image "+image.getAbsoluteIndex(),imageCardParameters(image),getChannelName())),
                    (id) -> this.fileData.removeTable(image)));
        return elements;
    }

    public List<GuiParameters.Parameter> titleCardParameters(Table table) {
        return Arrays.asList(new GuiParameters.Parameter("title","triggers",
                        this.fileData.getOrCreateVar(table,"triggers", new ArrayList<String>())),
                new GuiParameters.Parameter("title","titles",
                        this.fileData.getOrCreateVar(table,"titles", new ArrayList<String>())),
                new GuiParameters.Parameter("title","subtitles",
                        this.fileData.getOrCreateVar(table,"subtitles", new ArrayList<String>())),
                new GuiParameters.Parameter("title","title_color",
                        this.fileData.getOrCreateVar(table,"title_color", "red")),
                new GuiParameters.Parameter("title","subtitle_color",
                        this.fileData.getOrCreateVar(table,"subtitle_color", "white")),
                new GuiParameters.Parameter("title","centered",
                        this.fileData.getOrCreateVar(table,"centered", true)),
                new GuiParameters.Parameter("title","x",
                        this.fileData.getOrCreateVar(table,"x", -1)),
                new GuiParameters.Parameter("title","y",
                        this.fileData.getOrCreateVar(table,"y", -1)),
                new GuiParameters.Parameter("title","scale_x",
                        this.fileData.getOrCreateVar(table,"scale_x", 1f)),
                new GuiParameters.Parameter("title","scale_y",
                        this.fileData.getOrCreateVar(table,"scale_y", 1f)),
                new GuiParameters.Parameter("title","subtitle_scale",
                        this.fileData.getOrCreateVar(table,"subtitle_scale", 0.75f)),
                new GuiParameters.Parameter("title","play_once",
                        this.fileData.getOrCreateVar(table,"play_once", false)),
                new GuiParameters.Parameter("title","vague",
                        this.fileData.getOrCreateVar(table,"vague", false)));
    }

    public List<GuiParameters.Parameter> imageCardParameters(Table table) {
        return Arrays.asList(new GuiParameters.Parameter("image","triggers",
                        this.fileData.getOrCreateVar(table,"triggers", new ArrayList<String>())),
                new GuiParameters.Parameter("image","name",
                        this.fileData.getOrCreateVar(table,"name", "")),
                new GuiParameters.Parameter("image","time",
                        this.fileData.getOrCreateVar(table,"time", 100)),
                new GuiParameters.Parameter("image","fade_in",
                        this.fileData.getOrCreateVar(table,"fade_in", 20)),
                new GuiParameters.Parameter("image","fade_out",
                        this.fileData.getOrCreateVar(table,"fade_out", 20)),
                new GuiParameters.Parameter("image","opacity",
                        this.fileData.getOrCreateVar(table,"opacity", 1f)),
                new GuiParameters.Parameter("image","scale_x",
                        this.fileData.getOrCreateVar(table,"scale_x", 1f)),
                new GuiParameters.Parameter("image","scale_y",
                        this.fileData.getOrCreateVar(table,"scale_y", 1f)),
                new GuiParameters.Parameter("image","horizontal_alignment",
                        this.fileData.getOrCreateVar(table,"horizontal_alignment", "center")),
                new GuiParameters.Parameter("image","vertical_alignment",
                        this.fileData.getOrCreateVar(table,"vertical_alignment", "center")),
                new GuiParameters.Parameter("image","x",
                        this.fileData.getOrCreateVar(table,"x", 0)),
                new GuiParameters.Parameter("image","y",
                        this.fileData.getOrCreateVar(table,"y", 0)),
                new GuiParameters.Parameter("image","play_once",
                        this.fileData.getOrCreateVar(table,"play_once", false)),
                new GuiParameters.Parameter("image","vague",
                        this.fileData.getOrCreateVar(table,"vague", false)));
    }

    public ButtonSuperType[] transitionInstanceButtons(GuiSuperType grandfather) {
        List<ButtonSuperType> buttons = new ArrayList<>();
        String displayName = Translate.guiGeneric(false, "button", "add_card");
        int width = Minecraft.getMinecraft().fontRenderer.getStringWidth(displayName)+8;
        List<String> hoverText = Translate.singletonHover("button","add_card","hover");
        TriConsumer<GuiSuperType, ButtonSuperType, Integer> onClick = (parent, button, type) -> {
            if(this.titleView==1) this.fileData.addTable(null,"title");
            else Minecraft.getMinecraft().displayGuiScreen(new GuiSelection(parent,GuiType.SELECTION_GENERIC,
                    parent.getInstance(), Translate.guiGeneric(false,"selection","potential_images"),
                    false,true,() -> getPotentialImages(parent)));
            parent.parentUpdate();
        };
        buttons.add(grandfather.createBottomButton(displayName,width,1,hoverText,onClick));
        displayName = Translate.guiGeneric(false, "button", "title_view");
        width = Minecraft.getMinecraft().fontRenderer.getStringWidth(displayName)+8;
        hoverText = Translate.singletonHover("button","title_view","hover");
        onClick = (parent, button, type) -> {
            this.titleView = type;
            TextFormatting color = type == 1 ? TextFormatting.WHITE : TextFormatting.RED;
            String extraThing = type == 1 ? "title_view" : "image_view";
            String display = Translate.guiGeneric(false, "button", extraThing);
            button.updateDisplay(color + display);
            parent.parentUpdate();
        };
        buttons.add(grandfather.createBottomButton(displayName,width,2,hoverText,onClick));
        return buttons.toArray(new ButtonSuperType[]{});
    }

    public List<GuiSelection.Element> getPotentialImages(GuiSuperType grandfather) {
        List<GuiSelection.Element> elements = new ArrayList<>();
        int index = 0;
        for(String image : findImageResources()) {
            elements.add(new GuiSelection.MonoElement(image,index,image,
                    Collections.singletonList(LogUtil.injectParameters("assets/{}/textures", Constants.MODID)),
                    (parent) -> {
                        this.fileData.addVariable(this.fileData.addTable(null,"image"),"name",image);
                        grandfather.parentUpdate();
                        Minecraft.getMinecraft().displayGuiScreen(grandfather);
                    }));
            index++;
        }
        return elements;
    }

    public List<String> findImageResources() {
        try {
            URL url = MusicTriggers.class.getResource("/assets/musictriggers/textures/");
            if (Objects.nonNull(url)) {
                URI uri = url.toURI();
                Path path = null;
                if ("file".equals(uri.getScheme())) {
                    URL resource = MusicTriggers.class.getResource("/assets/musictriggers/textures");
                    if(Objects.nonNull(resource)) path = Paths.get(resource.toURI());
                }
                else {
                    FileSystem filesystem;
                    try {
                        filesystem = FileSystems.getFileSystem(uri);
                    } catch (FileSystemNotFoundException | ProviderNotFoundException ignored) {
                        filesystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                    }
                    path = filesystem.getPath("/assets/musictriggers/textures");
                }
                if(Objects.isNull(path)) return new ArrayList<>();
                return Files.walk(path,1).map(Path::getFileName).map(Objects::toString)
                        .filter(file -> file.endsWith(".png")).distinct().collect(Collectors.toList());
            }
        } catch (Exception e) {
            Constants.MAIN_LOG.error("Unable to get calculate base path for image cards with error",e);
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
