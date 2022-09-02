package mods.thecomputerizer.musictriggers.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class PNG {

    private final ResourceLocation source;
    private String horizontal;
    private String vertical;
    private int x;
    private int y;
    private float scaleX;
    private float scaleY;
    private long millis;
    private long milliStatus;

    public PNG(ResourceLocation location) throws IOException {
        if(!location.getResourcePath().contains(".png")) throw new IOException("Tried to initialize a non png file to a png object! " +
                "Make sure that you have the correct file extension on your resource location.");
        this.source = location;
        this.milliStatus = 0;
    }

    public boolean checkMillis(long partialTickMillis) {
        for(int i=0;i<partialTickMillis;i++) this.milliStatus++;
        return this.milliStatus<=this.millis;
    }

    public void loadToManager() {
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.source);
    }

    public void setHorizontal(String horizontal) {
        this.horizontal = horizontal;
    }

    public String getHorizontal() {
        return this.horizontal;
    }

    public void setVertical(String vertical) {
        this.vertical = vertical;
    }

    public String getVertical() {
        return this.vertical;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getX() {
        return x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getY() {
        return y;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public float getScaleX() {
        return scaleX;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setMillis(long millis) {
        this.millis = millis;
    }
}
