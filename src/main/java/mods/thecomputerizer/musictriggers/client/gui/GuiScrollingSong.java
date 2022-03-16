package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.config.configObject;
import mods.thecomputerizer.musictriggers.config.configToml;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("NullableProblems")
public class GuiScrollingSong extends GuiSlot {

    private final int size;
    private final List<String> songs;
    private final List<String> codes;
    private final GuiScreen IN;
    private String curSelected;
    private final ResourceLocation background;
    private final ResourceLocation darken;
    private final configObject holder;
    private GuiLinking linking = null;

    public GuiScrollingSong(Minecraft client, int width, int height, int top, int bottom, List<String> songs, List<String> codes, GuiScreen IN, configObject holder, GuiLinking linking) {
        super(client, width, height, top, bottom, 32);
        this.size = songs.size();
        this.songs = songs;
        this.codes = codes;
        this.IN = IN;
        this.background = new ResourceLocation(MusicTriggers.MODID,"textures/block/recorder_side_active.png");
        this.darken = new ResourceLocation(MusicTriggers.MODID,"textures/gui/background.png");
        this.holder = holder;
        if(linking!=null) this.linking = linking;
    }

    @Override protected int getSize() {
        return this.size;
    }

    @Override
    public void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        this.curSelected = this.songs.get(slotIndex)+"-"+slotIndex;
        if(isDoubleClick) {
            if(this.linking==null) {
                if (this.codes != null) {
                    String code = this.codes.get(slotIndex);
                    this.mc.displayGuiScreen(new GuiSongInfo(this.IN, this.songs.get(slotIndex), code, this.holder));
                } else {
                    this.mc.displayGuiScreen(new GuiSongInfo(this.IN, this.songs.get(slotIndex), this.holder.addSong(this.songs.get(slotIndex)), this.holder));
                }
            }
            else {
                this.holder.addLinkingSong(this.linking.songCode, this.songs.get(slotIndex));
                this.mc.displayGuiScreen(new GuiLinkingInfo(this.IN, this.songs.get(slotIndex), this.linking.songCode, this.holder));
            }
        }
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
}
