package mods.thecomputerizer.musictriggers.api.client.gui;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.client.MTClient;
import mods.thecomputerizer.musictriggers.api.client.MTClientEvents;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.DataLink;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.ParameterLink;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.log.MTLogger;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.HolderTrigger;
import mods.thecomputerizer.shadow.org.joml.Vector2d;
import mods.thecomputerizer.shadow.org.joml.Vector3d;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.MinecraftWindow;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.ScreenAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.ScreenHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.BasicWidgetGroup;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Button;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.ShapeWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.TextWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Widget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.WidgetGroup;
import mods.thecomputerizer.theimpossiblelibrary.api.client.input.KeyAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.input.KeyHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderShape;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.TextureWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceLocationAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.Circle;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.Plane;
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
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.input.KeyAPI.AlphaNum.R;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorHelper.*;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.block.Facing.Axis.Y;
import static mods.thecomputerizer.theimpossiblelibrary.api.util.MathHelper.RADIANS_45;
import static mods.thecomputerizer.theimpossiblelibrary.api.util.MathHelper.RADIANS_90;

@Getter
public class MTGUIScreen extends ScreenAPI implements LoggableAPI {
    
    public static final KeyAPI<?> GUI_KEY = KeyHelper.create(String.format("key.%1$s.gui",MODID),
            String.format("key.categories.%1$s",MODID),R);
    
    public static boolean isActive;
    
    public static MTGUIScreen constructScreen(@Nullable ScreenAPI parent, MTScreenInfo typeInfo,
            MinecraftWindow window, int scale) {
        switch(typeInfo.getType()) {
            case "channels":
            case "home": return new MTGUIScreen(parent,typeInfo,window,scale);
            case "commands":
            case "jukebox":
            case "main":
            case "potential_audio":
            case "potential_images":
            case "potential_triggers":
            case "redirect":
            case "renders":
            case "toggles": return new WrapperScreen(parent,typeInfo,window,scale);
            case "log": return new LogVisualizer(parent,typeInfo,window,scale);
            case "playback": return new PlaybackScreen(parent,typeInfo,window,scale);
            default: return new ParameterScreen(parent,typeInfo,window,scale);
        }
    }
    
