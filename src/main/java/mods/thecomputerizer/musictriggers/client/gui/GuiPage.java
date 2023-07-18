package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Mouse;

import javax.vecmath.Point4f;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class GuiPage extends GuiSuperType {

    private final String id;
    private final List<Icon> icons;
    private final boolean canEdit;
    private boolean deleteMode;
    private int scrollPos;
    private int numIconsAdjusted;
    private int numVisibleRows;
    private boolean canScroll;
    private boolean canScrollDown;
    private boolean hasEdits;

    public GuiPage(GuiSuperType parent, GuiType type, Instance configInstance, String id, List<Icon> icons, boolean buttons) {
        super(parent, type, configInstance);
        this.id = Objects.isNull(id) ? type.getId() : id;
        this.icons = icons;
        this.canEdit = buttons;
        this.deleteMode = false;
        this.numIconsAdjusted = icons.size()%2==0 ? icons.size() : icons.size()+1;
    }

    public String getID() {
        return this.id;
    }

    public void updateIcons(List<Icon> icons) {
        this.icons.clear();
        this.icons.addAll(icons);
        this.numIconsAdjusted = icons.size()%2==0 ? icons.size() : icons.size()+1;
    }

    private void calculateScrollSize() {
        this.scrollPos = 0;
        int iconSlot = this.spacing*7;
        int totalHeight = this.height-(this.spacing+24);
        int runningHeight = iconSlot;
        int runningTotal = 1;
        while(runningHeight+iconSlot<totalHeight) {
            runningTotal++;
            runningHeight+=iconSlot;
        }
        this.numVisibleRows = runningTotal;
        this.canScroll = this.numVisibleRows*2 < this.numIconsAdjusted;
        this.canScrollDown = this.canScroll;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if(this.canScroll) {
            int scroll = Mouse.getEventDWheel();
            if (scroll != 0) {
                if (scroll >= 1) {
                    if (this.scrollPos>0) {
                        this.scrollPos--;
                        this.canScrollDown = true;
                    }
                } else if (this.canScrollDown) {
                    this.scrollPos++;
                    this.canScrollDown = (this.scrollPos+this.numVisibleRows)*2 < this.numIconsAdjusted;
                }
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        calculateScrollSize();
        if(this.canEdit) {
            String displayName = Translate.guiGeneric(false,"button",this.id + "_add");
            int width = this.fontRenderer.getStringWidth(displayName)+8;
            int left = 16;
            addSuperButton(createBottomButton(displayName, width, 1, new ArrayList<>(),
                    (screen, button, mode) -> {
                        if(ChannelManager.isClientConfig()) {
                            Minecraft.getMinecraft().displayGuiScreen(new GuiPopUp(this, GuiType.POPUP,
                                    this.getInstance(), this.id, true, new ArrayList<>(this.icons)));
                            this.hasEdits = true;
                            save();
                        }
                    }),left);
            left+=(width+16);
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
        for(Icon icon : this.icons) icon.disableHover();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            Iterator<Icon> itr = this.icons.iterator();
            while (itr.hasNext()) {
                Icon icon = itr.next();
                if(this.deleteMode && icon.canDelete()) {
                    getInstance().deleteChannel(icon.channelName());
                    itr.remove();
                    this.hasEdits = true;
                    save();
                }
                else icon.onClick(this);
            }
        }
    }

    @Override
    protected void drawStuff(int mouseX, int mouseY, float partialTicks) {
        int top = this.spacing+24;
        drawIcons(mouseX,mouseY,top);
        drawLeftSide(top);
    }

    private void drawIcons(int mouseX, int mouseY, int top) {
        int left = this.width-(this.spacing*15);
        Vec2f topLeft = new Vec2f(left,top);
        boolean isLeft = true;
        int skip = this.scrollPos*2;
        for(Icon icon : this.icons) {
            if(skip>0) {
                icon.disableHover();
                skip--;
                continue;
            }
            if(isLeft) topLeft = new Vec2f(left,top);
            else topLeft = new Vec2f(left+(this.spacing*7),topLeft.y);
            icon.drawIcon(topLeft,this.spacing,mouseX,mouseY,black(192),this.zLevel,
                    Minecraft.getMinecraft().currentScreen == this);
            if(!isLeft) top+=this.spacing*7;
            if(top+(this.spacing*7)>=this.height) break;
            isLeft = !isLeft;
        }
        if(this.canScroll) drawScrollBar();
    }

    private void drawScrollBar() {
        float indices = (((float)this.numIconsAdjusted-((float)this.numVisibleRows*2f))/2f)+1f;
        float perIndex = this.height/indices;
        int top = (int)(perIndex*this.scrollPos);
        int x = this.width-1;
        Vec2f start = new Vec2f(x, top);
        if(perIndex<1) perIndex = 1;
        Vec2f end = new Vec2f(x, (int)(top+perIndex));
        GuiUtil.drawLine(start,end,white(192), 2f, this.zLevel);
    }

    private void drawLeftSide(int top) {
        if(isActive(this)) {
            int textHeight = this.fontRenderer.FONT_HEIGHT;
            int centerX = this.width / 2;
            int left = this.spacing;
            int textX = left + (this.spacing / 2);
            for (Icon icon : this.icons) {
                if (icon.getHover()) {
                    boolean enabled = ChannelManager.isClientConfig();
                    if(icon.id.matches("debug") || icon.id.matches("registration"))
                        enabled = enabled && ChannelManager.isButtonEnabled(icon.id);
                    GuiUtil.drawLine(new Vec2f(left, top), new Vec2f(centerX, top), white(128), 1f, this.zLevel);
                    top += this.spacing;
                    drawString(this.fontRenderer, icon.getDisplay(enabled), textX, top, GuiUtil.WHITE);
                    top += (textHeight + this.spacing);
                    GuiUtil.drawLine(new Vec2f(left, top), new Vec2f(centerX, top), white(128), 1f, this.zLevel);
                    if(!enabled) break;
                    top += this.spacing;
                    top = GuiUtil.drawMultiLineString(this.fontRenderer, icon.getDescription(), textX, centerX, top, textHeight + (this.spacing / 2),
                            100, 0, GuiUtil.WHITE) + (this.spacing / 2);
                    GuiUtil.drawLine(new Vec2f(left, top), new Vec2f(centerX, top), white(128), 1f, this.zLevel);
                    break;
                }
            }
        }
    }

    @Override
    protected void save() {
        if(this.hasEdits) {
            this.madeChange(true);
            this.hasEdits = false;
        }
    }

    public static class Icon {

        private final ResourceLocation texture;
        private final ResourceLocation hoverTexture;
        private final String id;
        private final String display;
        private final String description;
        private final BiConsumer<GuiSuperType,String> handlerFunction;
        private final boolean separateDisplay;
        private final boolean canDelete;
        private boolean hover;
        public Icon(String id, ResourceLocation texture, ResourceLocation hoverTexture, boolean canDelete,
                    BiConsumer<GuiSuperType,String> onCLick) {
            this(id,null,texture,hoverTexture,canDelete,onCLick);
        }

        public Icon(String id, String displayID, ResourceLocation texture, ResourceLocation hoverTexture, boolean canDelete,
                    BiConsumer<GuiSuperType,String> onCLick) {
            this.texture = texture;
            this.hoverTexture = hoverTexture;
            this.id = id;
            if(!Objects.isNull(displayID)) {
                this.separateDisplay = true;
                Translate.guiGeneric(false,"titles",displayID,"name");
                this.display = Translate.guiGeneric(false,"titles",displayID,"name");
                this.description = Translate.guiGeneric(false,"titles",displayID,"desc");
            } else {
                this.separateDisplay = false;
                this.display = Translate.guiGeneric(false,"titles",id,"name");
                this.description = Translate.guiGeneric(false,"titles",id,"desc");
            }
            this.handlerFunction = onCLick;
            this.hover = false;
            this.canDelete = canDelete;
        }

        private boolean canDelete() {
            return this.hover && this.canDelete;
        }

        /**
            This can only be done since the only case where an icon can be deleted is on the channel selection page
        */
        public String channelName() {
            return this.id;
        }

        public void disableHover() {
            this.hover = false;
        }

        private boolean isHovering(int mouseX, int mouseY, Vec2f topLeft, int sideLength) {
            return mouseX>topLeft.x && mouseX<(topLeft.x+sideLength) && mouseY>topLeft.y && mouseY<(topLeft.y+sideLength);
        }

        public void drawIcon(Vec2f topLeft, int spacing, int mouseX, int mouseY, Point4f color, float zLevel, boolean curScreen) {
            GuiUtil.drawBoxOutline(topLeft,spacing*6,spacing*6,new Point4f(255,255,255,192),
                    1f,zLevel);
            Vec2f backgroundTopLeft = new Vec2f(topLeft.x+((float)spacing/2),topLeft.y+((float)spacing/2));
            Vec2f iconCenter = new Vec2f((int)(backgroundTopLeft.x+(spacing*2.5f)),(int)(backgroundTopLeft.y+(spacing*2.5f)));
            this.hover = curScreen && isHovering(mouseX,mouseY,backgroundTopLeft,spacing*5);
            if(this.hover) {
                GuiUtil.drawBox(backgroundTopLeft,spacing*5,spacing*5,GuiUtil.reverseColors(color),zLevel);
                GuiUtil.bufferSquareTexture(iconCenter,spacing*1.5f,this.hoverTexture);
            } else {
                GuiUtil.drawBox(backgroundTopLeft, spacing * 5, spacing * 5, color, zLevel);
                GuiUtil.bufferSquareTexture(iconCenter, spacing * 1.5f, this.texture);
            }
        }

        public void onClick(GuiSuperType parent) {
            if(this.hover) {
                parent.playGenericClickSound();
                this.handlerFunction.accept(parent,this.id);
            }
        }

        public boolean getHover() {
            return this.hover;
        }

        public String getDisplay(boolean enabled) {
            if(enabled) {
                if (this.separateDisplay) return this.display + " - " + this.id;
                return this.display;
            }
            return Translate.disabledHover().get(0);
        }

        public String getDescription() {
            return this.description;
        }
    }
}
