package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;

import javax.vecmath.Point2i;
import java.io.IOException;
import java.util.Map;

public class GuiLogVisualizer extends GuiSuperType {

    private int scrollPos;
    private int numElements;
    private boolean canScroll;
    private boolean canScrollUp;

    public GuiLogVisualizer(GuiSuperType parent, GuiType type, Instance configInstance) {
        super(parent, type, configInstance);
        this.spacing = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT/2;
    }

    private void calculateScrollSize() {
        this.scrollPos = 0;
        int textSlot = mc.fontRenderer.FONT_HEIGHT+this.spacing;
        int totalHeight = this.height-40;
        int runningHeight = textSlot;
        int runningTotal = 1;
        while(runningHeight+textSlot<totalHeight) {
            runningTotal++;
            runningHeight+=textSlot;
        }
        this.numElements = runningTotal;
        this.canScroll = this.numElements < MusicTriggers.savedMessages.size();
        this.canScrollUp = this.canScroll;
        if(!this.canScroll) this.numElements = MusicTriggers.savedMessages.size();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if(this.canScroll) {
            int scroll = Mouse.getEventDWheel();
            if (scroll != 0) {
                if (scroll >= 1) {
                    if (this.canScrollUp) {
                        this.scrollPos++;
                        this.canScrollUp = this.scrollPos + this.numElements + 1 < MusicTriggers.savedMessages.size();
                    }
                } else if (this.scrollPos > 0) {
                    this.scrollPos--;
                    this.canScrollUp = true;
                }
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        calculateScrollSize();
    }

    @Override
    protected void drawStuff(int mouseX, int mouseY, float partialTicks) {
        int textSpacing = this.fontRenderer.FONT_HEIGHT+this.spacing;
        int y = this.canScroll ? 32 : this.height-(this.numElements*textSpacing);
        int skip = this.canScroll ? MusicTriggers.savedMessages.size()-this.numElements-this.scrollPos : 0;
        int finish = this.numElements;
        for(Map.Entry<String, Integer> logEntry : MusicTriggers.savedMessages.entrySet()) {
            if(skip>0) skip--;
            else {
                if(finish>0) {
                    drawString(this.fontRenderer, logEntry.getKey(), 16, y, logEntry.getValue());
                    y += textSpacing;
                    finish--;
                }
            }
        }
        if(this.canScroll) drawScrollBar();
    }

    private void drawScrollBar() {
        float indices = MusicTriggers.savedMessages.size()-this.numElements;
        float perIndex = this.height/indices;
        int top = (int)(perIndex*(indices-this.scrollPos-1));
        int x = this.width-1;
        Point2i start = new Point2i(x, top);
        if(perIndex<1) perIndex = 1;
        Point2i end = new Point2i(x, (int)(top+perIndex));
        GuiUtil.drawLine(start,end,white(192), 2f, this.zLevel);
    }

    @Override
    protected void save() {

    }
}
