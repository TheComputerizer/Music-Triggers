package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Tuple;
import org.lwjgl.input.Mouse;

import javax.vecmath.Point2i;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class GuiLogVisualizer extends GuiSuperType {

    private int scrollPos;
    private int numElements;
    private boolean canScroll;
    private boolean canScrollUp;
    private Set<Map.Entry<Integer,Tuple<String,Integer>>> orderedMessages;
    private int filteredSize;

    public GuiLogVisualizer(GuiSuperType parent, GuiType type, Instance configInstance) {
        super(parent, type, configInstance);
        this.spacing = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT/2;
        this.orderedMessages = MusicTriggers.getLogEntries();
        this.filteredSize = calculateFilteredSize();
    }

    private int calculateFilteredSize() {
        int size = 0;
        for(Map.Entry<Integer, Tuple<String, Integer>> logEntry : this.orderedMessages)
            if(renderLevel(logEntry.getValue().getFirst())) size++;
        return size;
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
        this.canScroll = this.numElements < this.filteredSize;
        this.canScrollUp = this.canScroll;
        if(!this.canScroll) this.numElements = this.filteredSize;
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
                        this.canScrollUp = this.scrollPos + this.numElements + 1 < this.filteredSize;
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
        this.orderedMessages = MusicTriggers.getLogEntries();
        this.filteredSize = calculateFilteredSize();
        calculateScrollSize();
    }

    @Override
    protected void drawStuff(int mouseX, int mouseY, float partialTicks) {
        int textSpacing = this.fontRenderer.FONT_HEIGHT+this.spacing;
        int y = this.canScroll ? 32 : this.height-(this.numElements*textSpacing);
        int skip = this.canScroll ? this.filteredSize-this.numElements-this.scrollPos : 0;
        int finish = this.numElements;
        for(Map.Entry<Integer, Tuple<String, Integer>> logEntry : this.orderedMessages) {
            if(skip>0) skip--;
            else {
                if(finish>0) {
                    Tuple<String, Integer> message = logEntry.getValue();
                    if(renderLevel(message.getFirst())) {
                        drawString(this.fontRenderer, message.getFirst(), 16, y, message.getSecond());
                        y += textSpacing;
                        finish--;
                    }
                }
            }
        }
        if(this.canScroll) drawScrollBar();
    }

    private void drawScrollBar() {
        float indices = this.filteredSize-this.numElements;
        float perIndex = this.height/indices;
        int top = (int)(perIndex*(indices-this.scrollPos-1));
        int x = this.width-1;
        Point2i start = new Point2i(x, top);
        if(perIndex<1) perIndex = 1;
        Point2i end = new Point2i(x, (int)(top+perIndex));
        GuiUtil.drawLine(start,end,white(192), 2f, this.zLevel);
    }

    private boolean renderLevel(String level) {
        if(ConfigDebug.LOG_LEVEL.matches("DEBUG")) return true;
        if(ConfigDebug.LOG_LEVEL.matches("INFO")) return !level.contains("DEBUG");
        if(ConfigDebug.LOG_LEVEL.matches("WARN")) return !level.contains("DEBUG") && !level.contains("INFO");
        if(ConfigDebug.LOG_LEVEL.matches("ERROR")) return level.contains("ERROR") && level.contains("FATAL");
        if(ConfigDebug.LOG_LEVEL.matches("FATAL")) return level.contains("FATAL");
        return true;
    }

    @Override
    protected void save() {

    }
}
