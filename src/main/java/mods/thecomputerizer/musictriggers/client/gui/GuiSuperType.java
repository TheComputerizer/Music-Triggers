package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.client.EventsClient;
import net.minecraft.client.gui.GuiScreen;

public class GuiSuperType extends GuiScreen {

    private final GuiScreen parent;
    private final GuiType type;
    private Circle circleButton;


    public GuiSuperType(GuiScreen parent, GuiType type) {
        this.parent = parent;
        this.type = type;
    }

    private Integer[] setCenterCircle() {
        return new Integer[]{(int)(((float)this.width)/2f),(int)(((float)this.height)/2f),50,75};
    }

    @Override
    public void initGui() {
        EventsClient.renderDebug = false;
        this.circleButton = type.getCircleForType(parent, setCenterCircle());
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        if(this.circleButton!=null) circleButton.render(this.zLevel,mouseX,mouseY);
    }

    @Override
    public void onGuiClosed() {
        EventsClient.renderDebug = true;
    }
}
