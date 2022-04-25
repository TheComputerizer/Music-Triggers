package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.config.ConfigObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class GuiAddTransition extends GuiScreen {

    public GuiScreen parentScreen;
    private final ResourceLocation background;
    public ConfigObject holder;

    public GuiAddTransition(GuiScreen parentScreen, ConfigObject holder) {
        this.parentScreen = parentScreen;
        this.holder = holder;
        this.background = new ResourceLocation(MusicTriggers.MODID,"textures/block/recorder_side_active.png");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.overlayBackground(0, 32, 255, 255);
        this.overlayBackground(this.height-32, this.height, 255, 255);
        super.drawScreen(mouseX,mouseY,partialTicks);
        this.drawCenteredString(this.fontRenderer, "Transitions", this.width/2, 8, 10526880);
    }

    @Override
    public void initGui() {
        this.addBackButton();
        this.addTitleCardButton();
        this.addImageCardButton();
        EventsClient.renderDebug = false;
    }

    private void addBackButton() {
        this.buttonList.add(new GuiButton(1, 16, 8, 64, 16,"Back"));
    }

    private void addTitleCardButton() {
        this.buttonList.add(new GuiButton(2, this.width / 2 - 175, this.height / 2 - 10, 150, 20, "Title Card"));
    }

    private void addImageCardButton() {
        this.buttonList.add( new GuiButton(3, this.width / 2 + 25, this.height / 2 - 10, 150, 20,"Image Card"));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            ((GuiTransitions)this.parentScreen).holder = this.holder;
            this.mc.displayGuiScreen(this.parentScreen);
        }
        if (button.id == 2) {
            this.mc.displayGuiScreen(new GuiTransitionInfo(this, holder, 0, true, true, false, ""));
        }
        if (button.id == 3) {
            this.mc.displayGuiScreen(new GuiChooseImage(this, this.holder));
        }
    }

    protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        this.mc.getTextureManager().bindTexture(this.background);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(0, endY, 0.0D).tex(0.0D, (float)endY / 32.0F).color(64, 64, 64, endAlpha).endVertex();
        bufferbuilder.pos(this.width, endY, 0.0D).tex((float)this.width / 32.0F, (float)endY / 32.0F).color(64, 64, 64, endAlpha).endVertex();
        bufferbuilder.pos(this.width, startY, 0.0D).tex((float)this.width / 32.0F, (float)startY / 32.0F).color(64, 64, 64, startAlpha).endVertex();
        bufferbuilder.pos(0, startY, 0.0D).tex(0.0D, (float)startY / 32.0F).color(64, 64, 64, startAlpha).endVertex();
        tessellator.draw();
    }

    @Override
    public void onGuiClosed() {
        EventsClient.renderDebug = true;
    }
}
