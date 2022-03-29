package mods.thecomputerizer.musictriggers.client.gui;

import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiSlider;

public class CustomSlider extends GuiSlider {

    public CustomSlider(GuiPageButtonList.GuiResponder guiResponder, int idIn, int x, int y, String nameIn, float minIn, float maxIn, float defaultValue, GuiSlider.FormatHelper formatter) {
        super(guiResponder, idIn, x, y, nameIn, minIn, maxIn, defaultValue, formatter);
    }

    public float getMax() {
        return this.max;
    }
}
