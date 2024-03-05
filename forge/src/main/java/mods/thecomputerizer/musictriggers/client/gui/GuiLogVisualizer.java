package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiLogVisualizer extends GuiSuperType {

    private int scrollPos;
    private int numLines;
    private boolean canScroll;
    private boolean canScrollUp;
    private final Map<Integer,Tuple<List<String>,Integer>> messageLines;
    private final Map<Integer,Tuple<List<String>,Integer>> searchedMessageLines;
    private int filteredSize;

    public GuiLogVisualizer(GuiSuperType parent, GuiType type, Instance configInstance) {
        super(parent, type, configInstance);
        this.spacing = Minecraft.getInstance().font.lineHeight/2;
        this.messageLines = new HashMap<>();
        this.searchedMessageLines = new HashMap<>();
        this.filteredSize = 0;
    }

    private void calculateSizes() {
        this.messageLines.clear();
        int logCounter = 0;
        List<Tuple<String,Integer>> logEntries = MusicTriggers.getLogEntries();
        for(Tuple<String,Integer> logEntry : logEntries) {
            String message = logEntry.getA();
            List<String> lines = GuiUtil.splitLines(Minecraft.getInstance().font, message, 16, this.width - 16);
            if (!lines.isEmpty() && renderLevel(message)) {
                this.messageLines.put(logCounter, new Tuple<>(lines, logEntry.getB()));
                logCounter++;
            }
        }
    }

    private void calculateScrollSize() {
        this.scrollPos = 0;
        int textSlot = this.minecraft.font.lineHeight+this.spacing;
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if(this.canScroll) {
            if (scroll != 0) {
                if (scroll >= 1) {
                    if (this.canScrollUp) {
                        this.scrollPos++;
                        this.canScrollUp = this.scrollPos + this.numLines < this.filteredSize;
                        return true;
                    }
                } else if (this.scrollPos > 0) {
                    this.scrollPos--;
                    this.canScrollUp = true;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void updateSearch() {
        int prevScroll = this.scrollPos;
        this.searchedMessageLines.clear();
        this.filteredSize = 0;
        int searchCounter = 0;
        for(Map.Entry<Integer,Tuple<List<String>,Integer>> messageEntry : this.messageLines.entrySet()) {
            boolean include = false;
            for(String line : messageEntry.getValue().getA()) {
                if(checkSearch(line)) {
                    include = true;
                    break;
                }
            }
            if(include) {
                this.filteredSize+=messageEntry.getValue().getA().size();
                this.searchedMessageLines.put(searchCounter,messageEntry.getValue());
                searchCounter++;
            }
        }
        calculateScrollSize();
        this.scrollPos = Math.min(prevScroll,this.canScroll ? this.filteredSize-this.numLines : 0);
    }

    @Override
    public void init() {
        super.init();
        enableSearch();
        calculateSizes();
        updateSearch();
    }

    @Override
    protected void drawStuff(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        int textSpacing = this.font.lineHeight+this.spacing;
        int y = this.canScroll ? 32 : this.height-(this.numLines*textSpacing);
        int skip = this.canScroll ? this.filteredSize-this.numLines-this.scrollPos : 0;
        int finish = this.numLines;
        for(int i=0;i<this.searchedMessageLines.size();i++) {
            if(finish<=0) break;
            for(String line : this.searchedMessageLines.get(i).getA()) {
                if(skip>0) skip--;
                else {
                    drawString(matrix,this.font, line, 16, y, this.searchedMessageLines.get(i).getB());
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
        Vector3f start = new Vector3f(x, top,0);
        if(perIndex<1) perIndex = 1;
        Vector3f end = new Vector3f(x, (int)(top+perIndex),0);
        GuiUtil.drawLine(start,end,white(192), 2f, getBlitOffset());
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