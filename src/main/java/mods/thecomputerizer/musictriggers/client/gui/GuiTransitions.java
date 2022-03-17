package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.client.eventsClient;
import mods.thecomputerizer.musictriggers.config.configObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.List;

public class GuiTransitions extends GuiScreen {

    public List<String> info;
    public GuiScreen parentScreen;
    public GuiScrollingTransitions scrollingSongs;
    public configObject holder;

    public GuiTransitions(GuiScreen parentScreen, configObject holder) {
        this.parentScreen = parentScreen;
        this.holder = holder;
        this.info = this.holder.getAllTransitions();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        scrollingSongs.drawScreen(mouseX,mouseY,partialTicks);
        super.drawScreen(mouseX,mouseY,partialTicks);
        if(this.scrollingSongs.curSelected!=null && this.scrollingSongs.index!=-1) {
            String curInfo = this.holder.getAllTriggersForTransition(this.scrollingSongs.index);
            this.drawCenteredString(this.fontRenderer, curInfo, this.width / 2, 8, 10526880);
        } else this.drawCenteredString(this.fontRenderer, "Transitions", this.width/2, 8, 10526880);
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
        this.addAddTransitionButton();
        eventsClient.renderDebug = false;
    }

    private void addScrollable() {
        this.scrollingSongs = new GuiScrollingTransitions(this.mc, this.width, this.height,32,this.height-32, this.info,this);
        this.scrollingSongs.registerScrollButtons(7, 8);
    }

    private void addBackButton() {
        this.buttonList.add(new GuiButton(1, 16, 8, 64, 16,"Back"));
    }

    private void addAddTransitionButton() {
        this.buttonList.add(new GuiButton(2, this.width/2-64, this.height-24, 128, 16, "Add Transition"));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            ((GuiMain)this.parentScreen).holder = this.holder;
            this.mc.displayGuiScreen(this.parentScreen);
        }
        if (button.id == 2) {
            this.mc.displayGuiScreen(new GuiAddTransition(this, this.holder));
        }
    }

    @Override
    public void onGuiClosed() {
        eventsClient.renderDebug = true;
    }
}
