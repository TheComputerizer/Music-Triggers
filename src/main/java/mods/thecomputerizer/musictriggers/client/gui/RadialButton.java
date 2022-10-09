package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import javax.vecmath.Vector2f;
import java.util.List;

public class RadialButton {

    private final List<String> tooltipLines;
    private final ResourceLocation centerIcon;
    private final String centerText;
    private float centerRadius;
    private final int r;
    private final int b;
    private final int g;
    private final int a;
    private boolean hover;
    private final Vector2f centerPos;

    public RadialButton(List<String> tooltipLines, @Nullable ResourceLocation centerIcon, @Nullable String centerText) {
        this.tooltipLines = tooltipLines;
        this.centerIcon = centerIcon;
        this.centerText = centerText;
        this.r = 0;
        this.b = 0;
        this.g = 0;
        this.a = 0;
        this.hover = false;
        this.centerPos = new Vector2f(0,0);
        this.centerRadius = 0f;
    }

    public boolean draw(BufferBuilder builder, Vector2f center, float zLevel, Vector2f radius, float startAngle,
                             float endAngle, Vector2f mouse, int resolution) {
        this.hover = GuiUtil.isInCircleSlice(mouse,center,radius,startAngle,endAngle);
        for (int i = 0; i < resolution; i++)
            drawRadialSection(builder,center,zLevel,radius,startAngle,endAngle-startAngle,i,resolution);
        this.centerPos.set(GuiUtil.getVertex(center, GuiUtil.getHalfway(radius), GuiUtil.getHalfway(startAngle,endAngle)));
        return this.hover;
    }

    private void drawRadialSection(BufferBuilder buffer, Vector2f center, float zLevel, Vector2f radius, float startAngle,
                                   float angleDif, int index, int resolution) {
        float angle1 = startAngle+(index/(float)resolution)*angleDif;
        float angle2 = startAngle+((index+1)/(float)resolution)*angleDif;
        MusicTriggers.logger.info("center {} {} angles {} {}",center.x,center.y,angle1,angle2);
        Vector2f pos1In = GuiUtil.getVertex(center,radius.x,angle1);
        Vector2f pos2In = GuiUtil.getVertex(center,radius.x,angle2);
        Vector2f pos1Out = GuiUtil.getVertex(center,radius.y,angle1);
        Vector2f pos2Out = GuiUtil.getVertex(center,radius.y,angle2);
        if(this.hover) GuiUtil.setBuffer(buffer,pos1In,pos2In,pos1Out,pos2Out,zLevel,255,255,255,this.a);
        else GuiUtil.setBuffer(buffer,pos1In,pos2In,pos1Out,pos2Out,zLevel,this.r,this.g,this.b,this.a);
    }

    public void drawCenterIcon(BufferBuilder buffer, Vector2f center, Vector2f radius, float zLevel, boolean isAlone) {
        if(isAlone) this.centerPos.set(GuiUtil.getVertex(center, GuiUtil.getHalfway(radius), (float)Math.toRadians(90d)));
        if(this.centerIcon!=null)
            GuiUtil.bufferSquareTexture(buffer,this.centerPos,this.centerRadius,zLevel,this.centerIcon);
    }

    public Vector2f getCenterPos() {
        return this.centerPos;
    }

    public String getCenterText() {
        return this.centerText;
    }

    public boolean getHover() {
        return this.hover;
    }

    public List<String> getTooltipLines() {
        return this.tooltipLines;
    }

    @FunctionalInterface
    public interface CreatorFunction<L, R, S, B> {
        B apply(L list, @Nullable R icon, @Nullable S text);
    }
}
