package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.util.TriConsumer;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import javax.vecmath.Point2i;
import javax.vecmath.Point4i;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

public class GuiSelection extends GuiSuperType {

    private final List<Element> elements;
    private final String channel;
    private final String group;
    private final String extra;
    private final String customTitle;
    private final TriConsumer<GuiSuperType, String, String> onAdd;
    private int numElements;
    private int verticalSpace;
    private int scrollPos;
    private boolean canScrollDown;
    private int elementHover;
    private ButtonSuperType toggleMode;
    private boolean deleteMode;
    private ButtonSuperType sortMode;
    private int sortType;
    private ButtonSuperType transitionMode;
    private String transitionView;

    public GuiSelection(GuiSuperType parent, GuiType type, Instance configInstance, String channel, String group,
                        String extra, String customTitle, TriConsumer<GuiSuperType, String, String> onAdd) {
        super(parent, type, configInstance);
        this.elements = new ArrayList<>();
        this.channel = channel;
        this.group = group;
        this.extra = extra;
        this.customTitle = Objects.nonNull(customTitle) ? customTitle : Translate.selectionTitle(group, channel);
        this.onAdd = onAdd;
        this.elementHover = -1;
        this.deleteMode = false;
        this.sortType = 0;
        this.transitionView = extra;
    }

    @Override
    public void setWorldAndResolution(@Nonnull Minecraft mc, int width, int height) {
        super.setWorldAndResolution(mc, width, height);
        int textSlot = mc.fontRenderer.FONT_HEIGHT+this.spacing;
        int totalHeight = this.height-((this.spacing*3)+textSlot+48);
        int runningHeight = textSlot;
        int runningTotal = 1;
        while(runningHeight+textSlot<totalHeight) {
            runningTotal++;
            runningHeight+=textSlot;
        }
        this.numElements = runningTotal;
        this.canScrollDown = this.numElements < this.elements.size();
        this.verticalSpace = (totalHeight-runningHeight)/2;
    }

    @Override
    public void initGui() {
        super.initGui();
        resetElements();
        String displayName;
        int width;
        int left = 96;
        if(this.onAdd!=null) {
            displayName = Translate.guiGeneric(false,"button",this.group+"_add");
            width = this.fontRenderer.getStringWidth(displayName)+4;
            addTopButton(left, Translate.guiGeneric(false,"button",displayName), width, new ArrayList<>(),
                    (screen, button) -> this.onAdd.accept(this,this.group,this.extra),this);
            left+=(width+16);
            displayName = Translate.guiGeneric(false,"button",this.group+"_toggle_delete");
            width = this.fontRenderer.getStringWidth(displayName)+4;
            this.toggleMode = addTopButton(left, Translate.guiGeneric(false,"button",displayName), width,
                    Translate.guiNumberedList(3,"button","delete_mode","desc"),
                    (screen, button) -> toggleDeleteMode(!this.deleteMode),this);
            left+=(width+16);
        }
        if(extra.matches("titles")) {
            displayName = Translate.guiGeneric(false,"button",this.group+"_transition_titles");
            width = this.fontRenderer.getStringWidth(displayName)+4;
            this.transitionMode = addTopButton(left, Translate.guiGeneric(false,"button",displayName),
                    width, Translate.guiNumberedList(3,"button","delete_mode","desc"),
                    (screen, button) -> transitionsView(),this);
            left+=(width+16);
        }
        displayName = Translate.guiGeneric(false,"button","sort","original");
        width = this.fontRenderer.getStringWidth(displayName)+4;
        this.sortMode = addTopButton(left, Translate.guiGeneric(false,"button",displayName), width,
                Translate.guiNumberedList(3,"button","sort","desc"), (screen, button) ->
                        button.updateBaseTextAndFormatting(this.fontRenderer,toggleSortMode()),this);
    }

    public boolean isTitleView() {
        return this.transitionView!=null && this.transitionView.matches("titles");
    }

    private void transitionsView() {
        if(this.transitionView.matches("titles")) this.transitionView = "images";
        else transitionView = "titles";
        this.transitionMode.updateBaseTextAndFormatting(this.fontRenderer,
                Translate.guiGeneric(false,"button",this.group+"_transition_images"));
    }

    private void toggleDeleteMode(boolean isActive) {
        this.deleteMode = isActive;
        this.toggleMode.updateDisplayFormat(isActive ? TextFormatting.RED : null);
    }

