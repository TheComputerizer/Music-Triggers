package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.client.gui.RadialButton;
import mods.thecomputerizer.theimpossiblelibrary.util.client.AssetUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
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
    LOG("log",2, 0.25f, null, (screen, button) -> {
        if(ChannelManager.isButtonEnabled("log")) ((GuiSuperType)screen).saveAndDisplay(
                new GuiLogVisualizer((GuiSuperType)screen,GuiType.LOG,((GuiSuperType)screen).getInstance()));
    },RadialButton::new),
    PLAYBACK("playback",3, 0.25f, null, (screen, button) -> {
        if(ChannelManager.isButtonEnabled("playback")) ((GuiSuperType)screen).saveAndDisplay(
                new GuiPlayback((GuiSuperType)screen,GuiType.PLAYBACK,((GuiSuperType)screen).getInstance()));
    },RadialButton::new),
    EDIT("edit",3, 0.25f, null, (screen, button) -> {
        if(ChannelManager.isClientConfig() || ChannelManager.isButtonEnabled("debug") ||
                ChannelManager.isButtonEnabled("registration"))
            guiPageShortcut(screen,ChannelManager.isClientConfig());
    },RadialButton::new),
    RELOAD("reload",4, 0.25f, null,(screen, button) -> {
        if(ChannelManager.isButtonEnabled("reload"))
            ((GuiSuperType)screen).saveAndClose(true);
    },RadialButton::new),
    SKIP_SONG("skip_song",2, 0.25f, null,(screen, button) ->
            ((GuiPlayback)screen).skip(),RadialButton::new),
    RESET_SONG("reset_song",2, 0.25f, null,(screen, button) ->
            ((GuiPlayback)screen).reset(),RadialButton::new),
    DEBUG("debug",(screen, id) -> {
        if(ChannelManager.isButtonEnabled("debug"))
            guiParameterShortcut(screen,GuiType.DEBUG);
    }),
    REGISTRATION("registration",(screen, id) -> {
        if(ChannelManager.isButtonEnabled("registration"))
            guiParameterShortcut(screen,GuiType.REGISTRATION);
    }),
    CHANNEL("channel",(screen, id) -> {
        if(ChannelManager.isClientConfig())
            screen.getInstance().pageClick(GuiType.EDIT, screen, id);
    }),
    CHANNEL_INFO("channel_info",(screen, id) -> screen.getInstance().pageClick(GuiType.CHANNEL, screen, id)),
    MAIN("main",(screen, id) -> screen.getInstance().pageClick(GuiType.CHANNEL, screen, id)),
    TRANSITIONS("transitions",(screen, id) -> screen.getInstance().pageClick(GuiType.CHANNEL, screen, id)),
    COMMANDS("commands",(screen, id) -> screen.getInstance().pageClick(GuiType.CHANNEL, screen, id)),
    TOGGLES("toggles",(screen, id) -> screen.getInstance().pageClick(GuiType.CHANNEL, screen, id)),
    REDIRECT("redirect",(screen, id) -> screen.getInstance().pageClick(GuiType.CHANNEL, screen, id)),
    JUKEBOX("jukebox",(screen, id) -> screen.getInstance().pageClick(GuiType.CHANNEL, screen, id));

    /**
     * Super Button Variables
     */
    private final ButtonSuperType.CreatorFunction<Integer, Integer, Integer, Integer, Integer, String, List<String>,
            TriConsumer<GuiSuperType, ButtonSuperType, Integer>, Boolean, ButtonSuperType> normalCreatorFunction;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final String name;
    private final int hoverLines;
    private final boolean startEnabled;
    private final TriConsumer<GuiSuperType, ButtonSuperType, Integer> normalClick;
    /**
     * Radial Button Variables
     */
    private final RadialButton.CreatorFunction<List<String>, ResourceLocation, ResourceLocation, Float, String,
            BiConsumer<Screen, RadialButton>, RadialButton> radialCreatorFunction;
    private final String iconName;
    private final int descLines;
    private final Float hoverIncrease;
    private final String centerText;
    private final BiConsumer<Screen, RadialButton> radialClick;
    /**
     * Page Icon Variables
     */
    private final String id;
    private final BiConsumer<GuiSuperType,String> iconClick;

    /**
     * Super Button Constructor
     */
    ButtonType(int x, int y, int width, int height, String name, int hoverLines, Boolean startEnabled,
               TriConsumer<GuiSuperType, ButtonSuperType, Integer> normalClick,
               ButtonSuperType.CreatorFunction<Integer, Integer, Integer, Integer, Integer, String, List<String>,
                       TriConsumer<GuiSuperType, ButtonSuperType, Integer>, Boolean, ButtonSuperType> creatorFunction) {
        this(x,y,width,height,name,hoverLines,startEnabled,normalClick,creatorFunction,
                null,null,0,0f,null,null,null,null);
    }

    /**
     * Radial Button Constructor
     */
    ButtonType(String iconName, int lines, Float hoverInc, @Nullable String centerText, BiConsumer<Screen, RadialButton> radialClick,
               RadialButton.CreatorFunction<List<String>, ResourceLocation, ResourceLocation, Float, String,
                       BiConsumer<Screen, RadialButton>, RadialButton> creatorFunction) {
        this(0,0,0,0,null,0,false,null,null,
                creatorFunction,iconName,lines,hoverInc,centerText,radialClick,null,null);
    }
    /**
     * Page Ican Constructor
     */
    ButtonType(String id, BiConsumer<GuiSuperType,String> iconClick) {
        this(0,0,0,0,null,0,false,null,null,
                null,null,0,0f,null,null,
                id,iconClick);
    }

    /**
     * A mess of a constructor
     */
    ButtonType(int x, int y, int width, int height, String name, int hoverLines, Boolean startEnabled,
               TriConsumer<GuiSuperType, ButtonSuperType, Integer> normalClick,
               ButtonSuperType.CreatorFunction<Integer, Integer, Integer, Integer, Integer, String, List<String>,
                       TriConsumer<GuiSuperType, ButtonSuperType, Integer>, Boolean, ButtonSuperType> creatorFunction,
               RadialButton.CreatorFunction<List<String>, ResourceLocation, ResourceLocation, Float, String,
                       BiConsumer<Screen, RadialButton>, RadialButton> radialCreatorFunction, String iconName,
               int descLines, Float hoverIncrease, String centerText, BiConsumer<Screen, RadialButton> radialClick,
               String id, BiConsumer<GuiSuperType,String> iconClick) {
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
    }

    public boolean isNormal() {
        return this.width!=0;
    }

    public boolean isRadial() {
        return this.iconName!=null;
    }

    public ButtonSuperType getNormalButton(GuiSuperType parent) {
        int adjustedX = this.x;
        int adjustedY = this.y;
        if(adjustedX<0) adjustedX = parent.width+adjustedX-this.width;
        else if(adjustedX==69) adjustedX = (int)((((float)parent.width)/2f)-(((float)this.width)/2f));
        if(adjustedY<0) adjustedY = parent.width+adjustedY-this.width;
        else if(adjustedY==69) adjustedY = (int)((((float)parent.width)/2f)-(((float)this.width)/2f));
        return this.normalCreatorFunction.apply(adjustedX,adjustedY,this.width,this.height, 0,getNormalDisplay(),
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
                ChannelManager.isButtonEnabled(this.iconName) ?
                        Translate.guiNumberedList(this.descLines,"button",this.iconName,"desc") : Translate.disabledHover(),
                getIcons(this.iconName,false),getIcons(this.iconName,true),this.hoverIncrease,
                this.centerText,this.radialClick);
    }

    public GuiPage.Icon getIconButton(String id, boolean canDelete) {
        boolean enabled = ChannelManager.isButtonEnabled(this.id);
        if(Objects.isNull(id))
            return new GuiPage.Icon(this.id,getIcons(this.id,false),getIcons(this.id,enabled),canDelete,
                    this.iconClick);
        return new GuiPage.Icon(id,this.id,getIcons(this.id,false),getIcons(this.id,enabled),canDelete,
                this.iconClick);
    }

    private static void guiParameterShortcut(GuiSuperType screen, GuiType type) {
        Instance configInstance = screen.getInstance();
        screen.saveAndDisplay(
                new GuiParameters(screen, type, configInstance, type.getId(),
                        Translate.guiGeneric(false,"titles",type.getId(),"name"),
                        configInstance.getParameters(type, null)));
    }

    private static void guiPageShortcut(Screen screen, boolean canEdit) {
        guiPageShortcut((GuiSuperType) screen, canEdit);
    }

    private static void guiPageShortcut(GuiSuperType screen, boolean canEdit) {
        GuiType type = GuiType.EDIT;
        Instance configInstance = screen.getInstance();
        screen.saveAndDisplay(
                new GuiPage(screen, type, configInstance, type.getId(), configInstance.getPageIcons(type), canEdit));
    }

    public static ResourceLocation getIcons(String name, boolean hover) {
        String base = "textures/gui/{}/{}.png";
        return AssetUtil.getAltResource(Constants.MODID,LogUtil.injectParameters(base,"black_icons",name),
                LogUtil.injectParameters(base,"white_icons",name),hover);
    }
}
