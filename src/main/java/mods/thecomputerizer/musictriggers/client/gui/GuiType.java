package mods.thecomputerizer.musictriggers.client.gui;

import com.google.common.collect.Maps;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.theimpossiblelibrary.client.gui.RadialButton;
import mods.thecomputerizer.theimpossiblelibrary.client.gui.RadialElement;
import mods.thecomputerizer.theimpossiblelibrary.client.gui.RadialProgressBar;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec2f;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.*;

public enum GuiType {
    MAIN("main", Constants.ICON_LOCATION,null,Collections.EMPTY_LIST, 100f,0f,
            new ButtonType[]{ButtonType.LOG,ButtonType.PLAYBACK,ButtonType.EDIT,ButtonType.RELOAD}, functionImpl()),
    LOG("log_visualizer", new ButtonType[]{ButtonType.BACK,ButtonType.APPLY}),
    PLAYBACK("playback", MusicTriggers.getIcon("gui/white_icons","playback"), null, Collections.EMPTY_LIST, 100f, 0f,
            new ButtonType[]{ButtonType.BACK,ButtonType.APPLY,ButtonType.SKIP_SONG,ButtonType.RESET_SONG},
            RadialProgressBar::new, functionImpl(), 100,
            (guiScreen, bar, mousePos) -> {
                GuiPlayback playback = (GuiPlayback)guiScreen;
                playback.click(bar.mousePosToProgress(mousePos));
            }),
    EDIT("edit", new ButtonType[]{ButtonType.BACK,ButtonType.APPLY}),
    DEBUG("debug", new ButtonType[]{ButtonType.BACK,ButtonType.APPLY}),
    REGISTRATION("registration", new ButtonType[]{ButtonType.BACK,ButtonType.APPLY}),
    CHANNEL("channel", new ButtonType[]{ButtonType.BACK,ButtonType.APPLY}),
    MAIN_CONFIG("main_config", new ButtonType[]{ButtonType.BACK,ButtonType.APPLY}),
    TRANSITIONS("transitions", new ButtonType[]{ButtonType.BACK,ButtonType.APPLY}),
    COMMANDS("commands", new ButtonType[]{ButtonType.BACK,ButtonType.APPLY}),
    TOGGLES("toggles", new ButtonType[]{ButtonType.BACK,ButtonType.APPLY}),
    REDIRECT("redirect", new ButtonType[]{ButtonType.BACK,ButtonType.APPLY}),
    JUKEBOX("jukebox", new ButtonType[]{ButtonType.BACK,ButtonType.APPLY}),
    CHANNEL_INFO("channel_info", new ButtonType[]{ButtonType.BACK,ButtonType.APPLY}),
    SELECTION_GENERIC("selection_generic", new ButtonType[]{ButtonType.BACK,ButtonType.APPLY}),
    PARAMETER_GENERIC("parameter_generic", new ButtonType[]{ButtonType.BACK,ButtonType.APPLY}),
    POPUP("popup", new ButtonType[]{});

    private static final Map<String, GuiType> GUI_TYPES = Maps.newHashMap();

    private final String id;
    private final ResourceLocation iconLocation;
    private final ResourceLocation altIconLocation;
    private final ProgressCreator<Integer, Integer, Float, Integer, TriConsumer<GuiScreen, RadialProgressBar, Vec2f>, RadialProgressBar> progressBar;
    private final String text;
    private final List<String> tooltips;
    private final Float resolution;
    private final Float iconIncrease;
    private final ButtonType[] buttonHolders;
    private final RadialElement.CreatorFunction<GuiScreen, ResourceLocation, ResourceLocation, RadialProgressBar,
            Integer, Integer, Integer, Integer, Integer, String, List<String>, Float, Float, ButtonType[],
            RadialElement> circleConstructor;
    private final Integer progressRes;
    private final TriConsumer<GuiScreen, RadialProgressBar, Vec2f> progressClick;

