package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.client.eventsClient;
import mods.thecomputerizer.musictriggers.config.configObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

public class GuiTriggerInfo extends GuiScreen {

    public String trigger;
    public String songCode;
    public List<String> info;
    public List<String> parameters;
    public GuiScreen parentScreen;
    public GuiScrollingTriggerInfo scrollingSongs;
    public configObject holder;

    public GuiTriggerInfo(GuiScreen parentScreen, String trigger, String songCode, configObject holder, boolean create) {
        this.parentScreen = parentScreen;
        this.trigger = trigger;
        this.songCode = songCode;
        this.holder = holder;
        if(create) {
            this.holder.addTrigger(songCode,trigger);
        }
        this.parameters = Mappings.convertList(Mappings.buildGuiParameters(trigger));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        scrollingSongs.drawScreen(mouseX,mouseY,partialTicks);
        super.drawScreen(mouseX,mouseY,partialTicks);
        if(this.scrollingSongs.curSelected!=null && this.scrollingSongs.index!=-1) {
            String curInfo = this.holder.getTriggerInfoAtIndex(this.songCode, this.trigger, this.scrollingSongs.index);
            this.drawCenteredString(this.fontRenderer, curInfo, this.width / 2, 8, 10526880);
        } else this.drawCenteredString(this.fontRenderer, this.trigger, this.width/2, 8, 10526880);
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
                int index = this.scrollingSongs.index;
                if(keyCode==14 && !this.holder.getTriggerInfoAtIndex(this.songCode, this.trigger, this.scrollingSongs.index).matches("")) this.holder.editTriggerInfoParameter(this.songCode, this.trigger, index, StringUtils.chop(this.holder.getTriggerInfoAtIndex(this.songCode, this.trigger, this.scrollingSongs.index)));
                else if(keyCode!=14) this.holder.editTriggerInfoParameter(this.songCode, this.trigger, index, this.holder.getTriggerInfoAtIndex(this.songCode, this.trigger, this.scrollingSongs.index)+typedChar);
            }
        }
    }

    @Override
    public void initGui() {
        this.addBackButton();
        this.addScrollable();
        this.addDeleteButton();
        eventsClient.renderDebug = false;
    }

    private void addScrollable() {
        this.scrollingSongs = new GuiScrollingTriggerInfo(this.mc, this.width, this.height,32,this.height-32, this.parameters,this, this.holder);
        this.scrollingSongs.registerScrollButtons(7, 8);
    }

    private void addBackButton() {
        this.buttonList.add(new GuiButton(1, 16, 8, 64, 16,"Back"));
    }

    private void addDeleteButton() {
        GuiButton back = new GuiButton(2, this.width-80, 8, "\u00A74Delete");
        back.setWidth(64);
        back.height = 16;
        this.buttonList.add(back);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            if(this.parentScreen instanceof GuiSongInfo) {
                ((GuiSongInfo)this.parentScreen).holder = this.holder;
            } else {
                ((GuiTriggers)this.parentScreen).holder = this.holder;
            }
            this.mc.displayGuiScreen(this.parentScreen);
        }
        if (button.id == 2) {
            if(this.parentScreen instanceof GuiSongInfo) {
                GuiSongInfo parent = ((GuiSongInfo)this.parentScreen);
                this.holder.removeTrigger(this.songCode,this.trigger);
                parent.holder = this.holder;
                parent.triggers = this.holder.getAllTriggersForCode(this.songCode);
            } else {
                GuiTriggers parent = ((GuiTriggers)this.parentScreen);
                this.holder.removeTrigger(this.songCode,this.trigger);
                parent.holder = this.holder;
            }
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    @Override
    public void onGuiClosed() {
        eventsClient.renderDebug = true;
    }
}
