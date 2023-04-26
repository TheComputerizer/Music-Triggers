package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.TextFormatting;

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
    private boolean canScrollDown;
    private boolean hasEdits;

    public GuiPage(GuiSuperType parent, GuiType type, Instance configInstance, String id, List<Icon> icons, boolean buttons) {
        super(parent, type, configInstance);
        this.id = id==null ? type.getId() : id;
        this.icons = icons;
        this.canEdit = buttons;
        this.deleteMode = false;
        this.scrollPos = 0;
        this.canScrollDown = false;
    }

    public String getID() {
        return this.id;
    }

    public void updateIcons(List<Icon> icons) {
        this.icons.clear();
        this.icons.addAll(icons);
    }

    @Override
    public void init() {
        super.init();
        if(this.canEdit) {
            String displayName = Translate.guiGeneric(false,"button",this.id + "_add");
            int width = this.font.width(displayName)+8;
            int left = 16;
            addSuperButton(createBottomButton(displayName, width, 1, new ArrayList<>(),
                    (screen, button, mode) -> {
                        Minecraft.getInstance().setScreen(
                                new GuiPopUp(this,GuiType.POPUP,this.getInstance(),this.id,true, new ArrayList<>(this.icons)));
                        this.hasEdits = true;
                        save();
                    }),left);
            left+=(width+16);
            displayName = Translate.guiGeneric(false, "button", "delete_mode");
            width = this.font.width(displayName) + 8;
            String finalDisplayName = displayName;
            addSuperButton(createBottomButton(displayName, width, 2,
                    Translate.guiNumberedList(3, "button", "delete_mode", "desc"),
                    (screen, button, mode) -> {
                        TextFormatting color = mode == 1 ? TextFormatting.WHITE : TextFormatting.RED;
                        this.deleteMode = color==TextFormatting.RED;
                        button.updateDisplay(color + finalDisplayName);
                    }), left);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if(scroll!=0) {
            if(scroll<1 && this.canScrollDown) {
                this.scrollPos++;
                this.canScrollDown = false;
                return true;
            } else if(this.scrollPos>0) {
                this.scrollPos--;
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if(super.mouseClicked(mouseX, mouseY, mouseButton)) return true;
        if (mouseButton == 0) {
            Iterator<Icon> itr = this.icons.iterator();
            while (itr.hasNext()) {
                Icon icon = itr.next();
                if(this.deleteMode && icon.canDelete()) {
                    getInstance().deleteChannel(icon.channelName());
                    itr.remove();
                    this.hasEdits = true;
                    save();
                    return true;
                }
                else if(icon.onClick(this)) return true;
            }
        }
        return false;
    }

    @Override
    protected void drawStuff(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        int top = this.spacing+24;
        drawIcons(matrix,mouseX,mouseY,top);
        drawLeftSide(matrix,top);
    }

    private void drawIcons(MatrixStack matrix, int mouseX, int mouseY, int top) {
        int left = this.width-(this.spacing*15);
        Vector2f topLeft = new Vector2f(left,top);
        boolean isLeft = true;
        for(Icon icon : this.icons) {
            if(isLeft) topLeft = new Vector2f(left,top);
            else topLeft = new Vector2f(left+(this.spacing*7),topLeft.y);
            icon.drawIcon(matrix,topLeft,this.spacing,mouseX,mouseY,black(192),this.getBlitOffset(),
                    Minecraft.getInstance().screen == this);
            if(!isLeft) top+=this.spacing*7;
            isLeft = !isLeft;
        }
    }

    private void drawLeftSide(MatrixStack matrix, int top) {
        if(Minecraft.getInstance().screen==this) {
            int textHeight = this.font.lineHeight;
            int centerX = this.width / 2;
            int left = this.spacing;
            int textX = left + (this.spacing / 2);
            for (Icon icon : this.icons) {
                if (icon.getHover()) {
                    GuiUtil.drawLine(new Vector2f(left, top), new Vector2f(centerX, top), white(128), 1f, this.getBlitOffset());
                    top += this.spacing;
                    drawString(matrix,font,icon.getDisplay(), textX, top, GuiUtil.WHITE);
                    top += (textHeight + this.spacing);
                    GuiUtil.drawLine(new Vector2f(left, top), new Vector2f(centerX, top), white(128), 1f, this.getBlitOffset());
                    top += this.spacing;
                    top = GuiUtil.drawMultiLineString(matrix, this.font, icon.getDescription(), textX, centerX, top, textHeight + (this.spacing / 2),
                            100, 0, GuiUtil.WHITE) + (this.spacing / 2);
                    GuiUtil.drawLine(new Vector2f(left, top), new Vector2f(centerX, top), white(128), 1f, this.getBlitOffset());
                    break;
                }
            }
        }
    }

    @Override
    protected void save() {
        if(this.hasEdits)
            this.madeChange(true);
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

        /*
            This can only be done since the only case where an icon can be deleted is on the channel selection page
        */
        public String channelName() {
            return this.id;
        }

        private boolean isHovering(int mouseX, int mouseY, Vector2f topLeft, int sideLength) {
            return mouseX>topLeft.x && mouseX<(topLeft.x+sideLength) && mouseY>topLeft.y && mouseY<(topLeft.y+sideLength);
        }

        public void drawIcon(MatrixStack matrix, Vector2f topLeft, int spacing, int mouseX, int mouseY, Vector4f color, float zLevel, boolean curScreen) {
            GuiUtil.drawBoxOutline(topLeft,spacing*6,spacing*6,new Vector4f(255,255,255,192),
                    1f,zLevel);
            Vector2f backgroundTopLeft = new Vector2f(topLeft.x+(((float)spacing)/2),topLeft.y+(((float)spacing)/2));
            Vector2f iconCenter = new Vector2f((int)(backgroundTopLeft.x+(spacing*2.5f)),(int)(backgroundTopLeft.y+(spacing*2.5f)));
            this.hover = curScreen && isHovering(mouseX,mouseY,backgroundTopLeft,spacing*5);
            if(hover) {
                GuiUtil.drawBox(backgroundTopLeft,spacing*5,spacing*5,GuiUtil.reverseColors(color),zLevel);
                GuiUtil.bufferSquareTexture(matrix, iconCenter,spacing*1.5f,this.hoverTexture);
            } else {
                GuiUtil.drawBox(backgroundTopLeft, spacing * 5, spacing * 5, color, zLevel);
                GuiUtil.bufferSquareTexture(matrix, iconCenter, spacing * 1.5f, this.texture);
            }
        }

        public boolean onClick(GuiSuperType parent) {
            if(this.hover) {
                parent.playGenericClickSound();
                this.handlerFunction.accept(parent,this.id);
                return true;
            }
            return false;
        }

        public boolean getHover() {
            return this.hover;
        }

        public String getDisplay() {
            if(this.separateDisplay) return this.display+" - "+this.id;
            return this.display;
        }

        public String getDescription() {
            return this.description;
        }
    }
}
