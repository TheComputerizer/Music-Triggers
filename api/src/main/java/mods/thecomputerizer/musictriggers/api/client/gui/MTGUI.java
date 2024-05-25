package mods.thecomputerizer.musictriggers.api.client.gui;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.client.MTClient;
import mods.thecomputerizer.musictriggers.api.client.MTClientEvents;
import mods.thecomputerizer.shadow.org.joml.Vector3d;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.MinecraftWindow;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.ScreenAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.ScreenHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.BasicWidgetGroup;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Button;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.ShapeWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Widget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.WidgetGroup;
import mods.thecomputerizer.theimpossiblelibrary.api.client.input.KeyAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.input.KeyHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderShape;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceLocationAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.Circle;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.Shape;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.ShapeHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.Square;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.vectors.VectorHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiConsumer;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.input.KeyAPI.AlphaNum.R;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorHelper.BLACK;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.block.Facing.Axis.Y;
import static mods.thecomputerizer.theimpossiblelibrary.api.util.MathHelper.RADIANS_45;
import static mods.thecomputerizer.theimpossiblelibrary.api.util.MathHelper.RADIANS_90;

public class MTGUI extends ScreenAPI {
    
    public static final KeyAPI<?> GUI_KEY = KeyHelper.create(String.format("key.%1$s.gui",MODID),
            String.format("key.categories.%1$s",MODID),R);
    
    public static boolean isActive;
    
    public static int getButtonCount(String screen) {
        switch(screen) {
            case "channels": return 6;
            case "home": return 4;
            case "playback": return 2;
            default: return 1;
        }
    }
    
    public static String getButtonType(String screen, int index) {
        switch(screen) {
            case "channels": switch(index) {
                case 0: return "channel_info";
                case 1: return "commands";
                case 2: return "jukebox";
                case 3: return "main";
                case 4: return "redirect";
                case 5: return "renders";
            }
            case "home": switch(index) {
                case 0: return "log";
                case 1: return "reload";
                case 2: return "channels";
                case 3: return "playback";
                default: break;
            }
            case "playback": switch(index) {
                case 0: return "reset_song";
                case 1: return "skip_song";
            }
            default: break;
        }
        return null;
    }
    
    protected static TextAPI<?> getDisplayName(String category, String name) {
        return TextHelper.getTranslated(lang(String.format("%1$s.%2$s.name",category,name)));
    }
    
    public static ResourceLocationAPI<?> getIconTexture(String type, boolean hover) {
        return MTRef.res(String.format("textures/gui%1$sicon/%2$s.png",hover ? "/hover/" : "/",type));
    }
    
    public static Collection<TextAPI<?>> getTooltip(String category, String type) {
        Collection<TextAPI<?>> lines = new ArrayList<>();
        lines.add(getDisplayName(category, type));
        for(int i=1;i<=getTooltipSize(type);i++) lines.add(TextHelper.getTranslated(lang(String.format(
                "%1$s.%2$s.tooltip.%3$d",category,type,i))));
        return lines;
    }
    
    public static int getTooltipSize(String type) {
        switch(type) {
            default: return 0;
            case "jukebox":
            case "reset_song": return 1;
            case "channels":
            case "commands":
            case "debug":
            case "log":
            case "playback":
            case "redirect":
            case "renders":
            case "skip_song":
            case "toggles": return 2;
            case "main":
            case "reload": return 3;
        }
    }
    
    public static void setClickFunction(MTGUI screen, Button button, String type) {
        button.setClickFunc(b -> {
            switch(type) {
                case "channels": {
                    openRadial(screen,"channels");
                    break;
                }
                case "log": {
                    MTLogVisualizer.open(screen,ClientHelper.getWindow());
                    break;
                }
                case "playback": {
                    openRadial(screen,"playback");
                    break;
                }
                case "reload": {
                    MTClientEvents.queueReload(ClientHelper.getMinecraft(),5);
                    break;
                }
            }
        });
    }
    
    protected static String lang(String key) {
        return String.format("gui.%1$s.%2$s",MODID,key);
    }
    
    public static void open() {
        openRadial(null,"home");
    }
    
    protected static void openRadial(@Nullable ScreenAPI parent, String screen) {
        double heightRatio = RenderHelper.getCurrentHeightRatio();
        MTGUI mtgui = new MTGUI(parent,screen,ClientHelper.getWindow(),ClientHelper.getGuiScale());
        mtgui.addRadial(heightRatio,getButtonCount(screen),(index,button) -> {
            button.getShape().setColor(BLACK);
            String type = getButtonType(screen,index);
            Square square = ShapeHelper.square(Y,0.25d,heightRatio);
            Widget texture = ShapeWidget.from(square,getIconTexture(type,false));
            Widget hoverTexture = ShapeWidget.from(square.getScaled(0.95d),getIconTexture(type,true));
            Vector3d pos = button.getShape().getCenterForGroup(VectorHelper.zero3D());
            texture.setX(pos.x);
            texture.setY(pos.y);
            hoverTexture.setX(pos.x);
            hoverTexture.setY(pos.y);
            button.addWidget(texture);
            button.setHoverLines(getTooltip("button",type));
            button.setHover(BasicWidgetGroup.from(ShapeWidget.of(RenderShape.from(
                    button.getShape().getWrapped().getWrapped()),0d,0d),hoverTexture));
            if(Objects.nonNull(type)) setClickFunction(mtgui,button,type);
        });
        if("home".equals(screen)) mtgui.addLogo(heightRatio,0.25d);
        else mtgui.addBackButton();
        ScreenHelper.open(mtgui);
        isActive = true;
    }
    
    protected final String type;
    
    public MTGUI(ScreenAPI parent, String type, MinecraftWindow window, int guiScale) {
        super(parent,TextHelper.getTranslated(String.format("gui.%1$s.screen.%2$s",MODID,type)),window,guiScale);
        this.type = type;
    }
    
    protected void addBackButton() {
        Button back = Button.basic(getDisplayName("button","back"));
        back.setX(-1d+(back.getWidth()*0.6d));
        back.setY(1d-(back.getHeight()));
        back.setClickFunc(button -> ScreenHelper.open(this.parentScreen));
        addWidget(back);
    }
    
    protected void addFuzz(Shape shape, int maxCount) {
        addWidget(ShapeWidget.fuzz(shape,maxCount));
    }
    
    protected void addLogo(double heightRatio, double radius) {
        addWidget(ShapeWidget.from(ShapeHelper.square(Y,radius*2d,heightRatio),MTClient.getLogoTexture()));
    }
    
    protected void addRadial(double heightRatio, int slices, BiConsumer<Integer,Button> sliceSettings) {
        double radius = 0.65d;
        double innerRadius = 0.35d;
        Circle circle = ShapeHelper.circle(Y,radius,innerRadius,heightRatio);
        Circle smallRing = circle.getScaled(6d/13d);
        Circle bigRing = circle.getScaled(1.1d);
        WidgetGroup radialMenu = Button.radialGroup(circle,0d,0d,slices,getRadialOffset(slices),sliceSettings);
        addFuzz(bigRing,5);
        addWidget(radialMenu);
        addWidget(ShapeWidget.outlineFrom(smallRing,10f));
        addWidget(ShapeWidget.outlineFrom(bigRing,10f));
    }
    
    protected double getRadialOffset(int slices) {
        switch(slices) {
            case 2: return RADIANS_90;
            case 4: return RADIANS_45;
            default: return 0d;
        }
    }
    
    @Override public void onScreenClosed() {
        if(Objects.isNull(this.parentScreen)) isActive = false;
    }
    
    @Override public boolean shouldPauseGame() {
        return true;
    }
}