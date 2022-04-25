package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.config.ConfigObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

public class GuiLoopInfo extends GuiScreen {

    public List<String> info;
    public GuiScreen parentScreen;
    public GuiScrollingLoopsInfo scrollingSongs;
    public ConfigObject holder;
    public int loopIndex;
    public String code;
    public String song;
    public boolean linked;

    public GuiLoopInfo(GuiScreen parentScreen, ConfigObject holder, int loopIndex, String code, String song, boolean linked) {
        this.parentScreen = parentScreen;
        this.holder = holder;
        this.loopIndex = loopIndex;
        this.code = code;
        this.song = song;
        this.linked = linked;
        this.info = this.holder.getAllLoopInfo();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        scrollingSongs.drawScreen(mouseX,mouseY,partialTicks);
        super.drawScreen(mouseX,mouseY,partialTicks);
        String curInfo;
        if(!linked) curInfo = "Loop Info";
        else curInfo = "Linked Loop Info";
        if(this.scrollingSongs.curSelected!=null && this.scrollingSongs.index!=-1) curInfo = this.holder.getLoopParameter(this.code,this.song,this.linked,this.loopIndex,this.scrollingSongs.index);
        this.drawCenteredString(this.fontRenderer, curInfo, this.width / 2, 8, 10526880);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.scrollingSongs.handleMouseInput();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar,keyCode);
        if(this.scrollingSongs.curSelected!=null) {
            if(keyCode>=2 && keyCode<=14 || keyCode>=16 && keyCode<=25 || keyCode>=30 && keyCode<=38 || keyCode>=44 && keyCode<=52 || keyCode==57) {
                if(keyCode==14 && !this.holder.getLoopParameter(this.code,this.song,this.linked,this.loopIndex,this.scrollingSongs.index).matches("")) this.holder.editLoopInfoAtIndex(this.code,this.song,this.linked,this.loopIndex,this.scrollingSongs.index, StringUtils.chop(this.holder.getLoopParameter(this.code,this.song,this.linked,this.loopIndex,this.scrollingSongs.index)));
                else if(keyCode!=14) this.holder.editLoopInfoAtIndex(this.code,this.song,this.linked,this.loopIndex,this.scrollingSongs.index, this.holder.getLoopParameter(this.code,this.song,this.linked,this.loopIndex,this.scrollingSongs.index)+typedChar);
            }
        }
    }

    @Override
    public void initGui() {
        this.addBackButton();
        this.addDeleteButton();
        this.addScrollable();
        EventsClient.renderDebug = false;
    }

    private void addScrollable() {
        this.scrollingSongs = new GuiScrollingLoopsInfo(this.mc, this.width, this.height,32,this.height-32, this.info,this);
        this.scrollingSongs.registerScrollButtons(7, 8);
    }

    private void addBackButton() {
        this.buttonList.add(new GuiButton(1, 16, 8, 64, 16,"Back"));
    }

    private void addDeleteButton() {
        this.buttonList.add(new GuiButton(2, this.width-80, 8, 64, 16, "\u00A74Delete"));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            ((GuiLoops)this.parentScreen).holder = this.holder;
            this.mc.displayGuiScreen(this.parentScreen);
        }
        if (button.id == 2) {
            this.holder.removeLoop(this.code,this.song,this.linked,this.loopIndex);
            ((GuiLoops)this.parentScreen).holder = this.holder;
            ((GuiLoops)this.parentScreen).info = this.holder.getAllLoops(this.code,this.song,this.linked);
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    @Override
    public void onGuiClosed() {
        EventsClient.renderDebug = true;
    }
}