    GuiType(String id, ButtonType[] buttonHolders) {
        this(id, null, null, null, null, null, null, buttonHolders,
                null, null, 0, null);
    }
    GuiType(String id, @Nullable ResourceLocation iconLocation, @Nullable ResourceLocation altIconLocation,
            List<String> tooltips, Float resolution, Float iconIncrease, ButtonType[] buttonHolders,
            ProgressCreator<Integer, Integer, Float, Integer, TriConsumer<GuiScreen, RadialProgressBar, Vec2f>, RadialProgressBar> progressBar,
            @Nullable RadialElement.CreatorFunction<GuiScreen, ResourceLocation, ResourceLocation, RadialProgressBar,
                    Integer, Integer, Integer, Integer, Integer, String, List<String>, Float, Float, ButtonType[],
                    RadialElement> circleConstructor, Integer progressRes, TriConsumer<GuiScreen, RadialProgressBar, Vec2f> progressClick) {
        this(id, iconLocation, altIconLocation, null, tooltips, resolution, iconIncrease, buttonHolders,
                progressBar, circleConstructor, progressRes, progressClick);
    }
    GuiType(String id, ResourceLocation iconLocation, @Nullable ResourceLocation altIconLocation,
            List<String> tooltips, Float resolution, Float iconIncrease, ButtonType[] buttonHolders,
            @Nullable RadialElement.CreatorFunction<GuiScreen, ResourceLocation, ResourceLocation, RadialProgressBar,
                    Integer, Integer, Integer, Integer, Integer, String, List<String>, Float, Float, ButtonType[],
                    RadialElement> circleConstructor) {
        this(id, iconLocation, altIconLocation, null, tooltips, resolution, iconIncrease,
                buttonHolders, null, circleConstructor, 0, null);
    }
    GuiType(String id, @Nullable ResourceLocation iconLocation, @Nullable ResourceLocation altIconLocation,
            @Nullable String text, List<String> tooltips, Float resolution, Float iconIncrease, ButtonType[] buttonHolders,
            @Nullable ProgressCreator<Integer, Integer, Float, Integer, TriConsumer<GuiScreen, RadialProgressBar, Vec2f>, RadialProgressBar> progressBar,
            @Nullable RadialElement.CreatorFunction<GuiScreen, ResourceLocation, ResourceLocation, RadialProgressBar,
                    Integer, Integer, Integer, Integer, Integer, String, List<String>, Float, Float, ButtonType[],
                    RadialElement> circleConstructor, Integer progressRes, TriConsumer<GuiScreen, RadialProgressBar, Vec2f> progressClick) {
        this.id = id;
        this.iconLocation = iconLocation;
        this.altIconLocation = altIconLocation;
        this.progressBar = progressBar;
        this.text = text;
        this.tooltips = tooltips;
        this.resolution = resolution;
        this.iconIncrease = iconIncrease;
        this.buttonHolders = buttonHolders;
        this.circleConstructor = circleConstructor;
        this.progressRes = progressRes;
        this.progressClick = progressClick;
    }

    public String getId() {
        return this.id;
    }

    public RadialElement getCircleForType(GuiScreen parentScreen, Integer[] loc, @Nullable RadialProgressBar bar) {
        if(Objects.isNull(this.circleConstructor)) return null;
        return this.circleConstructor.apply(parentScreen, this.iconLocation, this.altIconLocation, bar, loc[0], loc[1],
                loc[2], loc[3], loc[4], this.text, this.tooltips, this.resolution, this.iconIncrease, this.buttonHolders);
    }

    public RadialProgressBar getBarForType(Integer innerRadius, Integer outerRadius, Float initialProgress) {
        return this.progressBar!=null ? this.progressBar.apply(innerRadius, outerRadius, initialProgress, this.progressRes, this.progressClick) : null;
    }

    public ButtonType[] getButtonHolders() {
        return buttonHolders;
    }

    public static GuiType get(String id) {
        return GUI_TYPES.get(id);
    }

    static {
        for (GuiType type : values()) {
            if (GUI_TYPES.containsKey(type.getId()))
                throw new Error("Tried to register duplicate gui type with id "+type.getId());
            else GUI_TYPES.put(type.getId(), type);
        }
    }

    private static RadialElement.CreatorFunction<GuiScreen, ResourceLocation, ResourceLocation, RadialProgressBar,
            Integer, Integer, Integer, Integer, Integer, String, List<String>, Float, Float, ButtonType[],
            RadialElement> functionImpl() {
        return (parent, icon, alt, bar, x, y, in, out, iRad, text, tooltips, res, inc, buttons) ->
                new RadialElement(parent,icon,alt,bar,x,y,in,out,iRad,text,tooltips,res,inc,createButtons(buttons));
    }

    private static RadialButton[] createButtons(ButtonType ... types) {
        List<RadialButton> buttons = new ArrayList<>();
        for(ButtonType type : types) if(type.isRadial()) buttons.add(type.getRadialButton());
        return buttons.toArray(new RadialButton[0]);
    }

    @FunctionalInterface
    private interface ProgressCreator<I,O,P,R,C,B> {
        B apply(I inner, O outer, P percent, R res, C TriConsumer);
    }
}
