package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.config.ConfigObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GuiChooseImage extends GuiScreen {

    public Map<String, Boolean> imageMap;
    public List<String> images;
    public GuiScreen parentScreen;
    public GuiScrollingChooseImage scrollingSongs;
    public String curInfo = "";
    public ConfigObject holder;

    public GuiChooseImage(GuiScreen parentScreen, ConfigObject holder) {
        this.parentScreen = parentScreen;
        this.holder = holder;
        this.imageMap = this.holder.getAllImages();
        this.images = this.holder.extractStringListFromMapKeys(this.imageMap);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        scrollingSongs.drawScreen(mouseX,mouseY,partialTicks);
        super.drawScreen(mouseX,mouseY,partialTicks);
        this.drawCenteredString(this.fontRenderer, "Add an image file", this.width/2, 8, 10526880);
        this.drawCenteredString(this.fontRenderer, this.curInfo, this.width/2, this.height-8, 10526880);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.scrollingSongs.handleMouseInput();
    }

    @Override
    public void initGui() {
        this.addBackButton();
        this.addSongs();
        EventsClient.renderDebug = false;
    }

    private void addSongs() {
        this.scrollingSongs = new GuiScrollingChooseImage(this.mc, this.width, this.height,32,this.height-32, this.images, this, this.imageMap);
        this.scrollingSongs.registerScrollButtons(7, 8);
    }

    private void addBackButton() {
        this.buttonList.add(new GuiButton(1, 16, 8, 64, 16,"Back"));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            ((GuiAddTransition)this.parentScreen).holder = this.holder;
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    @Override
    public void onGuiClosed() {
        EventsClient.renderDebug = true;
    }
}
