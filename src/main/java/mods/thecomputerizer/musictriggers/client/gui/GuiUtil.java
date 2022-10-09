package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.ResourceLocation;

import javax.vecmath.Vector2f;

public class GuiUtil {
    public static final double CIRCLE_RADIANS = Math.toRadians(360);
    public static final double HALF_CIRCLE_RADIANS = Math.toRadians(180);

    public static double distance(Vector2f vec1, Vector2f vec2) {
        return Math.sqrt(Math.pow(vec1.x-vec2.x,2)+Math.pow(vec1.y-vec2.y,2));
    }

    public static boolean isInCircle(Vector2f vec1, Vector2f vec2, Vector2f radius) {
        double distance = GuiUtil.distance(vec1, vec2);
        return distance>=radius.x && distance<=radius.y;
    }

    //must be in radians
    public static Vector2f transformAngleBoundsIfNeeded(float angleStart, float angleEnd) {
        if(angleStart-angleEnd>=CIRCLE_RADIANS) new Vector2f(0f,(float)CIRCLE_RADIANS);
        if(angleStart<0) {
            while (angleStart < 0) {
                angleStart += CIRCLE_RADIANS;
                angleEnd += CIRCLE_RADIANS;
            }
        }
        else {
            while (angleStart > CIRCLE_RADIANS) {
                angleStart -= CIRCLE_RADIANS;
                angleEnd -= CIRCLE_RADIANS;
            }
        }
        return new Vector2f(angleStart,angleEnd);
    }

    //must be in radians
    public static boolean isInCircleSlice(Vector2f vec1, Vector2f vec2, Vector2f radius, float angleStart, float angleEnd) {
        if(!isInCircle(vec1, vec2, radius)) return false;
        float x = vec1.x-vec2.x;
        float y = vec1.y-vec2.y;
        double angle = Math.atan(y/x);
        Vector2f angleVec = transformAngleBoundsIfNeeded(angleStart, angleEnd);
        return angle>=(angleVec.x+HALF_CIRCLE_RADIANS) && angle<(angleVec.y+HALF_CIRCLE_RADIANS);
    }

    public static Vector2f getVertex(Vector2f center, float radius, float angle) {
        return new Vector2f(center.x+(radius*(float)Math.cos(angle)),center.y+(radius*(float)Math.sin(angle)));
    }

    public static float getHalfway(Vector2f vec) {
        return vec.x+((vec.y-vec.x)/2f);
    }

    public static float getHalfway(float start, float end) {
        return start+((end-start)/2f);
    }

    public static void setBuffer(BufferBuilder builder, Vector2f pos1In, Vector2f pos2In, Vector2f pos1Out, Vector2f pos2Out,
                                 float zLevel, int r, int g, int b, int a) {
        builder.pos(pos1Out.x, pos1Out.y, zLevel).color(r, g, b, a).endVertex();
        builder.pos(pos1In.x, pos1In.y, zLevel).color(r, g, b, a).endVertex();
        builder.pos(pos2In.x, pos2In.y, zLevel).color(r, g, b, a).endVertex();
        builder.pos(pos2Out.x, pos2Out.y, zLevel).color(r, g, b, a).endVertex();
    }

    public static void bufferSquareTexture(BufferBuilder builder, Vector2f center, float radius, float zLevel, ResourceLocation texture) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        Vector2f ver1 = new Vector2f(center.x - (radius / 2f), center.y + (radius / 2f));
        Vector2f ver2 = new Vector2f(center.x + (radius / 2f), center.y + (radius / 2f));
        Vector2f ver3 = new Vector2f(center.x + (radius / 2f), center.y - (radius / 2f));
        Vector2f ver4 = new Vector2f(center.x - (radius / 2f), center.y - (radius / 2f));
        builder.pos(ver1.x, ver1.y, zLevel).tex(1d, 2d).endVertex();
        builder.pos(ver2.x, ver2.y, zLevel).tex(2d, 2d).endVertex();
        builder.pos(ver3.x, ver3.y, zLevel).tex(2d, 1d).endVertex();
        builder.pos(ver4.x, ver4.y, zLevel).tex(1d, 1d).endVertex();
    }
}
