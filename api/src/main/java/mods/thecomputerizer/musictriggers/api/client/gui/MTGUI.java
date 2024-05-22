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
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.FuzzBall;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderContext;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderShape;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.TextureWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceLocationAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.Circle;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.ShapeHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.Square;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.vectors.VectorHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiConsumer;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.input.KeyAPI.AlphaNum.R;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorHelper.BLACK;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorHelper.WHITE;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.block.Facing.Axis.Y;
import static mods.thecomputerizer.theimpossiblelibrary.api.util.MathHelper.RADIANS_45;

public class MTGUI extends ScreenAPI {
    
    public static final KeyAPI<?> GUI_KEY = KeyHelper.create(String.format("key.%1$s.gui",MODID),
            String.format("key.categories.%1$s",MODID),R);
    
    public static boolean isActive;
    
    public static String getButtonType(String screen, int index) {
        switch(screen) {
            case "main": switch(index) {
                case 0: return "edit";
                case 1: return "reload";
                case 2: return "playback";
                case 3: return "log";
            }
        }
        return null;
    }
    
    public static ResourceLocationAPI<?> getIconTexture(String type, boolean hover) {
        return MTRef.res(String.format("textures/gui/%1$s_icons/%2$s.png",hover ? "black" : "white",type));
    }
    
    public static TextureWrapper getIconWrapper(String type, boolean hover) {
        return new TextureWrapper().setTexture(getIconTexture(type,hover));
    }
    
    public static Collection<TextAPI<?>> getTooltip(String category, String type) {
        Collection<TextAPI<?>> lines = new ArrayList<>();
        for(int i=1;i<=getTooltipSize(type);i++) lines.add(TextHelper.getTranslated(String.format(
                "gui.%1$s.tooltip.%2$s.%3$s.%4$d",MODID,category,type,i)));
        return lines;
    }
    
    public static int getTooltipSize(String type) {
        switch(type) {
            default: return 0;
            case "log": return 2;
            case "edit":
            case "playback": return 3;
            case "reload": return 4;
        }
    }
    
    public static void open() {
        openRadial("main");
    }
    
    private static void openRadial(String screen) {
        double heightRatio = RenderHelper.getCurrentHeightRatio();
        MTGUI mtgui = new MTGUI(screen,ClientHelper.getWindow(),ClientHelper.getGuiScale());
        if("main".equals(screen)) mtgui.addLogo(heightRatio,0.25d);
        mtgui.addRadial(heightRatio,4,(index,button) -> {
            button.getShape().setColor(BLACK);
            String type = getButtonType("main",index);
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
                    button.getShape().getShape().getWrapped()),0d,0d),hoverTexture));
            if(Objects.nonNull(type)) {
                button.setClickFunc(b -> {
                    switch(type) {
                        case "log": {
                            MTLogVisualizer.open(ClientHelper.getWindow());
                            break;
                        }
                        case "reload": {
                            MTClientEvents.queueReload(ClientHelper.getMinecraft(),5);
                            break;
                        }
                    }
                });
            }
        });
        ScreenHelper.open(mtgui);
        isActive = true;
    }
    
    protected final String type;
    protected final ShapeWidget darkBackground;
    protected final FuzzBall fuzz;
    
    public MTGUI(String type, MinecraftWindow window, int guiScale) {
        super(TextHelper.getTranslated(String.format("gui.%1$s.screen.%2$s",MODID,type)),window,guiScale);
        this.type = type;
        this.darkBackground = ShapeWidget.from(ShapeHelper.square(Y,2d,1d),BLACK.withAlpha(defaultBackgroundDarkness()));
        this.fuzz = makeFuzzGenerator(0,5,1f,1f);
    }
    
    private void addLogo(double heightRatio, double radius) {
        addWidget(ShapeWidget.from(ShapeHelper.square(Y,radius*2d,heightRatio),MTClient.getLogoTexture()));
    }
    
    private void addRadial(double heightRatio, int slices, BiConsumer<Integer,Button> sliceSettings) {
        double radius = 0.65d;
        double innerRadius = 0.35d;
        Circle circle = ShapeHelper.circle(Y,radius,innerRadius,heightRatio);
        WidgetGroup radialMenu = Button.raidalGroup(circle,0d,0d,slices,-RADIANS_45,sliceSettings);
        addWidget(radialMenu);
        addWidget(ShapeWidget.outlineFrom(circle.getScaled(6d/13d),10f));
        addWidget(ShapeWidget.outlineFrom(circle.getScaled(1.1d),10f));
    }
    
    @Override public float defaultBackgroundDarkness() {
        return 0.65f;
    }
    
    @Override public void draw(RenderContext ctx, Vector3d center, double mouseX, double mouseY) {
        this.darkBackground.draw(ctx,center,mouseX,mouseY);
        if(Objects.nonNull(this.fuzz)) this.fuzz.draw2D(ctx);
        super.draw(ctx,center,mouseX,mouseY);
    }
    
    protected FuzzBall makeFuzzGenerator(int min, int max, float minWidth, float maxWidth) {
        double heightRatio = RenderHelper.getCurrentHeightRatio();
        return ShapeHelper.circle(Y,0.715d,0.65d,heightRatio).makeFuzzBall(
                min,max,minWidth,maxWidth, () -> WHITE.withAlpha(0.75f));
    }
    
    @Override public void onScreenClosed() {
        isActive = false;
    }
    
    @Override public boolean shouldPauseGame() {
        return true;
    }
}