package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.client.gui.RadialButton;
import mods.thecomputerizer.theimpossiblelibrary.util.client.AssetUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public enum ButtonType {
    BACK(16,8,64,16,"back",0,true,(parent, button, mode) ->
            parent.saveAndClose(false),ButtonSuperType::new),
    APPLY(-16,8,96,16,"apply_changes",2,false,(parent, button, mode) ->
            parent.applyButton(),ButtonSuperType::new),
    LOG("log",2, 0.25f, null, (screen, button) ->
            ((GuiSuperType)screen).saveAndDisplay(
                new GuiLogVisualizer((GuiSuperType)screen,GuiType.LOG,((GuiSuperType)screen).getInstance())),RadialButton::new),
    PLAYBACK("playback",3, 0.25f, null, (screen, button) ->
            ((GuiSuperType)screen).saveAndDisplay(
                    new GuiPlayback((GuiSuperType)screen,GuiType.PLAYBACK,((GuiSuperType)screen).getInstance())),RadialButton::new),
    EDIT("edit",3, 0.25f, null, (screen, button) ->
            guiPageShortcut(screen,GuiType.EDIT,true),RadialButton::new),
    RELOAD("reload",4, 0.25f, null,(screen, button) ->
            ((GuiSuperType)screen).saveAndClose(true),RadialButton::new),
    SKIP_SONG("skip_song",2, 0.25f, null,(screen, button) ->
            ((GuiPlayback)screen).skip(),RadialButton::new),
    RESET_SONG("reset_song",2, 0.25f, null,(screen, button) ->
            ((GuiPlayback)screen).reset(),RadialButton::new),
    DEBUG("debug",(screen, id) -> guiParameterShortcut(screen,GuiType.DEBUG),GuiPage.Icon::new),
    REGISTRATION("registration",(screen, id) -> guiParameterShortcut(screen,GuiType.REGISTRATION),GuiPage.Icon::new),
    CHANNEL("channel",(screen, id) -> screen.getInstance().pageClick(GuiType.EDIT, screen, id),GuiPage.Icon::new),
    CHANNEL_INFO("channel_info",(screen, id) -> screen.getInstance().pageClick(GuiType.CHANNEL, screen, id),GuiPage.Icon::new),
    MAIN("main",(screen, id) -> screen.getInstance().pageClick(GuiType.CHANNEL, screen, id),GuiPage.Icon::new),
    TRANSITIONS("transitions",(screen, id) -> screen.getInstance().pageClick(GuiType.CHANNEL, screen, id),GuiPage.Icon::new),
    COMMANDS("commands",(screen, id) -> screen.getInstance().pageClick(GuiType.CHANNEL, screen, id),GuiPage.Icon::new),
    TOGGLES("toggles",(screen, id) -> screen.getInstance().pageClick(GuiType.CHANNEL, screen, id),GuiPage.Icon::new),
    REDIRECT("redirect",(screen, id) -> screen.getInstance().pageClick(GuiType.CHANNEL, screen, id),GuiPage.Icon::new),
    JUKEBOX("jukebox",(screen, id) -> screen.getInstance().pageClick(GuiType.CHANNEL, screen, id),GuiPage.Icon::new);

    //normal buttons variables
    private final ButtonSuperType.CreatorFunction<Integer, Integer, Integer, Integer, Integer, Integer, String, List<String>,
            TriConsumer<GuiSuperType, ButtonSuperType, Integer>, Boolean, ButtonSuperType> normalCreatorFunction;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final String name;
    private final int hoverLines;
    private final boolean startEnabled;
    private final TriConsumer<GuiSuperType, ButtonSuperType, Integer> normalClick;
    //radial button variables
    private final RadialButton.CreatorFunction<List<String>, ResourceLocation, ResourceLocation, Float, String,
            BiConsumer<GuiScreen, RadialButton>, RadialButton> radialCreatorFunction;
    private final String iconName;
    private final int descLines;
    private final Float hoverIncrease;
    private final String centerText;
    private final BiConsumer<GuiScreen, RadialButton> radialClick;
    //page icon variables
    private final String id;
    private final BiConsumer<GuiSuperType,String> iconClick;
    private final IconCreatorFunction<String,ResourceLocation,ResourceLocation,Boolean,BiConsumer<GuiSuperType,String>, GuiPage.Icon> iconCreator;

    ButtonType(int x, int y, int width, int height, String name, int hoverLines, Boolean startEnabled,
               TriConsumer<GuiSuperType, ButtonSuperType, Integer> normalClick,
               ButtonSuperType.CreatorFunction<Integer, Integer, Integer, Integer, Integer, Integer, String, List<String>,
                       TriConsumer<GuiSuperType, ButtonSuperType, Integer>, Boolean, ButtonSuperType> creatorFunction) {
        this(x,y,width,height,name,hoverLines,startEnabled,normalClick,creatorFunction,
                null,null,0,0f,null,null,
                null,null,null);
    }
    ButtonType(String iconName, int lines, Float hoverInc, @Nullable String centerText, BiConsumer<GuiScreen, RadialButton> radialClick,
               RadialButton.CreatorFunction<List<String>, ResourceLocation, ResourceLocation, Float, String,
                       BiConsumer<GuiScreen, RadialButton>, RadialButton> creatorFunction) {
        this(0,0,0,0,null,0,false,null,null,
                creatorFunction,iconName,lines,hoverInc,centerText,radialClick,
                null,null,null);
    }
    ButtonType(String id, BiConsumer<GuiSuperType,String> iconClick,
               IconCreatorFunction<String,ResourceLocation,ResourceLocation,Boolean,BiConsumer<GuiSuperType,String>, GuiPage.Icon> iconCreator) {
        this(0,0,0,0,null,0,false,null,null,
                null,null,0,0f,null,null,
                id,iconClick,iconCreator);
    }
    ButtonType(int x, int y, int width, int height, String name, int hoverLines, Boolean startEnabled,
               TriConsumer<GuiSuperType, ButtonSuperType, Integer> normalClick,
               ButtonSuperType.CreatorFunction<Integer, Integer, Integer, Integer, Integer, Integer, String, List<String>,
                       TriConsumer<GuiSuperType, ButtonSuperType, Integer>, Boolean, ButtonSuperType> creatorFunction,
               RadialButton.CreatorFunction<List<String>, ResourceLocation, ResourceLocation, Float, String,
                       BiConsumer<GuiScreen, RadialButton>, RadialButton> radialCreatorFunction, String iconName,
               int descLines, Float hoverIncrease, String centerText, BiConsumer<GuiScreen, RadialButton> radialClick,
               String id, BiConsumer<GuiSuperType,String> iconClick,
               IconCreatorFunction<String,ResourceLocation,ResourceLocation,Boolean,BiConsumer<GuiSuperType,String>, GuiPage.Icon> iconCreator) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.name = name;
        this.hoverLines = hoverLines;
        this.startEnabled = startEnabled;
        this.normalClick = normalClick;
        this.normalCreatorFunction = creatorFunction;
        this.radialCreatorFunction = radialCreatorFunction;
        this.iconName = iconName;
        this.descLines = descLines;
        this.hoverIncrease = hoverIncrease;
        this.centerText = centerText;
        this.radialClick = radialClick;
        this.id = id;
        this.iconClick = iconClick;
        this.iconCreator = iconCreator;
    }

    public boolean isNormal() {
        return this.width!=0;
    }

    public boolean isRadial() {
        return this.iconName!=null;
    }

    public ButtonSuperType getNormalButton(int id, GuiSuperType parent) {
        int adjustedX = this.x;
        int adjustedY = this.y;
        if(adjustedX<0) adjustedX = parent.width+adjustedX-this.width;
        else if(adjustedX==69) adjustedX = (int)((((float)parent.width)/2f)-(((float)this.width)/2f));
        if(adjustedY<0) adjustedY = parent.width+adjustedY-this.width;
        else if(adjustedY==69) adjustedY = (int)((((float)parent.width)/2f)-(((float)this.width)/2f));
        return this.normalCreatorFunction.apply(id,adjustedX,adjustedY,this.width,this.height, 0,getNormalDisplay(),
                Translate.guiNumberedList(this.hoverLines,"button",this.name,"desc"), this.normalClick,
                this.startEnabled);
    }

    public String getNormalDisplay() {
        return Translate.guiGeneric(false,"button",this.name);
    }

    public String getID() {
        return this.name;
    }

    public RadialButton getRadialButton() {
        return this.radialCreatorFunction.apply(
                Translate.guiNumberedList(this.descLines,"button",this.iconName,"desc"),
                getIcons(this.iconName,false),getIcons(this.iconName,true),this.hoverIncrease,
                this.centerText,this.radialClick);
    }

    public GuiPage.Icon getIconButton(String id, boolean canDelete) {
        if(Objects.isNull(id))
            return new GuiPage.Icon(this.id,getIcons(this.id,false),getIcons(this.id,true),canDelete,this.iconClick);
        return new GuiPage.Icon(id,this.id,getIcons(this.id,false),getIcons(this.id,true),canDelete,this.iconClick);
    }

    private static void guiParameterShortcut(GuiScreen screen, GuiType type) {
        guiParameterShortcut((GuiSuperType) screen, type);
    }

    private static void guiParameterShortcut(GuiSuperType screen, GuiType type) {
        Instance configInstance = screen.getInstance();
        screen.saveAndDisplay(
                new GuiParameters(screen, type, configInstance, type.getId(),
                        Translate.guiGeneric(false,"titles",type.getId(),"name"),
                        configInstance.getParameters(type, null)));
    }

    private static void guiPageShortcut(GuiScreen screen, GuiType type, boolean canEdit) {
        guiPageShortcut((GuiSuperType) screen, type, canEdit);
    }

    private static void guiPageShortcut(GuiSuperType screen, GuiType type, boolean canEdit) {
        Instance configInstance = screen.getInstance();
        screen.saveAndDisplay(
                new GuiPage(screen, type, configInstance, type.getId(), configInstance.getPageIcons(type), canEdit));
    }

    public static ResourceLocation getIcons(String name, boolean hover) {
        String base = "textures/gui/{}/{}.png";
        return AssetUtil.getAltResource(Constants.MODID,LogUtil.injectParameters(base,"black_icons",name),
                LogUtil.injectParameters(base,"white_icons",name),hover);
    }


    @FunctionalInterface
    private interface IconCreatorFunction<T,H,I,B,C,F> {
        F apply(T texture, H hoverTex, I id, B delete, C click);
    }
}
