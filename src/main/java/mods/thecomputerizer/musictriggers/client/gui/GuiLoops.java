package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.client.eventsClient;
import mods.thecomputerizer.musictriggers.config.configObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.List;

public class GuiLoops extends GuiScreen {

    public List<String> info;
    public GuiScreen parentScreen;
    public GuiScrollingLoops scrollingSongs;
    public configObject holder;
    public String code;
    public String song;
    public boolean linked;

    public GuiLoops(GuiScreen parentScreen, configObject holder, String code, String song, boolean linked) {
        this.parentScreen = parentScreen;
        this.holder = holder;
        this.code = code;
        this.song = song;
        this.linked = linked;
        this.info = this.holder.getAllLoops(this.code,this.song,this.linked);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        scrollingSongs.drawScreen(mouseX,mouseY,partialTicks);
        super.drawScreen(mouseX,mouseY,partialTicks);
        String curInfo;
        if(!linked) curInfo = "Loops";
        else curInfo = "Linked Loops";
        if(this.scrollingSongs.curSelected!=null && this.scrollingSongs.index!=-1) curInfo = this.holder.buildLoopTitle(this.code,this.song,this.linked,this.scrollingSongs.index);
        this.drawCenteredString(this.fontRenderer, curInfo, this.width / 2, 8, 10526880);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.scrollingSongs.handleMouseInput();
    }

    @Override
    public void initGui() {
        this.addBackButton();
        this.addScrollable();
        this.addAddLoopButton();
        eventsClient.renderDebug = false;
    }

    private void addScrollable() {
        this.scrollingSongs = new GuiScrollingLoops(this.mc, this.width, this.height,32,this.height-32, this.info,this, this.code, this.song, this.linked);
        this.scrollingSongs.registerScrollButtons(7, 8);
    }

    private void addBackButton() {
        this.buttonList.add(new GuiButton(1, 16, 8, 64, 16,"Back"));
    }

    private void addAddLoopButton() {
        this.buttonList.add(new GuiButton(2, this.width/2-64, this.height-24, 128, 16, "Add Loop"));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            if(!this.linked) ((GuiSongInfo)this.parentScreen).holder = this.holder;
            else ((GuiLinkingInfo)this.parentScreen).holder = this.holder;
            this.mc.displayGuiScreen(this.parentScreen);
        }
        if (button.id == 2) {
            this.holder.addLoop(this.code,this.song,this.linked);
            this.info = this.holder.getAllLoops(this.code,this.song,this.linked);
        }
    }

    @Override
    public void onGuiClosed() {
        eventsClient.renderDebug = true;
    }
}
