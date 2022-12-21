package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.input.Keyboard;

import javax.vecmath.Point2i;
import javax.vecmath.Point4i;
import java.util.ArrayList;
import java.util.List;

public class GuiPopUp extends GuiSuperType {

    private final int spacing;
    private final boolean canType;
    private final String id;
    private final String title;
    private final List<String> hoverText;
    private final List<GuiPage.Icon> icons;
    private boolean isHover;
    private String value;
    private String error;
    private boolean hoverYes;
    private boolean hoverNo;

    public GuiPopUp(GuiSuperType parent, GuiType type, Instance configInstance, String id) {
        this(parent, type, configInstance, id, false, 0, new ArrayList<>());
    }

    public GuiPopUp(GuiSuperType parent, GuiType type, Instance configInstance, String id, boolean canType, int descLines,
                    List<GuiPage.Icon> icons) {
        super(parent, type, configInstance);
        this.id = id;
        this.title = Translate.guiGeneric(false,"popup",id,"name");
        this.hoverText = Translate.guiNumberedList(descLines,"button",id+"_add");
        this.icons = icons;
        this.spacing = 16;
        this.value = "channel_name";
        this.canType = canType;
        this.error = null;
        this.hoverYes = false;
        this.hoverNo = false;
    }

    @Override
    protected void keyTyped(char c, int key) {
        if (key == Keyboard.KEY_ESCAPE) this.mc.displayGuiScreen(this.parent);
        if (this.canType) {
            if (!blacklistedKeys.contains(key)) {
                if (key == Keyboard.KEY_BACK) this.value = backspace(this.value);
                else this.value += c;
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            if(this.hoverYes) {
                this.mc.displayGuiScreen(null);
                this.mc.setIngameFocus();
            } else if(this.hoverNo) this.mc.displayGuiScreen(this.parent);
            else if (this.canType && this.isHover) click();
            else clearError();
        }
    }

    private void click() {
        if(this.value==null || this.value.isEmpty()) this.error = "blank";
        else if(this.getInstance().channelExists(this.value)) this.error = "duplicate";
        else {
            this.icons.add(this.getInstance().addChannel(this.value));
            ((GuiPage)this.parent).updateIcons(this.icons);
            Minecraft.getMinecraft().displayGuiScreen(this.parent);
        }
    }

    private void clearError() {
        this.error = null;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.parent.drawScreen(mouseX, mouseY, partialTicks);
        drawStuff(mouseX, mouseY, partialTicks);
    }
    @Override
    protected void drawStuff(int mouseX, int mouseY, float partialTicks) {
        GuiUtil.drawBox(new Point2i(0,0),this.width,this.height,new Point4i(0,0,0,128),this.zLevel);
        Point2i center = new Point2i(this.width/2,this.height/2);
        if(this.id.matches("confirmation")) drawConfirmationBox(center,mouseX,mouseY,this.fontRenderer);
        else {
            if (this.canType) drawCanType(center, mouseX, mouseY, this.fontRenderer);
            drawError(center, this.fontRenderer);
        }
    }

    private void drawError(Point2i center, FontRenderer font) {
        if(this.error!=null && !this.error.isEmpty()) {
            int boxHeight = (font.FONT_HEIGHT*2)+(this.spacing*4);
            GuiUtil.drawBox(new Point2i(0,this.height-boxHeight),this.width,boxHeight,black(255),this.zLevel);
            int red = GuiUtil.makeRGBAInt(255,0,0,255);
            int bottom = this.height-this.spacing-font.FONT_HEIGHT;
            drawCenteredString(font,Translate.guiGeneric(false,"popup","error",this.error,"name"),
                    center.x,bottom,red);
            Point2i start = new Point2i(0,bottom-this.spacing);
            Point2i end = new Point2i(this.width,bottom-this.spacing);
            GuiUtil.drawLine(start,end,white(255),1f,this.zLevel);
            start.setY(start.y-this.spacing-font.FONT_HEIGHT);
            end.setY(end.y-this.spacing-font.FONT_HEIGHT);
            drawCenteredString(font,Translate.guiGeneric(false,"popup","error","title"),center.x,
                    start.y,red);
            start.setY(start.y-this.spacing);
            end.setY(end.y-this.spacing);
            GuiUtil.drawLine(start,end,white(255),1f,this.zLevel);
        }
    }

    private void drawCanType(Point2i center, int mouseX, int mouseY, FontRenderer font) {
        int width = getTypeWidth(this.spacing*2,font);
        int totalHeight = (font.FONT_HEIGHT*2)+(this.spacing*4);
        int boxHeight = font.FONT_HEIGHT+(this.spacing*2);
        Point2i topLeft = new Point2i(center.x-(width/2),center.y-(totalHeight/2));
        drawSelectionBox(topLeft,width,boxHeight,false);
        drawCenteredString(font,this.title, center.x, topLeft.y+this.spacing,GuiUtil.WHITE);
        topLeft.setY(topLeft.y+boxHeight);
        this.isHover = mouseHover(topLeft,mouseX,mouseY,width,boxHeight);
        drawSelectionBox(topLeft,width,boxHeight,this.isHover);
        int color = GuiUtil.WHITE;
        if(this.isHover) color = GuiUtil.makeRGBAInt(0,0,0,255);
        drawCenteredString(font,this.value, center.x,topLeft.y+this.spacing,color);
        topLeft.setY(topLeft.y+boxHeight);
        if(this.isHover) drawHoveringText(this.hoverText,mouseX,mouseY);
    }

    private int getTypeWidth(int padding, FontRenderer font) {
        int base = 200;
        int textWidth = padding+font.getStringWidth(this.title);
        if(textWidth>base) base = textWidth;
        textWidth = padding+font.getStringWidth(this.value);
        if(textWidth>base) base = textWidth;
        return base;
    }

    private void drawConfirmationBox(Point2i center, int mouseX, int mouseY, FontRenderer font) {
        int width = font.getStringWidth(this.title);
        int totalHeight = (font.FONT_HEIGHT*2)+(this.spacing*4);
        int boxHeight = font.FONT_HEIGHT+(this.spacing*2);
        Point2i topLeft = new Point2i(center.x-(width/2),center.y-(totalHeight/2));
        drawSelectionBox(topLeft,width,boxHeight,false);
        drawCenteredString(font,this.title, center.x, topLeft.y+this.spacing,GuiUtil.WHITE);
        topLeft.setY(topLeft.y+boxHeight);
        String yes = Translate.guiGeneric(false,"popup","yes");
        String no = Translate.guiGeneric(false,"popup","no");
        this.hoverYes = mouseHover(topLeft,mouseX,mouseY,width/2,boxHeight);
        drawSelectionBox(topLeft,width/2,boxHeight,this.isHover);
        int color = GuiUtil.WHITE;
        if(this.hoverYes) color = GuiUtil.makeRGBAInt(0,0,0,255);
        drawCenteredString(font,yes, center.x-(width/4),topLeft.y+this.spacing,color);
        topLeft.setX(center.x);
        this.hoverNo = mouseHover(topLeft,mouseX,mouseY,width/2,boxHeight);
        drawSelectionBox(topLeft,width/2,boxHeight,this.isHover);
        color = GuiUtil.WHITE;
        if(this.hoverNo) color = GuiUtil.makeRGBAInt(0,0,0,255);
        drawCenteredString(font,no, center.x-(width/4),topLeft.y+this.spacing,color);
    }

    private void drawSelectionBox(Point2i topLeft, int width, int height, boolean hover) {
        if(hover) GuiUtil.drawBoxWithOutline(topLeft,width,height,new Point4i(64,64,64,255),
                new Point4i(255,255,255,255),1f,this.zLevel);
        else GuiUtil.drawBoxWithOutline(topLeft,width,height,new Point4i(0,0,0,255),
                new Point4i(255,255,255,255),1f,this.zLevel);
    }

    @Override
    protected void save() {

    }
}