    private String toggleSortMode() {
        this.sortType++;
        if(this.sortType>2) this.sortType = 0;
        return sortElements();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if(scroll!=0) {
            if(scroll>1 && this.canScrollDown) {
                this.scrollPos++;
                this.canScrollDown = this.numElements+this.scrollPos < this.elements.size();
            } else if(this.scrollPos>0) {
                this.scrollPos--;
                this.canScrollDown = true;
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0 && this.elementHover>=0) {
            Element element = this.elements.get(this.elementHover);
            if(this.deleteMode) {
                element.onDelete();
                this.elements.remove(element);
            }
            else element.onClick();
        }
    }

    private void resetElements() {
        this.elements.clear();
        if(this.extra.matches("titles"))
            this.elements.addAll(this.getInstance().getElementGroup(this,this.channel,this.group,this.extra));
        else this.elements.addAll(this.getInstance().transitionsSpecialCase(this,this.channel,this.transitionView));
    }

    public String sortElements() {
        if(this.sortType<=0) {
            this.elements.sort(Comparator.comparingInt(Element::getIndex));
            return Translate.guiGeneric(false,"button","sort","original");
        }
        this.elements.sort(Comparator.comparing(Element::getDisplay));
        if(this.sortType>1) {
            Collections.reverse(this.elements);
            return Translate.guiGeneric(false,"button","sort","reverse");
        }
        return Translate.guiGeneric(false,"button","sort","alphabetical");
    }

    @Override
    protected void drawStuff(int mouseX, int mouseY, float partialTicks) {
        int centerX = this.width/2;
        int top = this.spacing+24;
        GuiUtil.drawBox(new Point2i(0,top),this.width,this.height-this.spacing*2-48,black(196),this.zLevel);
        Point2i start = new Point2i(0, top);
        Point2i end = new Point2i(this.width, top);
        GuiUtil.drawLine(start,end,white(192), 1f, this.zLevel);
        top+=this.spacing;
        drawCenteredString(this.fontRenderer,this.customTitle,centerX,top,GuiUtil.WHITE);
        top+=(fontRenderer.FONT_HEIGHT+this.spacing+this.verticalSpace);
        boolean hoverAny = false;
        int index = 0;
        for(Element element : this.elements) {
            boolean hover = mouseHover(new Point2i(0, top), mouseX, mouseY, this.width, fontRenderer.FONT_HEIGHT + this.spacing);
            int textColor = GuiUtil.WHITE;
            if(hover) {
                hoverAny = true;
                this.elementHover = index;
                textColor = GuiUtil.makeRGBAInt(0,0,0,255);
                GuiUtil.drawBox(new Point2i(0,top),this.width,fontRenderer.FONT_HEIGHT+this.spacing*2,
                        new Point4i(64,64,46,96),this.zLevel);
                drawHoveringText(element.getHoverLines(),mouseX,mouseY);
            }
            top+=this.spacing;
            drawCenteredString(this.fontRenderer,element.getDisplay(),centerX,top,textColor);
            top+=this.spacing;
            index++;
        }
        if(!hoverAny) this.elementHover = -1;
        start.setY(this.height-this.spacing-24);
        end.setY(this.height-this.spacing-24);
        GuiUtil.drawLine(start,end,white(192), 1f, this.zLevel);
        if(this.elements.size()>this.numElements) drawScrollBar();
    }

    private void drawScrollBar() {
        float ratio = ((float)this.numElements)/((float)this.elements.size());
        int scrollBarHeight = (int)((this.height-48)*ratio);
        int emptyHeight = this.height-48-scrollBarHeight;
        int perIndex = emptyHeight/(this.elements.size()-this.numElements);
        int top = 24+(perIndex*this.scrollPos);
        int x = this.width-1;
        Point2i start = new Point2i(x, top);
        Point2i end = new Point2i(x, top+scrollBarHeight);
        GuiUtil.drawLine(start,end,white(192), 2f, this.zLevel);
    }

    @Override
    public void parentUpdate() {
        resetElements();
    }

    @Override
    protected void save() {

    }

    public static class Element {

        private final GuiSelection parent;
        private final String channel;
        private final String id;
        private final String display;
        private final List<String> hoverText;
        private final boolean isSong;
        private final int index;
        private final BiConsumer<String, String> onClick;
        private final BiConsumer<String, String> onDelete;

        public Element(GuiSelection parent, String channel, String id, String display, List<String> hoverText, boolean isSong,
                       int index, BiConsumer<String, String> onClick, BiConsumer<String, String> onDelete) {
            this.parent = parent;
            this.channel = channel;
            this.id = id;
            this.isSong = isSong;
            this.display = display;
            this.hoverText = isSong ? Translate.songHover(id.charAt(id.length()-1),
                    parent.getInstance().getTriggers(channel,id)) :
                    (Objects.isNull(hoverText) ? new ArrayList<>() : hoverText);
            this.index = index;
            this.onClick = onClick;
            this.onDelete = onDelete;
        }

        public int getIndex() {
            return this.index;
        }

        public String getDisplay() {
            return this.display;
        }

        public List<String> getHoverLines() {
            return this.hoverText;
        }

        public void onClick() {
            if(this.onClick!=null) this.onClick.accept(this.channel,this.id);
        }

        public void onDelete() {
            if(this.onDelete!=null) this.onDelete.accept(this.channel,this.id);
        }
    }
}
