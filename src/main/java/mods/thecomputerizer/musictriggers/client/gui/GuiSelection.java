package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.util.BiConsumer;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
        int textSlot = this.minecraft.font.lineHeight+(this.spacing*2);
        int totalHeight = this.height-(this.spacing*4)-48-this.minecraft.font.lineHeight;
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
    public void init() {
        super.init();
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
            width = this.font.width(displayName) + 8;
            String finalDisplayName = displayName;
            addSuperButton(createBottomButton(displayName, width, 2,
                    Translate.guiNumberedList(3, "button", "delete_mode", "desc"),
                    (screen, button, mode) -> {
                        ChatFormatting color = mode == 1 ? ChatFormatting.WHITE : ChatFormatting.RED;
                        this.deleteMode = color==ChatFormatting.RED;
                        button.updateDisplay(color + finalDisplayName,this.font,this);
                    }), left);
            left += (width + 16);
        }
        if(this.canSort) {
            displayName = Translate.guiGeneric(false, "button", "sort", "original");
            width = this.font.width(displayName) + 8;
            ButtonSuperType superButton = createBottomButton(displayName, width, 3,
                    Translate.guiNumberedList(3, "button", "sort", "desc"),
                    (screen, button, mode) -> {
                        this.sortType = mode - 1;
                        ChatFormatting color = mode == 1 ? ChatFormatting.WHITE : mode == 2 ? ChatFormatting.GRAY : ChatFormatting.DARK_GRAY;
                        button.updateDisplay(color + sortElements(),this.font,this);
                        Instance.setPreferredSort(mode);
                    });
            int mode = Instance.getPreferredSort();
            superButton.setMode(mode);
            this.sortType = mode-1;
            ChatFormatting color = mode == 1 ? ChatFormatting.WHITE : mode == 2 ? ChatFormatting.GRAY : ChatFormatting.DARK_GRAY;
            superButton.updateDisplay(color + sortElements(),this.font,this);
            addSuperButton(superButton,left);
            left += (superButton.getWidth() + 16);
        }
        for(ButtonSuperType button : this.bottomButtons) {
            addSuperButton(button,left);
            left += (button.getWidth() + 16);
        }
        updateSearch();
        sortElements();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if(this.canScroll) {
            if (scroll != 0) {
                if (scroll < 1) {
                    if (this.canScrollDown) {
                        this.scrollPos++;
                        this.canScrollDown = this.numElements + this.scrollPos < this.searchedElements.size();
                        return true;
                    }
                } else if (this.scrollPos > 0) {
                    this.scrollPos--;
                    this.canScrollDown = true;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if(super.mouseClicked(mouseX, mouseY, mouseButton)) return true;
        if (mouseButton == 0 && this.elementHover>=0) {
            Element element = this.searchedElements.get(this.elementHover);
            if(this.deleteMode) {
                if(element.onDelete(mouseX<=((float)this.width)/2)) {
                    playGenericClickSound();
                    this.hasEdits = true;
                    this.searchedElements.remove(element);
                    this.elementCache.remove(element);
                    save();
                    return true;
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
                    if(element.onClick(this, mouseX <= ((float)this.width)/2f))
                        for(Element otherElement : this.searchedElements)
                            if(element instanceof DualElement && element!=otherElement)
                                otherElement.setSelected(false);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int x, int y) {
        if(super.keyPressed(keyCode, x, y)) return true;
        for(Element element : this.searchedElements) {
            if(checkCopy(keyCode,element.onCopy())) return true;
            String paste = checkPaste(keyCode);
            if(!paste.isEmpty() && element.onPaste(paste)) {
                updateSearch();
                this.hasEdits = true;
                return true;
            }
            if(keyCode == GLFW.GLFW_KEY_BACKSPACE && element.onType(true, ' ')) {
                this.hasEdits = true;
                updateSearch();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char c, int mod) {
        if(super.charTyped(c, mod)) return true;
        if(SharedConstants.isAllowedChatCharacter(c)) {
            for(Element element : this.searchedElements) {
                if(element.onType(false, c)) {
                    this.hasEdits = true;
                    updateSearch();
                    return true;
                }
            }
        }
        return false;
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
            if(element instanceof MonoElement mono) {
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
    protected void drawStuff(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        MutableInt top = new MutableInt(this.spacing+24);
        GuiUtil.drawBox(new Vector3f(0,top.getValue(),0),this.width,this.height-this.spacing*2-48,black(196),this.getBlitOffset());
        Vector3f start = new Vector3f(0, top.getValue(),0);
        Vector3f end = new Vector3f(this.width, top.getValue(),0);
        GuiUtil.drawLine(start,end,white(192), 1f, this.getBlitOffset());
        drawCenteredString(matrix,this.font,this.customTitle,this.width/2,top.addAndGet(this.spacing),GuiUtil.WHITE);
        top.add(font.lineHeight+this.spacing+this.verticalSpace);
        boolean hoverAny = false;
        int index = 0;
        int bottom = this.height-(this.spacing+24);
        for(Element element : this.searchedElements) {
            if(index>=this.scrollPos) {
                if(element.renderElement(matrix,this,this.font,mouseX,mouseY,top,this.spacing,this.getBlitOffset())) {
                    hoverAny = true;
                    this.elementHover = index;
                }
                if ((bottom - top.getValue()) < (this.spacing + this.font.lineHeight)) break;
            }
            index++;
        }
        if(!hoverAny) this.elementHover = -1;
        start = new Vector3f(start.x(),this.height-this.spacing-24,0);
        end = new Vector3f(end.x(),this.height-this.spacing-24,0);
        GuiUtil.drawLine(start,end,white(192), 1f, this.getBlitOffset());
        if(this.canScroll) drawScrollBar();
        if(hoverAny) {
            for (Element element : this.searchedElements) {
                if(element==this.searchedElements.get(this.elementHover)) {
                    List<Component> hoverLines = element.getHoverLines(mouseX <= this.width / 2);
                    if(!hoverLines.isEmpty()) renderComponentTooltip(matrix,hoverLines, mouseX, mouseY);
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
        Vector3f start = new Vector3f(x, top,0);
        if(perIndex<1) perIndex = 1;
        int bottom = (int)(top+perIndex);
        Vector3f end = new Vector3f(x, this.canScrollDown ? bottom : Math.max(bottom,this.height-this.spacing-24),0);
        GuiUtil.drawLine(start,end,white(192), 2f, this.getBlitOffset());
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

        public abstract boolean renderElement(PoseStack matrix, GuiSuperType parent, Font font, int mouseX, int mouseY,
                                              MutableInt top, int spacing, float zLevel);

        public abstract String getDisplay(boolean isLeft);

        public abstract List<Component> getHoverLines(boolean isLeft);

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
        private final List<Component> hoverText;
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
            this.hoverText = hoverText.stream().map(Component::literal).collect(Collectors.toList());
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
        public boolean renderElement(PoseStack matrix, GuiSuperType parent, Font font, int mouseX, int mouseY, MutableInt top,
                                     int spacing, float zLevel) {
            boolean hover = parent.mouseHover(new Vector3f(0, top.getValue(),0),mouseX,mouseY,
                    parent.width,font.lineHeight+spacing*2);
            boolean isLeft = mouseX<=parent.width/2;
            int textColor = GuiUtil.WHITE;
            if (hover || this.multiSelect) {
                textColor = GuiUtil.makeRGBAInt(200, 200, 200, 255);
                GuiUtil.drawBox(new Vector3f(0, top.getValue(),0), parent.width, font.lineHeight+spacing*2,
                        new Vector4f(64, 64, 46, 96), zLevel);
            }
            drawCenteredString(matrix,font,getDisplay(isLeft),parent.width/2, top.addAndGet(spacing), textColor);
            top.add(spacing+font.lineHeight);
            this.hover = hover;
            return hover;
        }

        @Override
        public String getDisplay(boolean isLeft) {
            return this.display;
        }

        @Override
        public List<Component> getHoverLines(boolean isLeft) {
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
        private final List<Component> hoverTextKey;
        private final List<Component> hoverTextVal;
        private final Consumer<String> onDelete;
        private final BiConsumer<String, String> onSave;
        private boolean keySelected;

        public DualElement(String key, String val, int index, List<String> hoverTextKey, List<String> hoverTextVal,
                           Consumer<String> onDelete, BiConsumer<String, String> onSave) {
            super(index);
            this.key = key;
            this.val = val;
            this.hoverTextKey = hoverTextKey.stream().map(Component::literal).collect(Collectors.toList());
            this.hoverTextVal = hoverTextVal.stream().map(Component::literal).collect(Collectors.toList());
            this.onDelete = onDelete;
            this.onSave = onSave;
            this.keySelected = false;
        }

        @Override
        public boolean renderElement(PoseStack matrix, GuiSuperType parent, Font font, int mouseX, int mouseY, MutableInt top,
                                     int spacing, float zLevel) {
            boolean hover = parent.mouseHover(new Vector3f(0, top.getValue(),0),mouseX,mouseY,
                    parent.width,font.lineHeight+spacing*2);
            boolean isLeft = mouseX<=parent.width/2;
            int keyColor = GuiUtil.WHITE;
            int valColor = GuiUtil.WHITE;
            char keyExtra = this.isSelected && this.keySelected ? ChannelManager.blinkerChar : ' ';
            char valExtra = this.isSelected  && !this.keySelected ? ChannelManager.blinkerChar : ' ';
            if (hover) {
                if(isLeft) {
                    keyColor = GuiUtil.makeRGBAInt(200, 200, 200, 255);
                    GuiUtil.drawBox(new Vector3f(0, top.getValue(),0), parent.width/2, font.lineHeight + spacing * 2,
                            new Vector4f(64, 64, 46, 96), zLevel);
                } else {
                    valColor = GuiUtil.makeRGBAInt(200, 200, 200, 255);
                    GuiUtil.drawBox(new Vector3f(((float)parent.width)/2, top.getValue(),0), parent.width/2, font.lineHeight + spacing * 2,
                            new Vector4f(64, 64, 46, 96), zLevel);
                }
            }
            drawCenteredString(matrix,font,getDisplay(true)+keyExtra,parent.width/4, top.addAndGet(spacing), keyColor);
            drawCenteredString(matrix,font,getDisplay(false)+valExtra,parent.width-(parent.width/4), top.getValue(), valColor);
            top.add(spacing+font.lineHeight);
            this.hover = hover;
            return hover;
        }

        @Override
        public String getDisplay(boolean isLeft) {
            return isLeft ? this.key : this.val;
        }

        @Override
        public List<Component> getHoverLines(boolean isLeft) {
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