    public static @Nullable DataLink findLink(MTScreenInfo parent, String type, @Nullable ParameterWrapper wrapper) {
        switch(type) {
            case "debug": return ChannelHelper.getDebug().getLink();
            case "toggles": return ChannelHelper.getClientHelper().getTogglesLink();
            default: return parent.findChannelLink(type,wrapper);
        }
    }
    
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
                case 0: return "commands";
                case 1: return "jukebox";
                case 2: return "redirect";
                case 3: return "main";
                case 4: return "channel_info";
                case 5: return "renders";
                default: break;
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
                default: break;
            }
            default: break;
        }
        return null;
    }
    
    protected static TextAPI<?> getDisplayName(String category, String name) {
        return TextHelper.getTranslated(lang(String.format("%1$s.%2$s.name",category,name)));
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
            case "channel_info":
            case "help":
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
    
    public static TextAPI<?> jukeboxName(ParameterLink link) {
        String key = String.valueOf(link.getModifiedValue("jukebox_key"));
        String value = String.valueOf(link.getModifiedValue("jukebox_value"));
        return TextHelper.getLiteral(key+" = "+value);
    }
    
    protected static String lang(String key) {
        return String.format("gui.%1$s.%2$s",MODID,key);
    }
    
    public static void open() {
        openRadial(null,MTScreenInfo.get("home"));
    }
    
    public static void open(MTGUIScreen screen) {
        ScreenHelper.open(screen);
        isActive = true;
    }
    
    protected static void openRadial(@Nullable ScreenAPI parent, MTScreenInfo info) {
        double heightRatio = RenderHelper.getCurrentHeightRatio();
        MTGUIScreen mtgui = constructScreen(parent,info,ClientHelper.getWindow(),ClientHelper.getGuiScale());
        mtgui.addRadial(heightRatio,getButtonCount(info.getType()),(index,button) -> {
            button.getShape().setColor(BLACK);
            String type = getButtonType(info.getType(),index);
            Square square = ShapeHelper.square(Y,0.25d,heightRatio);
            Widget texture = ShapeWidget.from(square,info.getIconTexture(type,false));
            Widget hoverTexture = ShapeWidget.from(square.getScaled(0.95d),info.getIconTexture(type,true));
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
        if(info.is("home")) {
            mtgui.addCenterIcon(MTClient.getLogoTexture(),heightRatio,0.25d);
            mtgui.addSquareButton(-0.6d,0.3d,"debug");
            mtgui.addSquareButton(-0.6d,-0.3d,"toggles");
            mtgui.addSquareButton(0.6d,0d,"help");
        } else {
            double radius = info.is("playback") ? 0.2d : 0.18d;
            mtgui.addCenterIcon(info.getIconTexture(false),heightRatio,radius);
        }
        ScreenHelper.open(mtgui);
        isActive = true;
    }
    
    public static TextAPI<?> parameterDesc(String name) {
        return TextHelper.getTranslated(String.format("parameter.%1$s.%2$s.desc",MODID,name));
    }
    
    public static TextAPI<?> parameterName(String name) {
        return TextHelper.getTranslated(String.format("parameter.%1$s.%2$s.name",MODID,name));
    }
    
    public static TextAPI<?> redirectName(ParameterLink link) {
        String key = String.valueOf(link.getModifiedValue("redirect_key"));
        String value = String.valueOf(link.getModifiedValue("redirect_value"));
        return TextHelper.getLiteral(key+" = "+value);
    }
    
    public static TextAPI<?> selectionDesc(String name, Object ... args) {
        return TextHelper.getTranslated(String.format("selection.%1$s.%2$s.desc",MODID,name),args);
    }
    
    public static TextAPI<?> selectionName(String name) {
        return TextHelper.getTranslated(String.format("selection.%1$s.%2$s.name",MODID,name));
    }
    
    public static void setClickFunction(MTGUIScreen screen, Button button, String type) {
        setClickFunction(screen,button,type,null);
    }
    
    public static void setClickFunction(MTGUIScreen screen, Button button, String type, @Nullable DataLink link) {
        button.setClickFunc(b -> {
            switch(type) {
                case "channels":
                case "playback": {
                    MTScreenInfo nextType = screen.typeInfo.next(type,null);
                    nextType.setChannel(ChannelHelper.getClientHelper().findFirstUserChannel(),false);
                    openRadial(screen,nextType);
                    break;
                }
                case "reload": {
                    if(screen.typeInfo.isGloballyModified()) screen.typeInfo.applyChanges();
                    else MTClientEvents.queueReload(ClientHelper.getMinecraft(),5);
                    isActive = false;
                    break;
                }
                case "reset_song": {
                    ((PlaybackScreen)screen).resetSong();
                    break;
                }
                case "skip_song": {
                    ((PlaybackScreen)screen).skipSong();
                    break;
                }
                default: {
                    MTScreenInfo nextType = screen.typeInfo.next(type,link);
                    open(constructScreen(screen,nextType,ClientHelper.getWindow(),ClientHelper.getGuiScale()));
                    break;
                }
            }
        });
    }
    
    public static TextAPI<?> triggerDesc(String name) {
        return TextHelper.getTranslated(String.format("trigger.%1$s.%2$s.desc",MODID,name));
    }
    
    public static TextAPI<?> triggerName(String name, String id, boolean holder) {
        if(id.equals("null")) id = "not_set";
        if(holder) name+=".id";
        return TextHelper.getTranslated(String.format("trigger.%1$s.%2$s",MODID,name),id);
    }
    
    public static String triggerNames(Collection<TriggerAPI> triggers) {
        if(Objects.isNull(triggers) || triggers.isEmpty()) return "[]";
        String combo = type("trigger")+"["+type("combination").toString()+" = ";
        StringJoiner joiner = new StringJoiner("+");
        for(TriggerAPI trigger : triggers) {
            String name = triggerName(trigger.getName(),trigger.getIdentifier(),trigger instanceof HolderTrigger).toString();
            if(triggers.size()==1) return name;
            joiner.add(name);
        }
        return combo+joiner+"]";
    }
    
    public static TextAPI<?> type(String type) {
        return TextHelper.getTranslated(String.format("types.%1$s.%2$s",MODID,type));
    }
    
    protected final MTScreenInfo typeInfo;
    
    public MTGUIScreen(ScreenAPI parent, MTScreenInfo typeInfo, MinecraftWindow window, int guiScale) {
        super(parent,typeInfo.getDisplayName(),window,guiScale);
        if(!typeInfo.is("home")) addBackButton();
        this.typeInfo = typeInfo;
        addApplyButton();
    }
    
    protected void addApplyButton() {
        Button apply = Button.basic(getDisplayName("button","apply"));
        double offset = 2d/1.1d;
        apply.setX(1d-(apply.getWidth()/offset));
        apply.setY(1d-(apply.getHeight()/offset));
        apply.setClickFunc(button -> this.typeInfo.applyChanges());
        if(!this.typeInfo.isGloballyModified()) apply.setVisible(false);
        addWidget(apply);
        this.typeInfo.setApplyButton(apply);
    }
    
    protected void addBackButton() {
        Button back = Button.basic(getDisplayName("button","back"));
        double offset = 2d/1.1d;
        back.setX(-1d+(back.getWidth()/offset));
        back.setY(1d-(back.getHeight()/offset));
        back.setClickFunc(button -> back());
        addWidget(back);
    }
    
    protected void addCenterIcon(ResourceLocationAPI<?> texture, double heightRatio, double radius) {
        addWidget(ShapeWidget.from(ShapeHelper.square(Y,radius*2d,heightRatio),texture));
    }
    
    protected void addFuzz(Shape shape) {
        addWidget(ShapeWidget.fuzz(shape,5));
    }
    
    protected void addRadial(double heightRatio, int slices, BiConsumer<Integer,Button> sliceSettings) {
        double radius = 0.65d;
        double innerRadius = 0.35d;
        Circle circle = ShapeHelper.circle(Y,radius,innerRadius,heightRatio);
        Circle smallRing = circle.getScaled(6d/13d);
        Circle bigRing = circle.getScaled(1.1d);
        WidgetGroup radialMenu = Button.radialGroup(circle,0d,0d,slices,getRadialOffset(slices),sliceSettings);
        addFuzz(bigRing);
        addWidget(radialMenu);
        addWidget(ShapeWidget.from(ShapeHelper.circle(Y,0.25d,heightRatio),BLACK));
        addWidget(ShapeWidget.outlineFrom(smallRing,10f));
        addWidget(ShapeWidget.outlineFrom(bigRing,10f));
    }
    
    protected void addSquareButton(double x, double y, String iconName) {
        Shape shape = ShapeHelper.square(Y,0.3d,RenderHelper.getCurrentHeightRatio());
        ShapeWidget widget = ShapeWidget.from(shape,BLACK);
        TextureWrapper texture = new TextureWrapper().setTexture(this.typeInfo.getIconTexture(iconName,false));
        TextureWrapper hoverTexture = new TextureWrapper().setTexture(this.typeInfo.getIconTexture(iconName,true));
        Widget hover = BasicWidgetGroup.from(ShapeWidget.from(shape),
                ShapeWidget.from(shape.getScaled(0.75d),hoverTexture),
                ShapeWidget.outlineFrom(shape.getScaled(1.15d),5f));
        Button button = new Button(widget,null,hover);
        button.addWidget(ShapeWidget.from(shape.getScaled(0.8d),texture));
        button.addWidget(ShapeWidget.outlineFrom(shape.getScaled(1.15d),5f));
        button.setX(x);
        button.setY(y);
        button.setHoverLines(getTooltip("button",iconName));
        setClickFunction(this,button,iconName);
        addWidget(button);
    }
    
    protected void addTextBackground(Consumer<Void> doThisBetween) {
        Plane back = ShapeHelper.plane(Y,new Vector2d(-1d,-0.9d),new Vector2d(1d,0.9d));
        addWidget(ShapeWidget.from(back,BLACK.withAlpha(0.65f)));
        doThisBetween.accept(null);
        addWidget(ShapeWidget.outlineFrom(back,5f));
    }
    
    public void addTypeTexture(double offsetX, double offsetY) {
        addTypeTexture(offsetX,offsetY,this.typeInfo.getDisplayName(),this.typeInfo.getIconTexture(false));
    }
    
    public void addTypeTexture(double offsetX, double offsetY, TextAPI<?> displayName) {
        addTypeTexture(offsetX,offsetY,displayName,this.typeInfo.getIconTexture(false));
    }
    
    public void addTypeTexture(double offsetX, double offsetY, String textureType) {
        addTypeTexture(offsetX,offsetY,this.typeInfo.getDisplayName(),this.typeInfo.getIconTexture(textureType,false));
    }
    
    public void addTypeTexture(double offsetX, double offsetY, TextAPI<?> displayName, String textureType) {
        addTypeTexture(offsetX,offsetY,displayName,this.typeInfo.getIconTexture(textureType,false));
    }
    
    public void addTypeTexture(double offsetX, double offsetY, TextAPI<?> displayName, ResourceLocationAPI<?> iconLocation) {
        Shape shape = ShapeHelper.square(Y,0.2d,RenderHelper.getCurrentHeightRatio());
        TextureWrapper texture = new TextureWrapper().setTexture(iconLocation);
        TextWidget text = TextWidget.from(displayName).setColor(GREEN);
        double textHeight = text.getHeight();
        ShapeWidget widget = ShapeWidget.from(shape,texture,0d,offsetY+0.9d-textHeight*2d-shape.getHeight()/2d);
        double width = ((Math.max(text.getWidth(),widget.getWidth()))/2d)*1.05d;
        double height = textHeight*3+widget.getHeight();
        text.setX(1d-width+offsetX);
        text.setY(0.9d-textHeight+offsetY);
        widget.setX(1d-width+offsetX);
        Shape total = ShapeHelper.plane(Y,new Vector2d(-width,0.9d-height),new Vector2d(width,0.9d));
        ShapeWidget back = ShapeWidget.from(total,BLACK,1d-width+offsetX,offsetY);
        addWidget(back);
        addWidget(text);
        addWidget(widget);
        addWidget(ShapeWidget.outlineFrom(total,3f,1d-width+offsetX,offsetY));
    }
    
    protected void autoAddTypeTexture(double offsetX) {
        switch(this.typeInfo.getType()) {
            case "channel_info":
            case "commands":
            case "debug":
            case "help":
            case "jukebox":
            case "log":
            case "playback":
            case "redirect":
            case "toggles": {
                addTypeTexture(offsetX,0d);
                break;
            }
            case "command_element": {
                addTypeTexture(offsetX,0d,"commands");
                break;
            }
            case "event": {
                addTypeTexture(offsetX,0d,this.typeInfo.getDisplayName(),MTClient.getLogoTexture());
                break;
            }
            case "from":
            case "from_list":
            case "to":
            case "toggle":
            case "to_list": {
                addTypeTexture(offsetX,0d,"toggles");
                break;
            }
            case "image_card": {
                addTypeTexture(offsetX,0d,this.typeInfo.getDisplayName("image_element"),"renders");
                break;
            }
            case "image_element":
            case "title_element": {
                addTypeTexture(offsetX,0d,"renders");
                break;
            }
            case "interrupt_handler":
            case "link":
            case "loop":
            case "universal_audio":
            case "universal_triggers": {
                addTypeTexture(offsetX,0d,"main");
                break;
            }
            case "jukebox_element": {
                addTypeTexture(offsetX,0d,"jukebox");
                break;
            }
            case "main": {
                addTypeTexture(offsetX,0d,this.typeInfo.getDisplayName(offsetX<=-1d ? "songs" : "triggers"));
                break;
            }
            case "potential_audio": {
                addTypeTexture(offsetX,0d,this.typeInfo.getDisplayName(offsetX<=-1d ? "potential_files" : "potential_redirects"),"main");
                break;
            }
            case "potential_triggers": {
                addTypeTexture(offsetX,0d,this.typeInfo.getDisplayName(offsetX<=-1d ? "registered_triggers" : "potential_triggers"),"main");
                break;
            }
            case "redirect_element": {
                addTypeTexture(offsetX,0d,"redirect");
                break;
            }
            case "renders": {
                addTypeTexture(offsetX,0d,this.typeInfo.getDisplayName(offsetX<=-1d ? "images" : "titles"));
                break;
            }
            case "title_card": {
                addTypeTexture(offsetX,0d,this.typeInfo.getDisplayName("title_element"),"renders");
                break;
            }
            default: {
                addTypeTexture(offsetX,0d,this.typeInfo.getSpecialDisplayName(),"main");
                break;
            }
        }
    }
    
    public void back() {
        ScreenHelper.open(this.parentScreen);
    }
    
    protected double getRadialOffset(int slices) {
        switch(slices) {
            case 2: return RADIANS_90;
            case 4: return RADIANS_45;
            default: return 0d;
        }
    }
    
    @Override public void logDebug(String msg, Object... args) {
        MTLogger.logDebug("GUI",this.typeInfo.getType(),msg,args);
    }
    
    @Override public void logError(String msg, Object... args) {
        MTLogger.logError("GUI",this.typeInfo.getType(),msg,args);
    }
    
    @Override public void logFatal(String msg, Object... args) {
        MTLogger.logFatal("GUI",this.typeInfo.getType(),msg,args);
    }
    
    @Override public void logInfo(String msg, Object... args) {
        MTLogger.logInfo("GUI",this.typeInfo.getType(),msg,args);
    }
    
    @Override public void logTrace(String msg, Object... args) {
        MTLogger.logTrace("GUI",this.typeInfo.getType(),msg,args);
    }
    
    @Override public void logWarn(String msg, Object... args) {
        MTLogger.logWarn("GUI",this.typeInfo.getType(),msg,args);
    }
    
    @Override public void onScreenClosed() {
        if(Objects.isNull(this.parentScreen)) isActive = false;
    }
    
    @Override public boolean shouldPauseGame() {
        return !this.typeInfo.is("playback");
    }
}