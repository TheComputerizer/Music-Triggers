package mods.thecomputerizer.musictriggers.client.gui;

import com.google.common.collect.Maps;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public enum GuiType {
    MAIN("main", MusicTriggers.ICON_LOCATION,null, Collections.EMPTY_LIST, 100f,
            new RadialButtonType[]{RadialButtonType.LOG,RadialButtonType.PLAYBACK,RadialButtonType.EDIT,RadialButtonType.EDIT},
            functionImpl());

    private static final Map<String, GuiType> GUI_TYPES = Maps.newHashMap();

    private final String id;
    private final ResourceLocation iconLocation;
    private final String text;
    private final List<String> tooltips;
    private final Float resolution;
    private final RadialButtonType[] buttonHolders;
    private final Circle.CreatorFunction<GuiScreen, ResourceLocation, Integer[], String, List<String>, Float,
            RadialButtonType[], Circle> circleConstructor;
    GuiType(String id, ResourceLocation iconLocation, @Nullable String text, List<String> tooltips, Float resolution,
            RadialButtonType[] buttonHolders, @Nullable Circle.CreatorFunction<GuiScreen, ResourceLocation,
            Integer[], String, List<String>, Float, RadialButtonType[], Circle> circleConstructor) {
        this.id = id;
        this.iconLocation = iconLocation;
        this.text = text;
        this.tooltips = tooltips;
        this.resolution = resolution;
        this.buttonHolders = buttonHolders;
        this.circleConstructor = circleConstructor;
    }

    public String getId() {
        return this.id;
    }

    public Circle getCircleForType(GuiScreen parentScreen, Integer[] locationParameters) {
        if(this.circleConstructor==null) return null;
        return this.circleConstructor.apply(parentScreen, this.iconLocation, locationParameters, this.text, this.tooltips,
                this.resolution, this.buttonHolders);
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

    private static Circle.CreatorFunction<GuiScreen, ResourceLocation, Integer[], String, List<String>, Float, RadialButtonType[], Circle> functionImpl() {
        return (parent, icon, location, text, tooltips, res, buttons) ->
                new Circle(parent,icon, location[0],location[1],location[2],location[3],text,tooltips,res,createButtons(buttons));
    }

    private static RadialButton[] createButtons(RadialButtonType ... types) {
        return Arrays.stream(types).map(RadialButtonType::getButton).toArray(RadialButton[]::new);
    }
}
