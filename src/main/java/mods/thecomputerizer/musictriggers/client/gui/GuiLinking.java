package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.config.ConfigObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.List;

public class GuiLinking extends GuiScreen {
    public List<String> songs;
    public String songCode;
    public GuiScreen parentScreen;
    public GuiScrollingLinking scrollingSongs;
    public ConfigObject holder;

    public GuiLinking(GuiScreen parentScreen, String songCode, ConfigObject holder) {
        this.parentScreen = parentScreen;
        this.songCode = songCode;
        this.holder = holder;
        this.songs = this.holder.getAllSongsForLinking(this.songCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        scrollingSongs.drawScreen(mouseX,mouseY,partialTicks);
        super.drawScreen(mouseX,mouseY,partialTicks);
        this.drawCenteredString(this.fontRenderer, "Linked Songs", this.width/2, 8, 10526880);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.scrollingSongs.handleMouseInput();
    }

    @Override
    public void initGui() {
        this.addBackButton();
        this.addAddSongButton();
        this.addSongs();
        EventsClient.renderDebug = false;
    }

    private void addSongs() {
        this.scrollingSongs = new GuiScrollingLinking(this.mc, this.width, this.height,32,this.height-32, this.songs,this);
        this.scrollingSongs.registerScrollButtons(7, 8);
    }

    private void addBackButton() {
        this.buttonList.add(new GuiButton(1, 16, 8, 64, 16,"Back"));
    }

    private void addAddSongButton() {
        this.buttonList.add(new GuiButton(2, this.width/2-64, this.height-24, 128, 16, "Add Song"));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            ((GuiSongInfo)this.parentScreen).holder = this.holder;
            this.mc.displayGuiScreen(this.parentScreen);
        }
        if (button.id == 2) {
            //this.mc.displayGuiScreen(new GuiAddSongs(this, Json.allSongs, this.holder, this));
        }
    }

    @Override
    public void onGuiClosed() {
        EventsClient.renderDebug = true;
    }

}
