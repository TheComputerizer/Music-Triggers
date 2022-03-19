package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.client.eventsClient;
import mods.thecomputerizer.musictriggers.config.configObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.List;

public class GuiAddSongs extends GuiScreen {

    public List<String> songs;
    public GuiScreen parentScreen;
    public GuiScrollingSong scrollingSongs;
    public String curInfo = "";
    public configObject holder;
    public GuiLinking linking = null;

    public GuiAddSongs(GuiScreen parentScreen, List<String> songs, configObject holder, GuiLinking linking) {
        this.parentScreen = parentScreen;
        this.songs = songs;
        this.holder = holder;
        if(linking!=null) this.linking = linking;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        scrollingSongs.drawScreen(mouseX,mouseY,partialTicks);
        super.drawScreen(mouseX,mouseY,partialTicks);
        this.drawCenteredString(this.fontRenderer, "Add a song", this.width/2, 8, 10526880);
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
        eventsClient.renderDebug = false;
    }

    private void addSongs() {
        this.scrollingSongs = new GuiScrollingSong(this.mc, this.width, this.height,32,this.height-32, this.songs, null,this, this.holder, this.linking);
        this.scrollingSongs.registerScrollButtons(7, 8);
    }

    private void addBackButton() {
        this.buttonList.add(new GuiButton(1, 16, 8, 64, 16,"Back"));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            if(this.parentScreen instanceof GuiMain) ((GuiMain)this.parentScreen).holder = this.holder;
            else  {
                GuiLinking parent = (GuiLinking)this.parentScreen;
                (parent).holder = this.holder;
                parent.songs = parent.holder.getAllSongsForLinking(parent.songCode);
            }
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    @Override
    public void onGuiClosed() {
        eventsClient.renderDebug = true;
    }
}
