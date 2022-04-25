package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import mods.thecomputerizer.musictriggers.config.ConfigObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GuiOther extends GuiScreen {

    public String song;
    public String songCode;
    public List<String> info;
    public GuiScreen parentScreen;
    public GuiScrollingOther scrollingSongs;
    public ConfigObject holder;

    public GuiOther(GuiScreen parentScreen, ConfigObject holder) {
        this.parentScreen = parentScreen;
        this.holder = holder;
        this.info = this.holder.getAllDebugStuff();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        scrollingSongs.drawScreen(mouseX,mouseY,partialTicks);
        super.drawScreen(mouseX,mouseY,partialTicks);
        if(this.scrollingSongs.curSelected!=null && this.scrollingSongs.index!=-1) {
            String curInfo = this.holder.getOtherInfoAtIndex(this.scrollingSongs.index);
            this.drawCenteredString(this.fontRenderer, curInfo, this.width / 2, 8, 10526880);
        } else this.drawCenteredString(this.fontRenderer, this.song, this.width/2, 8, 10526880);
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
                if(keyCode==14 && !this.holder.getOtherInfoAtIndex(this.scrollingSongs.index).matches("")) this.holder.editOtherInfoAtIndex(this.scrollingSongs.index, StringUtils.chop(this.holder.getOtherInfoAtIndex(this.scrollingSongs.index)));
                else if(keyCode!=14) this.holder.editOtherInfoAtIndex(this.scrollingSongs.index, this.holder.getOtherInfoAtIndex(this.scrollingSongs.index)+typedChar);
            }
        }
    }

    @Override
    public void initGui() {
        this.addBackButton();
        addRefreshDebugButton();
        this.addScrollable();
        EventsClient.renderDebug = false;
    }

    private void addScrollable() {
        this.scrollingSongs = new GuiScrollingOther(this.mc, this.width, this.height,32,this.height-32, this.info,this, this.holder);
        this.scrollingSongs.registerScrollButtons(7, 8);
    }

    private void addBackButton() {
        this.buttonList.add(new GuiButton(1, 16, 8, 64, 16,"Back"));
    }

    private void addRefreshDebugButton() {
        this.buttonList.add(new GuiButton(2, this.width/2-64, this.height-24, 128, 16, "Refresh Debug"));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            ((GuiMain)this.parentScreen).holder = this.holder;
            this.mc.displayGuiScreen(this.parentScreen);
        }
        if (button.id == 2) {
            try {
                this.holder.writeOther();
                ConfigDebug.parse(new File("config/MusicTriggers/debug.toml"));
                this.mc.displayGuiScreen(null);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onGuiClosed() {
        EventsClient.renderDebug = true;
    }
}
