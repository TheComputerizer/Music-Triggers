package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.vecmath.Point2i;
import javax.vecmath.Point4i;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GuiParameterList extends GuiSuperType {
    private final GuiParameters.Parameter parameter;
    private final List<String> searchedElements;
    private int numElements;
    private int verticalSpace;
    private String elementHover;
    private String selectedElement;
    private int scrollPos;
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
        this.canScrollDown = this.numElements<this.searchedElements.size();
        this.verticalSpace = (totalHeight-runningHeight)/2;
    }

    @Override
    protected void updateSearch() {
        this.searchedElements.clear();
        for(String key : this.parameter.getListValues()) {
            if (checkSearch(key))
                this.searchedElements.add(key);
        }
        if(Objects.nonNull(this.selectedElement))
            this.selectedElement = this.searchedElements.contains(this.selectedElement) ? this.selectedElement : null;
        calculateScrollSize();
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
                (screen, button, mode) -> addEntry()), left);
        left += (width + 16);
        displayName = Translate.guiGeneric(false, "button", "delete_mode");
        width = this.fontRenderer.getStringWidth(displayName) + 8;
        String finalDisplayName = displayName;
        addSuperButton(createBottomButton(displayName, width, 2,
                Translate.guiNumberedList(3, "button", "delete_mode", "desc"),
                (screen, button, mode) -> {
                    this.deleteMode = mode > 1;
                    TextFormatting color = mode == 1 ? TextFormatting.WHITE : TextFormatting.RED;
                    button.updateDisplay(color + finalDisplayName);
                }), left);
    }

    private void addEntry() {
        int i = 1;
        String temp = "temp"+i;
        while(this.parameter.getListValues().contains(temp)) {
            i++;
            temp = "temp"+i;
        }
        this.parameter.getListValues().add(temp);
        updateSearch();
        sort();
        this.hasEdits = true;
        save();
    }

    private void delete(String element) {
        this.parameter.getListValues().remove(element);
        updateSearch();
        this.hasEdits = true;
        save();
    }



    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if(scroll!=0) {
            if(scroll>1 && this.canScrollDown) {
                this.scrollPos++;
                this.canScrollDown = this.numElements+this.scrollPos<this.searchedElements.size();
            } else if(this.scrollPos>0) {
                this.scrollPos--;
                this.canScrollDown = true;
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            if(this.deleteMode) delete(this.elementHover);
            else this.selectedElement = this.elementHover;
        }
    }

    @Override
    protected void keyTyped(char c, int key) {
        super.keyTyped(c, key);
        if(isKeyValid(c, key) && Objects.nonNull(this.selectedElement))
            addCharToKey(key == Keyboard.KEY_BACK,c);
        updateSearch();
    }

    private void addCharToKey(boolean backspace,char c) {
        String newVal = this.selectedElement;
        if (backspace) {
            if (!newVal.isEmpty())
                newVal = newVal.substring(0, newVal.length() - 1);
        } else newVal += c;
        delete(this.selectedElement);
        this.parameter.getListValues().add(newVal);
        this.selectedElement = newVal;
        this.hasEdits = true;
        save();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.parent.drawScreen(mouseX, mouseY, partialTicks);
        GuiUtil.drawBox(new Point2i(0,0),this.width,this.height,new Point4i(0,0,0,128),this.zLevel);
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawStuff(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawStuff(int mouseX, int mouseY, float partialTicks) {
        int top = this.spacing + 24;
        GuiUtil.drawBox(new Point2i(0, top), this.width, this.height - this.spacing * 2 - 48, black(196), this.zLevel);
        Point2i start = new Point2i(0, top);
        Point2i end = new Point2i(this.width, top);
        GuiUtil.drawLine(start, end, white(192), 1f, this.zLevel);
        top += this.spacing;
        String header = Translate.guiGeneric(false, "parameter_list", "header") +" "+
                this.parameter.getDisplayName();
        drawCenteredString(this.fontRenderer, header, this.width/2, top, GuiUtil.WHITE);
        top += (fontRenderer.FONT_HEIGHT + this.spacing + this.verticalSpace);
        boolean hoverAny = false;
        for (String element : this.searchedElements) {
            boolean hover = mouseHover(new Point2i(0, top), mouseX, mouseY, this.width, fontRenderer.FONT_HEIGHT + this.spacing);
            int textColor = GuiUtil.WHITE;
            if (hover) {
                hoverAny = true;
                this.elementHover = element;
                textColor = GuiUtil.makeRGBAInt(192, 192, 192, 255);
                GuiUtil.drawBox(new Point2i(0, top), this.width, fontRenderer.FONT_HEIGHT + this.spacing * 2,
                            new Point4i(64, 64, 46, 96), this.zLevel);
            }
            top += this.spacing;
            char extra = Objects.nonNull(this.selectedElement) && element.matches(this.selectedElement) ?
                    ChannelManager.blinker : ' ';
            drawCenteredString(this.fontRenderer, element + extra, width/2, top, textColor);
            top += this.spacing;
        }
        if (!hoverAny) this.elementHover = null;
        start.setY(this.height - this.spacing - 24);
        end.setY(this.height - this.spacing - 24);
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
        Point2i start = new Point2i(x, top);
        Point2i end = new Point2i(x, top+scrollBarHeight);
        GuiUtil.drawLine(start,end,white(192), 2f, this.zLevel);
    }

    @Override
    protected void save() {
        this.parent.save();
        if(this.hasEdits) madeChange(true);
    }
}
