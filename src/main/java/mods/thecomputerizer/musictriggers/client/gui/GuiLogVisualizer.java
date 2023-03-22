package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector2f;

import java.util.Map;

public class GuiLogVisualizer extends GuiSuperType {

    private int scrollPos;
    private int numElements;
    private boolean canScroll;
    private boolean canScrollUp;

    public GuiLogVisualizer(GuiSuperType parent, GuiType type, Instance configInstance) {
        super(parent, type, configInstance);
        this.spacing = Minecraft.getInstance().font.lineHeight/2;
    }

    private void calculateScrollSize() {
        this.scrollPos = 0;
        int textSlot = Minecraft.getInstance().font.lineHeight+this.spacing;
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if(this.canScroll) {
            if (scroll != 0) {
                if (scroll >= 1) {
                    if (this.canScrollUp) {
                        this.scrollPos++;
                        this.canScrollUp = this.scrollPos + this.numElements + 1 < MusicTriggers.savedMessages.size();
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
    public void init() {
        super.init();
        calculateScrollSize();
    }

    @Override
    protected void drawStuff(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        int textSpacing = this.font.lineHeight+this.spacing;
        int y = this.canScroll ? 32 : this.height-(this.numElements*textSpacing);
        int skip = this.canScroll ? MusicTriggers.savedMessages.size()-this.numElements-this.scrollPos : 0;
        int finish = this.numElements;
        for(Map.Entry<String, Integer> logEntry : MusicTriggers.savedMessages.entrySet()) {
            if(skip>0) skip--;
            else {
                if(finish>0) {
                    if(renderLevel(logEntry.getKey())) {
                        drawString(matrix, this.font, logEntry.getKey(), 16, y, logEntry.getValue());
                        y += textSpacing;
                        finish--;
                    }
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
        Vector2f start = new Vector2f(x, top);
        if(perIndex<1) perIndex = 1;
        Vector2f end = new Vector2f(x, (int)(top+perIndex));
        GuiUtil.drawLine(start,end,white(192), 2f, this.getBlitOffset());
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
