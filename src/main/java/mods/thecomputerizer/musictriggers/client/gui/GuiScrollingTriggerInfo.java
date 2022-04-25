package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.config.ConfigObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import java.util.List;

@SuppressWarnings("NullableProblems")
public class GuiScrollingTriggerInfo extends GuiSlot {

    private final int size;
    private final List<String> parameters;
    private final GuiTriggerInfo IN;
    public String curSelected;
    public int index;
    public ConfigObject holder;
    private final ResourceLocation background;

    public GuiScrollingTriggerInfo(Minecraft client, int width, int height, int top, int bottom, List<String> parameters, GuiTriggerInfo IN, ConfigObject holder) {
        super(client, width, height, top, bottom, 32);
        this.size = parameters.size();
        this.parameters = parameters;
        this.IN = IN;
        this.holder = holder;
        this.background = new ResourceLocation(MusicTriggers.MODID,"textures/block/recorder_side_active.png");
    }

    @Override protected int getSize() {
        return this.size;
    }

    @Override
    public void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        this.index = Mappings.reverseparameters.get(this.parameters.get(slotIndex));
        this.curSelected = this.parameters.get(slotIndex)+"-"+slotIndex;
        if(this.parameters.get(slotIndex).matches("zone")) {
            EventsClient.parentScreen = this.IN;
            EventsClient.zone = true;
            this.mc.displayGuiScreen(null);
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
        this.IN.drawCenteredString(this.mc.fontRenderer, this.parameters.get(slotIndex), this.width / 2, yPos + 1, 16777215);
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
