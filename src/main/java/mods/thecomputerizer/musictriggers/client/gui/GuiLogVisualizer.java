package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.Vec2f;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.*;

public class GuiLogVisualizer extends GuiSuperType {

    private int scrollPos;
    private int numLines;
    private boolean canScroll;
    private boolean canScrollUp;
    private final Map<Integer,Tuple<List<String>,Integer>> messageLines;
    private int filteredSize;

    public GuiLogVisualizer(GuiSuperType parent, GuiType type, Instance configInstance) {
        super(parent, type, configInstance);
        this.spacing = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT/2;
        this.messageLines = new HashMap<>();
        calculateSizes(false);
    }

    private void calculateSizes(boolean includeScrollSize) {
        this.messageLines.clear();
        this.filteredSize = 0;
        int logCounter = 0;
        Map<Integer,Tuple<String,Integer>> logEntries = MusicTriggers.getLogEntries();
        for(int i=0;i<logEntries.size();i++) {
            if(Objects.nonNull(logEntries.get(i))) {
                String message = logEntries.get(i).getFirst();
                List<String> lines = GuiUtil.splitLines(Minecraft.getMinecraft().fontRenderer, message, 16, this.width - 16);
                if(lines.size() > 0 && renderLevel(message)) {
                    this.filteredSize += lines.size();
                    this.messageLines.put(logCounter, new Tuple<>(lines, logEntries.get(i).getSecond()));
                    logCounter++;
                }
            }
        }
        if(includeScrollSize) calculateScrollSize();
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
        this.numLines = runningTotal;
        this.canScroll = this.numLines < this.filteredSize;
        this.canScrollUp = this.canScroll;
        if(!this.canScroll) this.numLines = this.filteredSize;
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
                        this.canScrollUp = this.scrollPos + this.numLines + 1 < this.filteredSize;
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
        calculateSizes(true);
    }

    @Override
    protected void drawStuff(int mouseX, int mouseY, float partialTicks) {
        int textSpacing = this.fontRenderer.FONT_HEIGHT+this.spacing;
        int y = this.canScroll ? 32 : this.height-(this.numLines*textSpacing);
        int skip = this.canScroll ? this.filteredSize-this.numLines-this.scrollPos : 0;
        int finish = this.numLines;
        for(int i=0;i<this.messageLines.size();i++) {
            if(finish<=0) break;
            for(String line : this.messageLines.get(i).getFirst()) {
                if(skip>0) skip--;
                else {
                    drawString(this.fontRenderer, line, 16, y, this.messageLines.get(i).getSecond());
                    y += textSpacing;
                    finish--;
                    if(finish <= 0) break;
                }
            }
        }
        if(this.canScroll) drawScrollBar();
    }

    private void drawScrollBar() {
        float indices = this.filteredSize-this.numLines;
        float perIndex = this.height/indices;
        int top = (int)(perIndex*(indices-this.scrollPos-1));
        int x = this.width-1;
        Vec2f start = new Vec2f(x, top);
        if(perIndex<1) perIndex = 1;
        Vec2f end = new Vec2f(x, (int)(top+perIndex));
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
