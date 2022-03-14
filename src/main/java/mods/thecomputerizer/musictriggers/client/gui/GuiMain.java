package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.MusicPicker;
import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import mods.thecomputerizer.musictriggers.client.eventsClient;
import mods.thecomputerizer.musictriggers.config.configObject;
import mods.thecomputerizer.musictriggers.util.json;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;

import java.io.IOException;

public class GuiMain extends GuiScreen {

    public boolean reload;
    private final ResourceLocation background;
    public configObject holder;

    public GuiMain(configObject holder) {
        this.reload = false;
        this.background = new ResourceLocation(MusicTriggers.MODID,"textures/block/recorder_side_active.png");
        this.holder = holder;
        this.holder.addAllExistingParameters();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX,mouseY,partialTicks);
        this.overlayBackground(0, 32, 255, 255);
        this.overlayBackground(this.height-32, this.height, 255, 255);
        this.drawCenteredString(this.fontRenderer, "Music Triggers in game config integration", this.width/2, 8, 10526880);
    }

    @Override
    public void initGui() {
        this.addApplyButton();
        this.addReloadButton();
        this.addAddTriggersButton();
        this.addEditTriggersButton();
        eventsClient.renderDebug = false;
    }

    private void addApplyButton() {
        GuiButton apply = new GuiButton(1, this.width / 2 +25, this.height / 2 - 30, "Apply Changes");
        apply.setWidth(150);
        this.buttonList.add(apply);
    }

    private void addReloadButton() {
        GuiButton reload = new GuiButton(2, this.width / 2 - 175, this.height / 2 - 30, "Reload");
        reload.setWidth(150);
        this.buttonList.add(reload);
    }

    private void addAddTriggersButton() {
        GuiButton triggers = new GuiButton(3, this.width / 2 - 175, this.height / 2 + 10, "Add Songs");
        triggers.setWidth(150);
        this.buttonList.add(triggers);
    }

    private void addEditTriggersButton() {
        GuiButton triggers = new GuiButton(4, this.width / 2 + 25, this.height / 2 + 10, "Edit Songs");
        triggers.setWidth(150);
        this.buttonList.add(triggers);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if(button.id==1) {
            try {
                this.holder.write();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Could not write new configuration to file! Check the log for the actual stacktrace");
            }
            this.reload = true;
            this.mc.displayGuiScreen(null);
            this.mc.setIngameFocus();
        }
        if(button.id==2) {
            this.reload = true;
            this.mc.displayGuiScreen(null);
            this.mc.setIngameFocus();
        }
        if(button.id==3) {
            this.mc.displayGuiScreen(new GuiAddSongs(this, json.allSongs, holder));
        }
        if(button.id==4) {
            this.mc.displayGuiScreen(new GuiEditSongs(this, holder));
        }
    }

    @Override
    public void onGuiClosed() {
        eventsClient.renderDebug = true;
        if(this.reload && this.mc.player!=null) {
            Minecraft.getMinecraft().getSoundHandler().stopSounds();
            MusicPicker.player.sendMessage(new TextComponentString("\u00A74\u00A7oReloading Music... This may take a while!"));
            MusicPlayer.reloading = true;
            eventsClient.reloadCounter = 5;
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
}
