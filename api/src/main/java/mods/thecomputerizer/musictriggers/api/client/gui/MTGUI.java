package mods.thecomputerizer.musictriggers.api.client.gui;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.shadow.org.joml.Vector2d;
import mods.thecomputerizer.shadow.org.joml.Vector2f;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.RadialButtonAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.ScreenHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.WidgetCollectionAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorCache;
import mods.thecomputerizer.theimpossiblelibrary.api.client.widget.SimpleWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.widget.shapes.WidgetRadial;
import mods.thecomputerizer.theimpossiblelibrary.api.client.widget.shapes.WidgetShape;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceLocationAPI;

import javax.annotation.Nullable;
import java.util.Objects;

public class MTGUI {

    private static final ColorCache WHITE_192 = new ColorCache(255,255,255,192);

    public static SimpleWidget getColoredRing(
            float centerX, float centerY, double innerRadius, double outerRadius, ColorCache color) {
        WidgetRadial shape = getRadialShape(centerX,centerY,innerRadius,outerRadius);
        shape.setColor(color);
        return new SimpleWidget(shape);
    }

    public static @Nullable RadialButtonAPI<?> getRadialButton(WidgetRadial shape, int index, double angle, String name) {
        shape.setAnglesDegrees(angle*(double)index,angle*(double)(index+1));
        shape.getTexture().setTexture(ResourceHelper.getResource(MTRef.MODID,"textures/gui/button/"+name+".png"));
        RadialButtonAPI<?> button = ScreenHelper.getRadialButton(shape);
        if(Objects.nonNull(button)) button.setLocale(MTRef.MODID+".gui.button."+name);
        return button;
    }

    /**
     * radial buttons + 2 colored rings + center texture
     */
    public static @Nullable WidgetCollectionAPI<?> getRadialMenu(
            float centerX, float centerY, String name, String ... buttonNames) {
        WidgetCollectionAPI<?> collection = ScreenHelper.getWidgetCollection();
        if(Objects.isNull(collection)) return null;
        collection.addWidget(getSimpleTexture(getRadialShape(centerX,centerY,0d,35d),
                ResourceHelper.getResource(MTRef.MODID,"textures/gui/center/"+name+".png")));
        collection.addWidget(getColoredRing(centerX,centerY,35d,37d,WHITE_192));
        collection.addWidget(getColoredRing(centerX,centerY,110d,112d,WHITE_192));
        double angle = 360d/(double)buttonNames.length;
        int i=0;
        WidgetRadial shape = null;
        for(String buttonName : buttonNames) {
            shape = Objects.nonNull(shape) ? new WidgetRadial(shape) :
                    getRadialShape(centerX,centerY,50d,100d);
            collection.addWidget(getRadialButton(shape,i,angle,buttonName));
            i++;
        }
        return collection;
    }

    public static WidgetRadial getRadialShape(Vector2f center, Vector2d radii) {
        return getRadialShape(center.x,center.y,radii.x,radii.y);
    }

    public static WidgetRadial getRadialShape(float centerX, float centerY, double innerRadius, double outerRadius) {
        return new WidgetRadial(centerX,centerY,innerRadius,outerRadius);
    }

    public static SimpleWidget getSimpleTexture(WidgetShape shape, ResourceLocationAPI<?> location) {
        shape.getTexture().setTexture(location);
        return new SimpleWidget(shape);
    }
}