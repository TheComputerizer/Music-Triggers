package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;

import java.util.Map;

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

    @SuppressWarnings("unchecked")
    @Override
    protected void drawStuff(int mouseX, int mouseY, float partialTicks) {
        int y = this.height-20;
        int index = MusicTriggers.savedMessages.size()-1;
        while(y>20 && index>=0) {
            Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>)MusicTriggers.savedMessages.entrySet().toArray()[index];
            drawString(this.fontRenderer,entry.getKey(),20,y,entry.getValue());
            index--;
            y-=this.spacing;
        }
    }

    @Override
    protected void save() {

    }
}
