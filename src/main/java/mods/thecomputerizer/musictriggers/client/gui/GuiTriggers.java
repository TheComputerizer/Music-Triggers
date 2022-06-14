package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.config.ConfigObject;
import mods.thecomputerizer.musictriggers.config.ConfigMain;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GuiTriggers extends GuiScreen {

    public List<String> triggers;
    public GuiScreen parentScreen;
    public GuiScrollingTrigger scrollingSongs;
    public String curInfo = "";
    public String songCode;
    public ConfigObject holder;

    public GuiTriggers(GuiScreen parentScreen, ConfigObject holder, String songCode) {
        this.parentScreen = parentScreen;
        this.songCode = songCode;
        this.holder = holder;
        this.triggers = new ArrayList<>();
        this.triggers.addAll(Arrays.stream(ConfigMain.triggers).collect(Collectors.toList()));
        this.triggers.addAll(Arrays.stream(ConfigMain.modtriggers).collect(Collectors.toList()));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        scrollingSongs.drawScreen(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRenderer, "Select a trigger", this.width/2, 8, 10526880);
        this.drawCenteredString(this.fontRenderer, this.curInfo, this.width/2, this.height-8, 10526880);
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
        EventsClient.renderDebug = false;
    }

    private void addScrollable() {
        this.scrollingSongs = new GuiScrollingTrigger(this.mc, this.width, this.height, 32, this.height - 32, this.triggers, this, this.holder, this.songCode);
        this.scrollingSongs.registerScrollButtons(7, 8);
    }

    private void addBackButton() {
        this.buttonList.add(new GuiButton(1, 16, 8, 64, 16,"Back"));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            GuiSongInfo parent = ((GuiSongInfo)this.parentScreen);
            parent.holder = this.holder;
            parent.triggersCodes = this.holder.getAllTriggersForCode(this.songCode);
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    @Override
    public void onGuiClosed() {
        EventsClient.renderDebug = true;
    }
}
