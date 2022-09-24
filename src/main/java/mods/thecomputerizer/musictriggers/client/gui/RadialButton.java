package mods.thecomputerizer.musictriggers.client.gui;

import net.minecraft.util.ResourceLocation;

import java.util.List;

public class RadialButton {

    private final List<String> tooltipLines;
    private final ResourceLocation menuIcon;
    private final int red;
    private final int blue;
    private final int green;
    private final int alpha;

    public RadialButton(List<String> tooltipLines, ResourceLocation menuIcon, int color) {
        this.tooltipLines = tooltipLines;
        this.menuIcon = menuIcon;
        this.red = (color >> 16 & 255);
        this.blue = (color >> 8 & 255);
        this.green = (color & 255);
        this.alpha = (color >> 24 & 255);
    }

    public List<String> getTooltipLines() {
        return this.tooltipLines;
    }

    public ResourceLocation getMenuIcon() {
        return menuIcon;
    }

    public int getRed() {
        return red;
    }

    public int getBlue() {
        return this.blue;
    }

    public int getGreen() {
        return green;
    }

    public int getAlpha() {
        return alpha;
    }
}
