package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

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
    private boolean hasEdits;

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
    public void init() {
        super.init();
        int textSlot = Minecraft.getInstance().font.lineHeight+(this.spacing*2);
        int totalHeight = this.height-((this.spacing*3)+textSlot+48);
        int runningHeight = (textSlot*2)-this.spacing;
        int runningTotal = 1;
        while(runningHeight+textSlot<totalHeight) {
            runningTotal++;
            runningHeight+=textSlot;
        }
        this.numElements = runningTotal;
        this.canScrollDown = this.numElements < this.elements.size();
        this.verticalSpace = (totalHeight-runningHeight)/2;
        resetElements();
        int index = Integer.MIN_VALUE;
        for(Element element : this.elements) {
            if(index==Integer.MIN_VALUE)
                index = element.getIndex();
            else index = element.adjustOriginalIndex(index);
        }
        String displayName;
        int width;
        int left = 96;
        if(this.onAdd!=null) {
            displayName = Translate.guiGeneric(false,"button",this.group+"_add");
            width = this.font.width(displayName)+8;
            addTopButton(left, displayName, width, new ArrayList<>(),
                    (screen, button) -> {
                        this.onAdd.accept(this,this.group,this.extra);
                        this.hasEdits = true;
                        save();
                    },this);
            left+=(width+16);
            displayName = Translate.guiGeneric(false,"button","delete_mode");
            width = this.font.width(displayName)+8;
            this.toggleMode = addTopButton(left, displayName, width,
                    Translate.guiNumberedList(3,"button","delete_mode","desc"),
                    (screen, button) -> toggleDeleteMode(!this.deleteMode),this);
            left+=(width+16);
        }
        if(Objects.nonNull(this.extra) && this.extra.matches("titles")) {
            displayName = Translate.guiGeneric(false,"button","transition_titles");
            width = this.font.width(displayName)+8;
            this.transitionMode = addTopButton(left, displayName,
                    width, Translate.guiNumberedList(3,"button","transition_titles","desc"),
                    (screen, button) -> transitionsView(),this);
            left+=(width+16);
        }
        displayName = Translate.guiGeneric(false,"button","sort","original");
        width = this.font.width(displayName)+8;
        this.sortMode = addTopButton(left, displayName, width,
                Translate.guiNumberedList(3,"button","sort","desc"), (screen, button) ->
                        button.updateBaseTextAndFormatting(this.font,toggleSortMode(), ChatFormatting.WHITE),
                this);
    }

    public boolean isTitleView() {
        return this.transitionView!=null && this.transitionView.matches("titles");
    }

    private void transitionsView() {
        if(this.transitionView.matches("titles")) this.transitionView = "images";
        else transitionView = "titles";
        this.transitionMode.updateBaseTextAndFormatting(this.font,
                Translate.guiGeneric(false,"button","transition_images"),ChatFormatting.WHITE);
    }

    private void toggleDeleteMode(boolean isActive) {
        this.deleteMode = isActive;
        this.toggleMode.updateDisplayFormat(isActive ? ChatFormatting.RED : ChatFormatting.WHITE);
    }

    private String toggleSortMode() {
        this.sortType++;
        if(this.sortType>2) this.sortType = 0;
        return sortElements();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if(scroll!=0) {
            if(scroll<1) {
                if (this.canScrollDown) {
                    this.scrollPos++;
                    this.canScrollDown = this.numElements + this.scrollPos < this.elements.size();
                }
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
        if (mouseButton == 0 && this.elementHover>=0) {
            Element element = this.elements.get(this.elementHover);
            if(this.deleteMode) {
                if (element.onDelete()) {
                    this.elements.remove(element);
                    this.hasEdits = true;
                    save();
                }
            } else element.onClick();
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void resetElements() {
        this.elements.clear();
        if(Objects.nonNull(this.extra) && this.extra.matches("titles"))
            this.elements.addAll(this.getInstance().transitionsSpecialCase(this,this.channel,this.transitionView));
        else this.elements.addAll(this.getInstance().getElementGroup(this,this.channel,this.group,this.extra));
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
    protected void drawStuff(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        int centerX = this.width/2;
        int top = this.spacing+24;
        GuiUtil.drawBox(new Vector3f(0,top,0),this.width,this.height-this.spacing*2-48,black(196),this.getBlitOffset());
        Vector3f start = new Vector3f(0, top, 0);
        Vector3f end = new Vector3f(this.width, top, 0);
        GuiUtil.drawLine(start,end,white(192), 1f, this.getBlitOffset());
        top+=this.spacing;
        drawCenteredString(matrix,this.font,this.customTitle,centerX,top,GuiUtil.WHITE);
        top+=(font.lineHeight+this.spacing+this.verticalSpace);
        boolean hoverAny = false;
        int index = 0;
        int bottom = this.height-(this.spacing+24);
        for(Element element : this.elements) {
            if(index>=this.scrollPos) {
                boolean hover = mouseHover(new Vector3f(0, top, 0), mouseX, mouseY, this.width, font.lineHeight + this.spacing);
                int textColor = GuiUtil.WHITE;
                if (hover) {
                    hoverAny = true;
                    this.elementHover = index;
                    textColor = GuiUtil.makeRGBAInt(0, 0, 0, 255);
                    GuiUtil.drawBox(new Vector3f(0, top, 0), this.width, font.lineHeight + this.spacing * 2,
                            new Vector4f(64, 64, 46, 96), this.getBlitOffset());
                    renderComponentTooltip(matrix,element.getHoverLines(), mouseX, mouseY);
                }
                top += this.spacing;
                drawCenteredString(matrix, this.font, element.getDisplay(), centerX, top, textColor);
                top += (this.spacing + this.font.lineHeight);
                if ((bottom - top) < (this.spacing + this.font.lineHeight)) break;
            }
            index++;
        }
        if(!hoverAny) this.elementHover = -1;
        start = new Vector3f(start.x(),this.height-this.spacing-24,0);
        end = new Vector3f(end.x(),this.height-this.spacing-24,0);
        GuiUtil.drawLine(start,end,white(192), 1f, this.getBlitOffset());
        if(this.elements.size()>this.numElements) drawScrollBar();
    }

    private void drawScrollBar() {
        float ratio = ((float)this.numElements)/((float)this.elements.size());
        int scrollBarHeight = (int)((this.height-48)*ratio);
        int emptyHeight = this.height-48-scrollBarHeight;
        int perIndex = emptyHeight/(this.elements.size()-this.numElements);
        int top = 24+this.spacing+(perIndex*this.scrollPos);
        int x = this.width-1;
        Vector3f start = new Vector3f(x, top, 0);
        Vector3f end = new Vector3f(x, top+scrollBarHeight, 0);
        GuiUtil.drawLine(start,end,white(192), 2f, this.getBlitOffset());
    }

    @Override
    public void parentUpdate() {
        resetElements();
    }

    @Override
    protected void save() {
        if(this.hasEdits)
            this.madeChange(true);
    }

    public static class Element {

        private final GuiSelection parent;
        private final String channel;
        private final String id;
        private final String display;
        private final List<Component> hoverText;
        private final boolean isSong;
        private int index;
        private final BiConsumer<String, String> onClick;
        private final BiConsumer<String, String> onDelete;

        public Element(GuiSelection parent, String channel, String id, String display, List<String> hoverText, boolean isSong,
                       int index, BiConsumer<String, String> onClick, BiConsumer<String, String> onDelete) {
            this.parent = parent;
            this.channel = channel;
            this.id = id;
            this.isSong = isSong;
            this.display = display;
            List<String> hoverString = isSong ? Translate.songHover(id.charAt(id.length()-1),
                    parent.getInstance().getTriggers(channel,id)) :
                    (Objects.isNull(hoverText) ? new ArrayList<>() : hoverText);
            this.hoverText = hoverString.stream().map(TextComponent::new).collect(Collectors.toList());
            this.index = index;
            this.onClick = onClick;
            this.onDelete = onDelete;
        }

        public int adjustOriginalIndex(int previous) {
            if(this.index<=previous) this.index = previous+1;
            return this.index;
        }

        public int getIndex() {
            return this.index;
        }

        public String getDisplay() {
            return this.display;
        }

        public List<Component> getHoverLines() {
            return this.hoverText;
        }

        public void onClick() {
            if(this.onClick!=null) this.onClick.accept(this.channel,this.id);
        }

        public boolean onDelete() {
            if(this.onDelete!=null) this.onDelete.accept(this.channel,this.id);
            return Objects.nonNull(this.onDelete);
        }
    }
}
