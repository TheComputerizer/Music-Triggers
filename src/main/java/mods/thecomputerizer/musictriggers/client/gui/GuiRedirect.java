package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.musictriggers.client.gui.instance.Redirect;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GuiRedirect extends GuiSuperType {
    private final Redirect redirectInstance;
    private final Map<String, String> internalRedirectMap;
    private final Map<String, String> externalRedirectMap;
    private final List<String> allKeys;
    private final List<String> newKeys;
    private int numElements;
    private int verticalSpace;
    private String elementHover;
    private boolean hoverKey;
    private String selectedElement;
    private boolean isKeySelected;
    private int scrollPos;
    private boolean canScrollDown;
    private ButtonSuperType toggleMode;
    private boolean deleteMode;
    private ButtonSuperType viewMode;
    private boolean viewAll;
    private boolean hasEdits;
    public GuiRedirect(GuiSuperType parent, GuiType type, Instance configInstance, Redirect redirect,
                       Map<String, String> internalRedirectMap, Map<String, String> externalRedirectMap) {
        super(parent, type, configInstance);
        this.redirectInstance = redirect;
        this.internalRedirectMap = internalRedirectMap;
        this.externalRedirectMap = externalRedirectMap;
        this.allKeys = makeSortedList(internalRedirectMap.keySet(),externalRedirectMap.keySet());
        this.newKeys = new ArrayList<>();
        this.deleteMode = false;
        this.viewAll = true;
        this.hoverKey = false;
        this.isKeySelected = true;
    }

    private List<String> makeSortedList(Collection<String> internal, Collection<String> external) {
        return Stream.of(internal,external).flatMap(Collection::stream).sorted().collect(Collectors.toList());
    }

    private void sort() {
        Collections.sort(this.allKeys);
        Collections.sort(this.newKeys);
    }

    @Override
    public void init(@Nonnull Minecraft mc, int width, int height) {
        super.init(mc, width, height);
        int textSlot = mc.font.lineHeight+this.spacing;
        int totalHeight = this.height-((this.spacing*3)+textSlot+48);
        int runningHeight = textSlot;
        int runningTotal = 1;
        while(runningHeight+textSlot<totalHeight) {
            runningTotal++;
            runningHeight+=textSlot;
        }
        this.numElements = runningTotal;
        this.canScrollDown = (this.viewAll && this.numElements<this.allKeys.size()) ||
                (!this.viewAll && this.numElements<this.newKeys.size());
        this.verticalSpace = (totalHeight-runningHeight)/2;
    }

    @Override
    public void init() {
        super.init();
        String displayName = Translate.guiGeneric(false,"button","redirect","add_external");
        int width = this.font.width(displayName)+4;
        int left = 96;
        addTopButton(left, displayName, width, new ArrayList<>(),
                (screen, button) -> addExternal(),this);
        left+=(width+16);
        displayName = Translate.guiGeneric(false,"button","redirect","add_internal");
        width = this.font.width(displayName)+4;
        addTopButton(left, displayName, width, new ArrayList<>(),
                (screen, button) -> this.redirectInstance.openSoundFinderSelection(this),this);
        left+=(width+16);
        displayName = Translate.guiGeneric(false,"button","delete_mode");
        width = this.font.width(displayName)+4;
        this.toggleMode = addTopButton(left, displayName, width,
                Translate.guiNumberedList(3,"button","delete_mode","desc"),
                (screen, button) -> toggleDeleteMode(!this.deleteMode),this);
        left+=(width+16);
        displayName = Translate.guiGeneric(false,"button","redirect","view_mode");
        width = this.font.width(displayName)+4;
        this.viewMode = addTopButton(left, displayName, width,
                Translate.guiNumberedList(1,"button","redirect","view_mode","desc"),
                (screen, button) -> toggleViewMode(!this.viewAll),this);
    }

    private void addExternal() {
        String temp = "temp"+this.allKeys.size();
        this.externalRedirectMap.put(temp,temp);
        this.allKeys.add(temp);
        this.newKeys.add(temp);
        sort();
        this.hasEdits = true;
        save();
    }

    public void addInternal(String foundVal) {
        String temp = "temp"+this.allKeys.size();
        this.internalRedirectMap.put(temp,foundVal);
        this.allKeys.add(temp);
        this.newKeys.add(temp);
        sort();
        this.hasEdits = true;
        save();
    }

    private void toggleDeleteMode(boolean isActive) {
        this.deleteMode = isActive;
        this.toggleMode.updateDisplayFormat(isActive ? TextFormatting.RED : TextFormatting.WHITE);
    }

    private void toggleViewMode(boolean isActive) {
        this.viewAll = isActive;
        this.viewMode.updateDisplayFormat(isActive ? TextFormatting.RED : TextFormatting.WHITE);
    }

    private void delete(String key) {
        this.internalRedirectMap.remove(key);
        this.externalRedirectMap.remove(key);
        this.allKeys.remove(key);
        this.newKeys.remove(key);
    }



    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if(scroll!=0) {
            if(scroll>1 && this.canScrollDown) {
                this.scrollPos++;
                this.canScrollDown = (this.viewAll && (this.numElements+this.scrollPos)<this.allKeys.size()) ||
                        (!this.viewAll && (this.numElements+this.scrollPos)<this.newKeys.size());
            } else if(this.scrollPos>0) {
                this.scrollPos--;
                this.canScrollDown = true;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 0) {
            if(this.deleteMode) {
                if (this.hoverKey) {
                    delete(this.elementHover);
                }
            } else {
                this.isKeySelected = this.hoverKey;
                this.selectedElement = this.elementHover;
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean keyPressed(int keyCode, int x, int y) {
        if(!blacklistedKeys.contains(keyCode) && Objects.nonNull(this.selectedElement)) {
            String typed = GLFW.glfwGetKeyName(keyCode, -1);
            if(hasShiftDown() && Objects.nonNull(typed)) typed = typed.toUpperCase(Locale.ROOT);
            addCharToKey(keyCode == GLFW.GLFW_KEY_BACKSPACE, this.internalRedirectMap.containsKey(this.selectedElement), typed);
            return true;
        }
        return super.keyPressed(keyCode, x, y);
    }

    private void addCharToKey(boolean backspace, boolean internal, String c) {
        if(this.isKeySelected) {
            String newVal = this.selectedElement;
            if (backspace) {
                if (!newVal.isEmpty())
                    newVal = newVal.substring(0, newVal.length() - 1);
            } else newVal += c;
            String value = internal ? this.internalRedirectMap.get(this.selectedElement) :
                    this.externalRedirectMap.get(this.selectedElement);
            boolean wasNew = this.newKeys.contains(this.selectedElement);
            delete(this.selectedElement);
            if(internal) this.internalRedirectMap.put(newVal,value);
            else this.externalRedirectMap.put(newVal,value);
            if(wasNew) this.newKeys.add(newVal);
            this.allKeys.add(newVal);
        } else {
            String value = internal ? this.internalRedirectMap.get(this.selectedElement) :
                    this.externalRedirectMap.get(this.selectedElement);
            if (backspace) {
                if (!value.isEmpty())
                    value = value.substring(0, value.length() - 1);
            } else value += c;
            if(internal) this.internalRedirectMap.put(this.selectedElement,value);
            else this.externalRedirectMap.put(this.selectedElement,value);
        }
        this.hasEdits = true;
        save();
    }

    @Override
    protected void drawStuff(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        int centerLeft = this.width/4;
        int centerRight = this.width-centerLeft;
        int top = this.spacing+24;
        GuiUtil.drawBox(new Vector2f(0,top),this.width,this.height-this.spacing*2-48,black(196),this.getBlitOffset());
        Vector2f start = new Vector2f(0, top);
        Vector2f end = new Vector2f(this.width, top);
        GuiUtil.drawLine(start,end,white(192), 1f, this.getBlitOffset());
        top+=this.spacing;
        String key = this.font.plainSubstrByWidth(Translate.guiGeneric(false,"redirect","key"),
                this.width/2);
        String value = this.font.plainSubstrByWidth(Translate.guiGeneric(false,"redirect","value"),
                this.width/2);
        drawCenteredString(matrix,this.font,key,centerLeft,top, GuiUtil.WHITE);
        drawCenteredString(matrix,this.font,value,centerRight, top, GuiUtil.WHITE);
        top+=(font.lineHeight+this.spacing+this.verticalSpace);
        boolean hoverAny = false;
        if(this.viewAll) {
            for (String element : this.allKeys) {
                boolean internal = this.internalRedirectMap.containsKey(element);
                boolean hover = mouseHover(new Vector2f(0, top), mouseX, mouseY, this.width, font.lineHeight + this.spacing);
                boolean left = mouseX<(this.width/2);
                int textColor = GuiUtil.WHITE;
                if (hover) {
                    this.hoverKey = left;
                    hoverAny = true;
                    this.elementHover = element;
                    textColor = GuiUtil.makeRGBAInt(192, 192, 192, 255);
                    if(left) GuiUtil.drawBox(new Vector2f(0, top), this.width/2, font.lineHeight + this.spacing * 2,
                            new Vector4f(64, 64, 46, 96), this.getBlitOffset());
                    else GuiUtil.drawBox(new Vector2f(((float)this.width)/2, top), this.width/2, font.lineHeight + this.spacing * 2,
                            new Vector4f(64, 64, 46, 96), this.getBlitOffset());
                    renderComponentTooltip(matrix,elementHover(element,left,internal), mouseX, mouseY);
                }
                top += this.spacing;
                drawCenteredString(matrix,this.font, element, centerLeft, top, textColor);
                value = internal ? this.internalRedirectMap.get(element) : this.externalRedirectMap.get(element);
                drawCenteredString(matrix,this.font, value, centerRight, top, textColor);
                top += this.spacing;
            }
        } else {
            for (String element : this.newKeys) {
                boolean internal = this.internalRedirectMap.containsKey(key);
                boolean hover = mouseHover(new Vector2f(0, top), mouseX, mouseY, this.width, font.lineHeight + this.spacing);
                boolean left = mouseX<(this.width/2);
                int textColor = GuiUtil.WHITE;
                if (hover) {
                    hoverAny = true;
                    this.hoverKey = left;
                    this.elementHover = element;
                    textColor = GuiUtil.makeRGBAInt(192, 192, 192, 255);
                    if(left) GuiUtil.drawBox(new Vector2f(0, top), this.width/2, font.lineHeight + this.spacing * 2,
                            new Vector4f(64, 64, 46, 96), this.getBlitOffset());
                    else GuiUtil.drawBox(new Vector2f(((float)this.width)/2, top), this.width/2, font.lineHeight + this.spacing * 2,
                            new Vector4f(64, 64, 46, 96), this.getBlitOffset());
                    renderComponentTooltip(matrix,elementHover(element,left,internal), mouseX, mouseY);
                }
                top += this.spacing;
                drawCenteredString(matrix,this.font, element, centerLeft, top, textColor);
                value = internal ? this.internalRedirectMap.get(element) : this.externalRedirectMap.get(element);
                drawCenteredString(matrix,this.font, value, centerRight, top, textColor);
                top += this.spacing;
            }
        }
        if(!hoverAny) this.elementHover = null;
        start = new Vector2f(start.x,this.height-this.spacing-24);
        end = new Vector2f(end.x,this.height-this.spacing-24);
        GuiUtil.drawLine(start,end,white(192), 1f, this.getBlitOffset());
        boolean size = (this.viewAll && this.allKeys.size()>this.numElements) ||
                (!this.viewAll && newKeys.size()>this.numElements);
        if(size) drawScrollBar();
    }

    private void drawScrollBar() {
        float num = this.viewAll ? this.allKeys.size() : this.newKeys.size();
        float ratio = ((float)this.numElements)/ num;
        int scrollBarHeight = (int)((this.height-48)*ratio);
        int emptyHeight = this.height-48-scrollBarHeight;
        int perIndex = (int)(emptyHeight/(num-this.numElements));
        int top = 24+(perIndex*this.scrollPos);
        int x = this.width-1;
        Vector2f start = new Vector2f(x, top);
        Vector2f end = new Vector2f(x, top+scrollBarHeight);
        GuiUtil.drawLine(start,end,white(192), 2f, this.getBlitOffset());
    }

    private List<ITextComponent> elementHover(String key, boolean left, boolean internal) {
        String type = internal ? "internal" : "external";
        if(left)
            return Stream.of(Translate.guiGeneric(false, "redirect", type),
                    Translate.guiGeneric(false, "redirect", "is_used") + " " +
                            (this.redirectInstance.isSongEntryUsed(getInstance(), key)))
                    .map(StringTextComponent::new).collect(Collectors.toList());
        return Stream.of(type.matches("internal") ? this.internalRedirectMap.get(key) :
                this.externalRedirectMap.get(key)).map(StringTextComponent::new).collect(Collectors.toList());
    }

    @Override
    protected void save() {
        if(this.hasEdits)
            this.madeChange(true);
        this.redirectInstance.save(this.internalRedirectMap,this.externalRedirectMap);
    }
}
