package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class GuiScrollingTransitions extends GuiSlot {

    public int size;
    public List<String> info;
    private final GuiTransitions IN;
    public String curSelected;
    private final ResourceLocation background;
    public int index;

    public GuiScrollingTransitions(Minecraft client, int width, int height, int top, int bottom, List<String> info, GuiTransitions IN) {
        super(client, width, height, top, bottom, 32);
        this.size = info.size();
        this.info = info;
        this.IN = IN;
        this.background = new ResourceLocation(MusicTriggers.MODID,"textures/block/recorder_side_active.png");
    }

    @Override protected int getSize() {
        return this.size;
    }

    @Override
    public void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        this.index = slotIndex;
        this.curSelected = this.info.get(slotIndex)+"-"+slotIndex;
        this.mc.displayGuiScreen(new GuiTransitionInfo(this.IN, this.IN.holder, slotIndex, false, this.IN.holder.isTitle(slotIndex), false, ""));
    }

    @Override protected boolean isSelected(int index) {
        if(this.curSelected==null) {
            return false;
        }
        else {
            return index==Integer.parseInt(curSelected.substring(curSelected.length()-1));
        }
    }

    @Override
    public void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn, float partialTicks) {
        this.IN.drawCenteredString(this.mc.fontRenderer, this.info.get(slotIndex), this.width / 2, yPos + 1, 16777215);
    }

    @Override protected void drawBackground() {}

    @Override
    protected void drawContainerBackground(Tessellator tessellator) {}

    @Override
    protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        this.mc.getTextureManager().bindTexture(this.background);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(this.left, endY, 0.0D).tex(0.0D, (float)endY / 32.0F).color(64, 64, 64, endAlpha).endVertex();
        bufferbuilder.pos(this.left + this.width, endY, 0.0D).tex((float)this.width / 32.0F, (float)endY / 32.0F).color(64, 64, 64, endAlpha).endVertex();
        bufferbuilder.pos(this.left + this.width, startY, 0.0D).tex((float)this.width / 32.0F, (float)startY / 32.0F).color(64, 64, 64, startAlpha).endVertex();
        bufferbuilder.pos(this.left, startY, 0.0D).tex(0.0D, (float)startY / 32.0F).color(64, 64, 64, startAlpha).endVertex();
        tessellator.draw();
    }
}
