package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GuiPopUp extends GuiSuperType {

    private final int spacing;
    private final boolean canType;
    private final String id;
    private final String title;
    private final List<Component> hoverText;
    private final List<GuiPage.Icon> icons;
    private boolean isHover;
    private String value;
    private String error;
    private boolean hoverYes;
    private boolean hoverNo;

    public GuiPopUp(GuiSuperType parent, GuiType type, Instance configInstance, String id) {
        this(parent, type, configInstance, id, false, new ArrayList<>());
    }

    public GuiPopUp(GuiSuperType parent, GuiType type, Instance configInstance, String id, boolean canType, List<GuiPage.Icon> icons) {
        super(parent, type, configInstance);
        this.id = id;
        this.title = Translate.guiGeneric(false,"popup",id,"name");
        this.hoverText = Translate.guiNumberedList(4,"button","add_channel","hover").stream()
                .map(Component::literal).collect(Collectors.toList());
        this.icons = icons;
        this.spacing = 16;
        this.value = "channel_name";
        this.canType = canType;
        this.error = null;
        this.hoverYes = false;
        this.hoverNo = false;
    }

    @Override
    public boolean keyPressed(int keyCode, int x, int y) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            Minecraft.getInstance().setScreen(this.parent);
            return true;
        }
        if(this.canType) {
            if(checkCopy(keyCode,this.value)) return true;
            String paste = checkPaste(keyCode);
            if(!paste.isEmpty()) {
                this.value += paste;
                return true;
            }
            if(keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                this.value = backspace(this.value);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char c, int mod) {
        if(this.canType && SharedConstants.isAllowedChatCharacter(c)) {
            this.value += c;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if(super.mouseClicked(mouseX, mouseY, mouseButton)) return true;
        if (mouseButton == 0) {
            if(this.hoverYes) {
                this.onClose();
                return true;
            }
            else if(this.hoverNo) {
                Minecraft.getInstance().setScreen(this.parent);
                return true;
            }
            else if (this.canType && this.isHover) {
                click();
                return true;
            }
            else clearError();
        }
        return false;
    }

    private void click() {
        if(Objects.isNull(this.value) || this.value.isEmpty()) this.error = "blank";
        else if(this.getInstance().channelExists(this.value)) this.error = "duplicate";
        else {
            String name = this.value.trim();
            boolean isValid = true;
            for(char c : Constants.BLACKLISTED_TABLE_CHARACTERS) {
                if(name.contains(String.valueOf(c))) {
                    isValid = false;
                    break;
                }
            }
            if(!isValid) this.error = "invalid";
            else {
                this.icons.add(this.getInstance().addChannel(this.value));
                ((GuiPage) this.parent).updateIcons(this.icons);
                Minecraft.getInstance().setScreen(this.parent);
            }
        }
    }

    private void clearError() {
        this.error = null;
    }

    @Override
    public void render(@Nonnull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        this.parent.render(matrix,mouseX, mouseY, partialTicks);
        drawStuff(matrix, mouseX, mouseY, partialTicks);
    }
    @Override
    protected void drawStuff(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        GuiUtil.drawBox(new Vector3f(0,0,0),this.width,this.height,new Vector4f(0,0,0,128),this.getBlitOffset());
        Vector3f center = new Vector3f(((float)this.width)/2,((float)this.height)/2,0);
        if(this.id.matches("confirm")) drawConfirmationBox(matrix,center,mouseX,mouseY,this.font);
        else {
            if (this.canType) drawCanType(matrix, center, mouseX, mouseY, this.font);
            drawError(matrix,center, this.font);
        }
    }

    private void drawError(PoseStack matrix, Vector3f center, Font font) {
        if(Objects.nonNull(this.error) && !this.error.isEmpty()) {
            int boxHeight = (font.lineHeight*2)+(this.spacing*4);
            GuiUtil.drawBox(new Vector3f(0,this.height-boxHeight,0),this.width,boxHeight,black(255),this.getBlitOffset());
            int red = GuiUtil.makeRGBAInt(255,0,0,255);
            int bottom = this.height-this.spacing-font.lineHeight;
            drawCenteredString(matrix,font,Translate.guiGeneric(false,"popup","error",this.error,"name"),
                    (int)center.x(),bottom,red);
            Vector3f start = new Vector3f(0,bottom-this.spacing,0);
            Vector3f end = new Vector3f(this.width,bottom-this.spacing,0);
            GuiUtil.drawLine(start,end,white(255),1f,this.getBlitOffset());
            start = new Vector3f(start.x(),start.y()-this.spacing-font.lineHeight,0);
            end = new Vector3f(end.x(),end.y()-this.spacing-font.lineHeight,0);
            drawCenteredString(matrix,font,Translate.guiGeneric(false,"popup","error","title"),(int)center.x(),
                    (int)start.y(),red);
            start = new Vector3f(start.x(),start.y()-this.spacing,0);
            end = new Vector3f(end.x(),end.y()-this.spacing,0);
            GuiUtil.drawLine(start,end,white(255),1f,this.getBlitOffset());
        }
    }

    private void drawCanType(PoseStack matrix, Vector3f center, int mouseX, int mouseY, Font font) {
        int width = getTypeWidth(this.spacing*2,font);
        int totalHeight = (font.lineHeight*2)+(this.spacing*4);
        int boxHeight = font.lineHeight+(this.spacing*2);
        Vector3f topLeft = new Vector3f(center.x()-(((float)width)/2),center.y()-(((float)totalHeight)/2),0);
        drawSelectionBox(topLeft,width,boxHeight,false);
        drawCenteredString(matrix,font,this.title,(int)center.x(),(int)topLeft.y()+this.spacing,GuiUtil.WHITE);
        topLeft = new Vector3f(topLeft.x(),topLeft.y()+boxHeight,0);
        this.isHover = mouseHover(topLeft,mouseX,mouseY,width,boxHeight);
        drawSelectionBox(topLeft,width,boxHeight,this.isHover);
        int color = GuiUtil.WHITE;
        if(this.isHover) color = GuiUtil.makeRGBAInt(200,200,200,255);
        drawCenteredString(matrix,font,this.value+ChannelManager.blinkerChar, (int)center.x(),(int)(topLeft.y()+this.spacing),color);
        if(this.isHover) renderComponentTooltip(matrix,this.hoverText,mouseX,mouseY);
    }

    private int getTypeWidth(int padding, Font font) {
        int base = 200;
        int textWidth = padding+font.width(this.title);
        if(textWidth>base) base = textWidth;
        textWidth = padding+font.width(this.value);
        if(textWidth>base) base = textWidth;
        return base;
    }

    private void drawConfirmationBox(PoseStack matrix, Vector3f center, int mouseX, int mouseY, Font font) {
        int width = font.width(this.title)+4;
        int totalHeight = (font.lineHeight*2)+(this.spacing*4);
        int boxHeight = font.lineHeight+(this.spacing*2);
        Vector3f topLeft = new Vector3f(center.x()-(((float)width)/2),center.y()-(((float)totalHeight)/2),0);
        drawSelectionBox(topLeft,width,boxHeight,false);
        drawCenteredString(matrix,font,this.title,(int) center.x(),(int) topLeft.y()+this.spacing,GuiUtil.WHITE);
        topLeft = new Vector3f(topLeft.x(),topLeft.y()+boxHeight,0);
        String yes = Translate.guiGeneric(false,"popup","yes");
        String no = Translate.guiGeneric(false,"popup","no");
        this.hoverYes = mouseHover(topLeft,mouseX,mouseY,width/2,boxHeight);
        drawSelectionBox(topLeft,width/2,boxHeight,this.hoverYes);
        int color = GuiUtil.WHITE;
        if(this.hoverYes) color = GuiUtil.makeRGBAInt(200,200,200,255);
        drawCenteredString(matrix,font,yes,(int)(center.x()-(width/4)),(int)(topLeft.y()+this.spacing),color);
        topLeft = new Vector3f(center.x(),topLeft.y(),0);
        this.hoverNo = mouseHover(topLeft,mouseX,mouseY,width/2,boxHeight);
        drawSelectionBox(topLeft,width/2,boxHeight,this.hoverNo);
        color = GuiUtil.WHITE;
        if(this.hoverNo) color = GuiUtil.makeRGBAInt(200,200,200,255);
        drawCenteredString(matrix,font,no,(int)(center.x()+(width/4)),(int)(topLeft.y()+this.spacing),color);
    }

    private void drawSelectionBox(Vector3f topLeft, int width, int height, boolean hover) {
        if(hover) GuiUtil.drawBoxWithOutline(topLeft,width,height,new Vector4f(64,64,64,255),
                new Vector4f(255,255,255,255),1f,this.getBlitOffset());
        else GuiUtil.drawBoxWithOutline(topLeft,width,height,new Vector4f(0,0,0,255),
                new Vector4f(255,255,255,255),1f,this.getBlitOffset());
    }

    @Override
    protected void save() {

    }
}
