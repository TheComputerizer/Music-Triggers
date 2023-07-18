package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.util.BiConsumer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.vecmath.Point4f;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GuiSelection extends GuiSuperType {

    private final Supplier<List<Element>> elementSupplier;
    private List<Element> elementCache;
    private final Consumer<List<Element>> multiSelectHandler;
    private final List<Element> searchedElements;
    private final List<Element> selectedElements;
    private final String customTitle;
    private final boolean canDelete;
    private final boolean canSort;
    private final boolean canMultiSelect;
    private final ButtonSuperType[] bottomButtons;
    private int numElements;
    private int verticalSpace;
    private int scrollPos;
    private boolean canScroll;
    private boolean canScrollDown;
    private int elementHover;
    private int sortType;
    private boolean deleteMode;
    private boolean hasEdits;

    public GuiSelection(GuiSuperType parent, GuiType type, Instance configInstance, String customTitle, boolean canDelete,
                        boolean canSort, Supplier<List<Element>> elementSupplier, ButtonSuperType ... bottomButtons) {
        this(parent,type,configInstance,customTitle,canDelete,canSort,false,elementSupplier,null,bottomButtons);
    }

    public GuiSelection(GuiSuperType parent, GuiType type, Instance configInstance, String customTitle,
                        boolean canDelete, boolean canSort, boolean canMultiSelect,
                        Supplier<List<Element>> elementSupplier, Consumer<List<Element>> multiSelectHandler,
                        ButtonSuperType ... bottomButtons) {
        super(parent, type, configInstance);
        this.elementSupplier = elementSupplier;
        this.multiSelectHandler = multiSelectHandler;
        this.searchedElements = new ArrayList<>();
        this.selectedElements = new ArrayList<>();
        this.customTitle = customTitle;
        this.canDelete = canDelete;
        this.canSort = canSort;
        this.canMultiSelect = canMultiSelect;
        this.bottomButtons = bottomButtons;
        this.elementHover = -1;
        this.deleteMode = false;
        this.sortType = 0;
    }

    private void calculateScrollSize() {
        this.scrollPos = 0;
        int textSlot = this.mc.fontRenderer.FONT_HEIGHT+(this.spacing*2);
        int totalHeight = this.height-(this.spacing*4)-48-this.mc.fontRenderer.FONT_HEIGHT;
        int runningHeight = textSlot;
        int runningTotal = 1;
        while(runningHeight+textSlot<totalHeight) {
            runningTotal++;
            runningHeight+=textSlot;
        }
        this.numElements = runningTotal;
        this.canScroll = this.numElements < this.searchedElements.size();
        this.canScrollDown = this.canScroll;
        this.verticalSpace = (totalHeight-runningHeight)/2;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.elementCache = this.elementSupplier.get();
        enableSearch();
        int index = Integer.MIN_VALUE;
        for (Element element : this.elementCache) {
            if (index == Integer.MIN_VALUE)
                index = element.getIndex();
            else index = element.adjustOriginalIndex(index);
        }
        String displayName;
        int width;
        int left = 16;
        if(this.canDelete) {
            displayName = Translate.guiGeneric(false, "button", "delete_mode");
            width = this.fontRenderer.getStringWidth(displayName) + 8;
            String finalDisplayName = displayName;
            addSuperButton(createBottomButton(displayName, width, 2,
                    Translate.guiNumberedList(3, "button", "delete_mode", "desc"),
                    (screen, button, mode) -> {
                        TextFormatting color = mode == 1 ? TextFormatting.WHITE : TextFormatting.RED;
                        this.deleteMode = color==TextFormatting.RED;
                        button.updateDisplay(color + finalDisplayName,this.fontRenderer,this);
                    }), left);
            left += (width + 16);
        }
        if(this.canSort) {
            displayName = Translate.guiGeneric(false, "button", "sort", "original");
            width = this.fontRenderer.getStringWidth(displayName) + 8;
            ButtonSuperType superButton = createBottomButton(displayName, width, 3,
                    Translate.guiNumberedList(3, "button", "sort", "desc"),
                    (screen, button, mode) -> {
                        this.sortType = mode - 1;
                        TextFormatting color = mode == 1 ? TextFormatting.WHITE : mode == 2 ? TextFormatting.GRAY : TextFormatting.DARK_GRAY;
                        button.updateDisplay(color + sortElements(),this.fontRenderer,this);
                        Instance.setPreferredSort(mode);
                    });
            int mode = Instance.getPreferredSort();
            superButton.setMode(mode);
            this.sortType = mode-1;
            TextFormatting color = mode == 1 ? TextFormatting.WHITE : mode == 2 ? TextFormatting.GRAY : TextFormatting.DARK_GRAY;
            superButton.updateDisplay(color + sortElements(),this.fontRenderer,this);
            addSuperButton(superButton,left);
            left += (superButton.width + 16);
        }
        for(ButtonSuperType button : this.bottomButtons) {
            addSuperButton(button,left);
            left += (button.width + 16);
        }
        updateSearch();
        sortElements();
    }
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if(this.canScroll) {
            int scroll = Mouse.getEventDWheel();
            if (scroll != 0) {
                if (scroll < 1) {
                    if (this.canScrollDown) {
                        this.scrollPos++;
                        this.canScrollDown = this.numElements + this.scrollPos < this.searchedElements.size();
                    }
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
        if (mouseButton == 0 && this.elementHover>=0) {
            Element element = this.searchedElements.get(this.elementHover);
            if(this.deleteMode) {
                if(element.onDelete(mouseX<=this.width/2)) {
                    playGenericClickSound();
                    this.hasEdits = true;
                    this.searchedElements.remove(element);
                    this.elementCache.remove(element);
                    save();
                }
            } else {
                if (this.canMultiSelect) {
                    if (this.selectedElements.contains(element)) {
                        this.selectedElements.remove(element);
                        element.setSelected(false);
                    } else {
                        this.selectedElements.add(element);
                        element.setSelected(true);
                    }
                }
                else {
                    if(element.onClick(this, mouseX <= this.width / 2))
                        for(Element otherElement : this.searchedElements)
                            if(element instanceof DualElement && element!=otherElement)
                                otherElement.setSelected(false);
                }
            }
        }
    }

    @Override
    protected void keyTyped(char c, int key) {
        super.keyTyped(c, key);
        boolean shouldUpdateSearch = false;
        for(Element element : this.searchedElements) {
            if(checkCopy(key,element.onCopy())) continue;
            String paste = checkPaste(key);
            if(!paste.isEmpty() && element.onPaste(paste)) {
                shouldUpdateSearch = true;
                continue;
            }
            if(isKeyValid(c, key))
                if(element.onType(key == Keyboard.KEY_BACK, c)) shouldUpdateSearch = true;
        }
        if(shouldUpdateSearch) {
            updateSearch();
            this.hasEdits = true;
        }
    }

    @Override
    protected void updateSearch() {
        int prevScroll = this.scrollPos;
        this.searchedElements.clear();
        for(Element element : this.elementCache)
            if(checkSearch(element))
                this.searchedElements.add(element);
        calculateScrollSize();
        this.scrollPos = Math.min(prevScroll,this.canScroll ? this.searchedElements.size()-this.numElements : 0);
    }

    private boolean checkSearch(Element element) {
        if(element instanceof MonoElement)
            return checkSearch(element.getDisplay(false));
        else return checkSearch(element.getDisplay(true)) || checkSearch(element.getDisplay(false));
    }

    public String sortElements() {
        if(this.sortType<=0) {
            this.searchedElements.sort(Comparator.comparingInt(Element::getIndex));
            return Translate.guiGeneric(false,"button","sort","original");
        }
        this.searchedElements.sort(Comparator.comparing(element -> element.getDisplay(true)));
        if(this.sortType>1) {
            Collections.reverse(this.searchedElements);
            universalIsAlwaysFirst();
            return Translate.guiGeneric(false,"button","sort","reverse");
        }
        universalIsAlwaysFirst();
        return Translate.guiGeneric(false,"button","sort","alphabetical");
    }

    private void universalIsAlwaysFirst() {
        MonoElement universal = null;
        for(Element element : this.searchedElements) {
            if(element instanceof MonoElement) {
                MonoElement mono = (MonoElement)element;
                if(mono.id.matches("universal_trigger") || mono.id.matches("universal_song")) {
                    universal = mono;
                    break;
                }
            }
        }
        if(Objects.nonNull(universal)) {
            this.searchedElements.remove(universal);
            this.searchedElements.add(0,universal);
        }
    }

    @Override
    protected void drawStuff(int mouseX, int mouseY, float partialTicks) {
        MutableInt top = new MutableInt(this.spacing+24);
        GuiUtil.drawBox(new Vec2f(0,top.getValue()),this.width,this.height-this.spacing*2-48,black(196),this.zLevel);
        Vec2f start = new Vec2f(0, top.getValue());
        Vec2f end = new Vec2f(this.width, top.getValue());
        GuiUtil.drawLine(start,end,white(192), 1f, this.zLevel);
        drawCenteredString(this.fontRenderer,this.customTitle,this.width/2,top.addAndGet(this.spacing),GuiUtil.WHITE);
        top.add(fontRenderer.FONT_HEIGHT+this.spacing+this.verticalSpace);
        boolean hoverAny = false;
        int index = 0;
        int bottom = this.height-(this.spacing+24);
        for(Element element : this.searchedElements) {
            if(index>=this.scrollPos) {
                if(element.renderElement(this,this.fontRenderer,mouseX,mouseY,top,this.spacing,this.zLevel)) {
                    hoverAny = true;
                    this.elementHover = index;
                }
                if ((bottom - top.getValue()) < (this.spacing + this.fontRenderer.FONT_HEIGHT)) break;
            }
            index++;
        }
        if(!hoverAny) this.elementHover = -1;
        start = new Vec2f(start.x,this.height-this.spacing-24);
        end = new Vec2f(end.x,this.height-this.spacing-24);
        GuiUtil.drawLine(start,end,white(192), 1f, this.zLevel);
        if(this.canScroll) drawScrollBar();
        if(hoverAny) {
            for (Element element : this.searchedElements) {
                if(element==this.searchedElements.get(this.elementHover)) {
                    List<String> hoverLines = element.getHoverLines(mouseX <= this.width / 2);
                    if(!hoverLines.isEmpty()) drawHoveringText(hoverLines, mouseX, mouseY);
                }
            }
        }
    }

    private void drawScrollBar() {
        float height = this.height-(this.spacing*2)-48;
        float indices = (this.searchedElements.size()-this.numElements)+1;
        float perIndex = height/indices;
        int top = (int)(24+spacing+(perIndex*this.scrollPos));
        int x = this.width-1;
        Vec2f start = new Vec2f(x, top);
        if(perIndex<1) perIndex = 1;
        int bottom = (int)(top+perIndex);
        Vec2f end = new Vec2f(x, this.canScrollDown ? bottom : Math.max(bottom,this.height-this.spacing-24));
        GuiUtil.drawLine(start,end,white(192), 2f, this.zLevel);
    }

    @Override
    public void parentUpdate() {
        super.parentUpdate();
        if(Objects.nonNull(this.multiSelectHandler)) this.multiSelectHandler.accept(this.selectedElements);
        save();
        this.elementCache = this.elementSupplier.get();
        updateSearch();
    }

    @Override
    protected void save() {
        for(Element element: this.elementCache) element.onSave();
        if(this.hasEdits) {
            this.madeChange(true);
            this.hasEdits = false;
        }
        updateSearch();
    }

    public static abstract class Element {
        private int index;
        protected boolean isSelected;

        protected boolean hover;

        public Element(int index) {
            this.index = index;
            this.isSelected = false;
        }

        public int adjustOriginalIndex(int previous) {
            if(this.index<=previous) this.index = previous+1;
            return this.index;
        }

        public int getIndex() {
            return this.index;
        }

        public void setSelected(boolean selected) {}

        public boolean isHover() {
            return this.hover;
        }

        public abstract boolean renderElement(GuiSuperType parent, FontRenderer font, int mouseX, int mouseY,
                                              MutableInt top, int spacing, float zLevel);

        public abstract String getDisplay(boolean isLeft);

        public abstract List<String> getHoverLines(boolean isLeft);

        public abstract String onCopy();

        public abstract boolean onPaste(String pasted);

        public abstract boolean onType(boolean backspace, char c);

        public abstract boolean onClick(GuiSelection parent, boolean isLeft);

        public abstract boolean onDelete(boolean isLeft);

        public abstract void onSave();
    }

    public static class MonoElement extends Element {
        private final String id;
        private final String display;
        private final List<String> hoverText;
        private final Consumer<GuiSelection> onClick;
        private final Consumer<String> onDelete;
        private boolean multiSelect;

        public MonoElement(String id, int index, String display, Consumer<String> onDelete) {
            this(id,index,display,new ArrayList<>(),null,onDelete);
        }

        public MonoElement(String id, int index, String display, List<String> hoverText) {
            this(id,index,display,hoverText,null,null);
        }

        public MonoElement(String id, int index, String display, List<String> hoverText, Consumer<GuiSelection> onClick) {
            this(id,index,display,hoverText,onClick,null);
        }

        public MonoElement(String id, int index, String display, List<String> hoverText, Consumer<GuiSelection> onClick,
                           Consumer<String> onDelete) {
            super(index);
            this.id = id;
            this.display = display;
            this.hoverText = hoverText;
            this.onClick = onClick;
            this.onDelete = onDelete;
            this.multiSelect = false;
        }

        public String getID() {
            return this.id;
        }

        @Override
        public void setSelected(boolean selected) {
            this.multiSelect = selected;
        }

        @Override
        public boolean renderElement(GuiSuperType parent, FontRenderer font, int mouseX, int mouseY, MutableInt top,
                                     int spacing, float zLevel) {
            boolean hover = parent.mouseHover(new Vec2f(0, top.getValue()),mouseX,mouseY,
                    parent.width,font.FONT_HEIGHT+spacing*2);
            boolean isLeft = mouseX<=parent.width/2;
            int textColor = GuiUtil.WHITE;
            if (hover || this.multiSelect) {
                textColor = GuiUtil.makeRGBAInt(200, 200, 200, 255);
                GuiUtil.drawBox(new Vec2f(0, top.getValue()), parent.width, font.FONT_HEIGHT+spacing*2,
                        new Point4f(64, 64, 46, 96), zLevel);
            }
            parent.drawCenteredString(font,getDisplay(isLeft),parent.width/2, top.addAndGet(spacing), textColor);
            top.add(spacing+font.FONT_HEIGHT);
            this.hover = hover;
            return hover;
        }

        @Override
        public String getDisplay(boolean isLeft) {
            return this.display;
        }

        @Override
        public List<String> getHoverLines(boolean isLeft) {
            return this.hoverText;
        }

        @Override
        public String onCopy() {
            return "";
        }

        @Override
        public boolean onPaste(String pasted) {
            return false;
        }

        @Override
        public boolean onType(boolean backspace, char c) {
            return false;
        }

        @Override
        public boolean onClick(GuiSelection parent, boolean isLeft) {
            if(this.hover && Objects.nonNull(this.onClick)) {
                this.onClick.accept(parent);
                parent.playGenericClickSound();
            }
            this.isSelected = this.hover;
            return this.isSelected;
        }

        @Override
        public boolean onDelete(boolean isLeft) {
            if(Objects.isNull(this.onDelete)) return false;
            this.onDelete.accept(this.id);
            return true;
        }

        @Override
        public void onSave() {}
    }

    public static class DualElement extends Element {
        private String key;
        private String val;
        private final List<String> hoverTextKey;
        private final List<String> hoverTextVal;
        private final Consumer<String> onDelete;
        private final BiConsumer<String, String> onSave;
        private boolean keySelected;

        public DualElement(String key, String val, int index, List<String> hoverTextKey, List<String> hoverTextVal,
                           Consumer<String> onDelete, BiConsumer<String, String> onSave) {
            super(index);
            this.key = key;
            this.val = val;
            this.hoverTextKey = hoverTextKey;
            this.hoverTextVal = hoverTextVal;
            this.onDelete = onDelete;
            this.onSave = onSave;
            this.keySelected = false;
        }

        @Override
        public boolean renderElement(GuiSuperType parent, FontRenderer font, int mouseX, int mouseY, MutableInt top,
                                     int spacing, float zLevel) {
            boolean hover = parent.mouseHover(new Vec2f(0, top.getValue()),mouseX,mouseY,
                    parent.width,font.FONT_HEIGHT+spacing*2);
            boolean isLeft = mouseX<=parent.width/2;
            int keyColor = GuiUtil.WHITE;
            int valColor = GuiUtil.WHITE;
            char keyExtra = this.isSelected && this.keySelected ? ChannelManager.blinkerChar : ' ';
            char valExtra = this.isSelected  && !this.keySelected ? ChannelManager.blinkerChar : ' ';
            if (hover) {
                if(isLeft) {
                    keyColor = GuiUtil.makeRGBAInt(200, 200, 200, 255);
                    GuiUtil.drawBox(new Vec2f(0, top.getValue()), parent.width/2, font.FONT_HEIGHT + spacing * 2,
                            new Point4f(64, 64, 46, 96), zLevel);
                } else {
                    valColor = GuiUtil.makeRGBAInt(200, 200, 200, 255);
                    GuiUtil.drawBox(new Vec2f((float)parent.width/2, top.getValue()), parent.width/2, font.FONT_HEIGHT + spacing * 2,
                            new Point4f(64, 64, 46, 96), zLevel);
                }
            }
            parent.drawCenteredString(font,getDisplay(true)+keyExtra,parent.width/4, top.addAndGet(spacing), keyColor);
            parent.drawCenteredString(font,getDisplay(false)+valExtra,parent.width-(parent.width/4), top.getValue(), valColor);
            top.add(spacing+font.FONT_HEIGHT);
            this.hover = hover;
            return hover;
        }

        @Override
        public String getDisplay(boolean isLeft) {
            return isLeft ? this.key : this.val;
        }

        @Override
        public List<String> getHoverLines(boolean isLeft) {
            return isLeft ? this.hoverTextKey : this.hoverTextVal;
        }

        @Override
        public String onCopy() {
            return this.isSelected ? this.keySelected ? this.key : this.val : "";
        }

        @Override
        public boolean onPaste(String pasted) {
            if(this.isSelected) {
                if(this.keySelected) this.key += pasted;
                else this.val += pasted;
                return true;
            }
            return false;
        }

        @Override
        public boolean onType(boolean backspace, char c) {
            if(this.isSelected) {
                if (this.keySelected) {
                    if (backspace) {
                        if (!this.key.isEmpty()) {
                            this.key = this.key.substring(0, this.key.length() - 1);
                            return true;
                        }
                    } else {
                        this.key += c;
                        return true;
                    }
                } else {
                    if (backspace) {
                        if (!this.val.isEmpty()) {
                            this.val = this.val.substring(0, this.val.length() - 1);
                            return true;
                        }
                    } else {
                        this.val += c;
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void setSelected(boolean selected) {
            this.isSelected = selected;
        }

        @Override
        public boolean onClick(GuiSelection parent, boolean isLeft) {
            this.isSelected = this.hover;
            this.keySelected = isLeft;
            return this.isSelected;
        }

        @Override
        public boolean onDelete(boolean isLeft) {
            if(Objects.isNull(this.onDelete)) return false;
            if(isLeft) {
                this.onDelete.accept(this.key);
                return true;
            }
            this.val = "";
            return false;
        }

        @Override
        public void onSave() {
            this.onSave.accept(this.key,this.val);
        }
    }
}
