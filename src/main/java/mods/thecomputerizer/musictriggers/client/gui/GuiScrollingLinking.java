package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.config.ConfigObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class GuiScrollingLinking extends GuiSlot {

    private final int size;
    private final List<String> songs;
    private final GuiLinking IN;
    public String curSelected;
    private final ResourceLocation background;
    public int index;
    public GuiScrollingLinking(Minecraft client, int width, int height, int top, int bottom, List<String> songs, GuiLinking IN) {
        super(client, width, height, top, bottom, 32);
        this.size = songs.size();
        this.songs = songs;
        this.IN = IN;
        this.background = new ResourceLocation(MusicTriggers.MODID,"textures/block/recorder_side_active.png");
    }

    @Override protected int getSize() {
        return this.size;
    }

    @Override
    public void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        this.index = getIndex(this.songs.get(slotIndex));
        this.curSelected = this.songs.get(slotIndex)+"-"+slotIndex;
        this.mc.displayGuiScreen(new GuiLinkingInfo(this.IN, this.songs.get(slotIndex), this.IN.songCode, this.IN.holder));
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
        this.IN.drawCenteredString(this.mc.fontRenderer, this.songs.get(slotIndex), this.width / 2, yPos + 1, 16777215);
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

    public int getIndex(String s){
        return this.songs.indexOf(s);
    }
}
