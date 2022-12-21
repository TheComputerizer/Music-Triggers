package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;

public class GuiLogVisualizer extends GuiSuperType {

    private int spacing;

    public GuiLogVisualizer(GuiSuperType parent, GuiType type, Instance configInstance) {
        super(parent, type, configInstance);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.spacing = (int)(this.fontRenderer.FONT_HEIGHT*1.5);
    }

    @Override
    protected void drawStuff(int mouseX, int mouseY, float partialTicks) {
        int y = this.height-20;
        int index = MusicTriggers.savedMessages.size()-1;
        while(y>20 && index>=0) {
            drawString(this.fontRenderer,MusicTriggers.savedMessages.get(index),20,y,14737632);
            index--;
            y-=this.spacing;
        }
    }

    @Override
    protected void save() {

    }
}
