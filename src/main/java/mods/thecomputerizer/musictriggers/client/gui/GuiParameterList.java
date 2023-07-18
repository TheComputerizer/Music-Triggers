package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.vecmath.Point4f;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiParameterList extends GuiSuperType {
    private final GuiParameters.Parameter parameter;
    private final List<Integer> searchedElements;
    private int numElements;
    private int verticalSpace;
    private int indexHover;
    private int selectedIndex;
    private int scrollPos;
    private boolean canScroll;
    private boolean canScrollDown;
    private boolean deleteMode;
    private boolean hasEdits;
    public GuiParameterList(GuiSuperType parent, GuiType type, Instance configInstance, GuiParameters.Parameter parameter) {
        super(parent, type, configInstance);
        this.parameter = parameter;
        this.searchedElements = new ArrayList<>();
        this.deleteMode = false;
        this.hasEdits = false;
    }

    private void sort() {
        Collections.sort(this.searchedElements);
    }

    private void calculateScrollSize() {
        this.scrollPos = 0;
        int textSlot = mc.fontRenderer.FONT_HEIGHT+this.spacing;
        int totalHeight = this.height-((this.spacing*3)+textSlot+48);
        int runningHeight = textSlot;
        int runningTotal = 1;
        while(runningHeight+textSlot<totalHeight) {
            runningTotal++;
            runningHeight+=textSlot;
        }
        this.numElements = runningTotal;
        this.canScroll = this.numElements<this.searchedElements.size();
        this.canScrollDown = this.canScroll;
        this.verticalSpace = (totalHeight-runningHeight)/2;
    }

    @Override
    protected void updateSearch() {
        int prevScroll = this.scrollPos;
        this.searchedElements.clear();
        for(int i=0;i<this.parameter.getList().size();i++) {
            if(checkSearch(getParameter(i)))
                this.searchedElements.add(i);
        }
        if(this.selectedIndex>=0)
            this.selectedIndex = this.searchedElements.contains(this.selectedIndex) ? this.selectedIndex : -1;
        calculateScrollSize();
        this.scrollPos = Math.min(prevScroll,this.canScroll ? this.searchedElements.size()-this.numElements : 0);
    }

    @Override
    public void initGui() {
        super.initGui();
        enableSearch();
        updateSearch();
        String displayName = Translate.guiGeneric(false, "button", "parameter_list", "add");
        int width = this.fontRenderer.getStringWidth(displayName) + 8;
        int left = 16;
        addSuperButton(createBottomButton(displayName, width, 1, new ArrayList<>(),
                (screen, button, mode) -> {
                    addEntry();
                    this.selectedIndex = this.parameter.getList().size()-1;
                }), left);
        left += (width + 16);
        displayName = Translate.guiGeneric(false, "button", "delete_mode");
        width = this.fontRenderer.getStringWidth(displayName) + 8;
        String finalDisplayName = displayName;
        addSuperButton(createBottomButton(displayName, width, 2,
                Translate.guiNumberedList(3, "button", "delete_mode", "desc"),
                (screen, button, mode) -> {
                    this.deleteMode = mode > 1;
                    TextFormatting color = mode == 1 ? TextFormatting.WHITE : TextFormatting.RED;
                    button.updateDisplay(color + finalDisplayName,this.fontRenderer,this);
                }), left);
    }

    private void addEntry() {
        List<String> things = this.parameter.getList();
        things.add("temp");
        this.selectedIndex = things.size()-1;
        this.parameter.setList(things);
        updateSearch();
        sort();
        this.hasEdits = true;
        save();
    }

    private void delete(int index) {
        List<String> things = this.parameter.getList();
        things.remove(index);
        this.parameter.setList(things);
        this.selectedIndex = index==this.selectedIndex ? -1 : index>this.selectedIndex ?
                this.selectedIndex : this.selectedIndex-1;
        updateSearch();
        this.hasEdits = true;
        save();
    }



    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if(this.canScroll) {
            int scroll = Mouse.getEventDWheel();
            if (scroll != 0) {
                if (scroll > 1 && this.canScrollDown) {
                    this.scrollPos++;
                    this.canScrollDown = this.numElements + this.scrollPos < this.searchedElements.size();
                } else if (this.scrollPos > 0) {
                    this.scrollPos--;
                    this.canScrollDown = true;
                }
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            if(this.deleteMode) {
                if (this.indexHover >= 0) delete(this.indexHover);
            } else if(mouseY>this.spacing+24 && mouseY<this.height-this.spacing-24) this.selectedIndex = this.indexHover;
        }
    }

    @Override
    protected void keyTyped(char c, int key) {
        super.keyTyped(c, key);
        if(this.selectedIndex>=0) {
            if(checkCopy(key,getParameter(this.selectedIndex))) return;
            String paste = checkPaste(key).replaceAll("\"","");
            if(!paste.isEmpty()) {
                updateKey(getParameter(this.selectedIndex)+paste);
                updateSearch();
                return;
            }
            if(isKeyValid(c, key) && c!='"') {
                addCharToKey(key == Keyboard.KEY_BACK, c);
                updateSearch();
            }
        }
    }

    private void addCharToKey(boolean backspace, char c) {
        String newVal = getParameter(this.selectedIndex);
        if (backspace) {
            if (!newVal.isEmpty())
                newVal = newVal.substring(0, newVal.length() - 1);
        } else newVal += c;
        updateKey(newVal);
    }

    private void updateKey(String newVal) {
        List<String> things = this.parameter.getList();
        things.set(this.selectedIndex,newVal);
        this.parameter.setList(things);
        this.hasEdits = true;
        save();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.parent.drawScreen(mouseX, mouseY, partialTicks);
        GuiUtil.drawBox(new Vec2f(0,0),this.width,this.height,new Point4f(0,0,0,128),this.zLevel);
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawStuff(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawStuff(int mouseX, int mouseY, float partialTicks) {
        int top = this.spacing + 24;
        GuiUtil.drawBox(new Vec2f(0, top), this.width, this.height - this.spacing * 2 - 48, black(196), this.zLevel);
        Vec2f start = new Vec2f(0, top);
        Vec2f end = new Vec2f(this.width, top);
        GuiUtil.drawLine(start, end, white(192), 1f, this.zLevel);
        top += this.spacing;
        String header = Translate.guiGeneric(false, "parameter_list", "header") +" "+
                this.parameter.getDisplayName();
        drawCenteredString(this.fontRenderer, header, this.width/2, top, GuiUtil.WHITE);
        top += (fontRenderer.FONT_HEIGHT + this.spacing + this.verticalSpace);
        this.indexHover = -1;
        for(int i=0;i<this.parameter.getList().size();i++) {
            if(this.searchedElements.contains(i)) {
                boolean hover = mouseHover(new Vec2f(0, top), mouseX, mouseY, this.width, fontRenderer.FONT_HEIGHT + this.spacing);
                int textColor = GuiUtil.WHITE;
                if (hover) {
                    this.indexHover = i;
                    textColor = GuiUtil.makeRGBAInt(192, 192, 192, 255);
                    GuiUtil.drawBox(new Vec2f(0, top), this.width, fontRenderer.FONT_HEIGHT + this.spacing * 2,
                            new Point4f(64, 64, 46, 96), this.zLevel);
                }
                top += this.spacing;
                char extra = this.selectedIndex>=0 && i==this.selectedIndex ? ChannelManager.blinkerChar : ' ';
                drawCenteredString(this.fontRenderer, getParameter(i) + extra, width / 2, top, textColor);
                top += this.spacing;
            }
        }
        start = new Vec2f(start.x, this.height - this.spacing - 24);
        end = new Vec2f(end.x, this.height - this.spacing - 24);
        GuiUtil.drawLine(start, end, white(192), 1f, this.zLevel);
        boolean size = this.searchedElements.size() > this.numElements;
        if (size) drawScrollBar();
    }

    private void drawScrollBar() {
        float num = this.searchedElements.size();
        float ratio = ((float)this.numElements)/ num;
        int scrollBarHeight = (int)((this.height-48)*ratio);
        int emptyHeight = this.height-48-scrollBarHeight;
        int perIndex = (int)(emptyHeight/(num-this.numElements));
        int top = 24+(perIndex*this.scrollPos);
        int x = this.width-1;
        Vec2f start = new Vec2f(x, top);
        Vec2f end = new Vec2f(x, top+scrollBarHeight);
        GuiUtil.drawLine(start,end,white(192), 2f, this.zLevel);
    }

    @Override
    protected void save() {
        this.parent.save();
        if(this.hasEdits) {
            madeChange(true);
            this.hasEdits = false;
        }
    }

    private String getParameter(int index) {
        return parameter.getList().get(index);
    }
}
