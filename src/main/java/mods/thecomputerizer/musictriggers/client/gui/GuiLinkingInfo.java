package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.eventsClient;
import mods.thecomputerizer.musictriggers.config.configObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

public class GuiLinkingInfo extends GuiScreen {

    public String song;
    public String songCode;
    public List<String> info;
    public GuiScreen parentScreen;
    public GuiScrollingLinkingInfo scrollingSongs;
    public configObject holder;

    public GuiLinkingInfo(GuiScreen parentScreen, String song, String songCode, configObject holder) {
        this.parentScreen = parentScreen;
        this.song = song;
        this.songCode = songCode;
        this.holder = holder;
        this.info = this.holder.getAllLinkingInfo(this.songCode, this.song);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        scrollingSongs.drawScreen(mouseX,mouseY,partialTicks);
        super.drawScreen(mouseX,mouseY,partialTicks);
        if(this.scrollingSongs.curSelected!=null && this.scrollingSongs.index!=-1) {
            String curInfo = this.holder.getLinkingInfoAtIndex(this.songCode, this.song, this.scrollingSongs.index);
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
                int index = this.scrollingSongs.index;
                if(keyCode==14 && !this.holder.getLinkingInfoAtIndex(this.songCode, this.song, this.scrollingSongs.index).matches("")) this.holder.editLinkingInfoParameter(this.songCode, this.song, index, StringUtils.chop(this.holder.getLinkingInfoAtIndex(this.songCode, this.song, this.scrollingSongs.index)));
                else if(keyCode==14) {
                    if(this.holder.isLinkingInfoTrigger(this.songCode, this.song, index)) {
                        this.holder.removeLinkingTrigger(this.songCode, this.song, index);
                        this.scrollingSongs.refreshList(this.holder.getAllLinkingInfo(this.songCode, this.song));
                    }
                }
                else this.holder.editLinkingInfoParameter(this.songCode, this.song, index, this.holder.getLinkingInfoAtIndex(this.songCode, this.song, this.scrollingSongs.index)+typedChar);
            }
        }
    }

    @Override
    public void initGui() {
        this.addBackButton();
        this.addScrollable();
        addAddTriggerButton();
        this.addDeleteButton();
        eventsClient.renderDebug = false;
    }

    private void addScrollable() {
        this.scrollingSongs = new GuiScrollingLinkingInfo(this.mc, this.width, this.height,32,this.height-32, this.info,this);
        this.scrollingSongs.registerScrollButtons(7, 8);
    }

    private void addBackButton() {
        this.buttonList.add(new GuiButton(1, 16, 8, 64, 16,"Back"));
    }

    private void addDeleteButton() {
        this.buttonList.add(new GuiButton(2, this.width-80, 8, 64, 16, "\u00A74Delete"));
    }

    private void addAddTriggerButton() {
        this.buttonList.add(new GuiButton(3, this.width/2-64, this.height-24, 128, 16, "Add Trigger"));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            if(this.parentScreen instanceof GuiLinking) ((GuiLinking) this.parentScreen).holder = this.holder;
            else ((GuiAddSongs) this.parentScreen).holder = this.holder;
            this.mc.displayGuiScreen(this.parentScreen);
        }
        if (button.id == 2) {
            if(this.parentScreen instanceof GuiLinking) {
                GuiLinking parent = ((GuiLinking) this.parentScreen);
                parent.holder.removeLinkingSong(this.songCode, this.song);
                parent.songs = parent.holder.getAllSongsForLinking(this.songCode);
            } else ((GuiAddSongs) this.parentScreen).holder = this.holder;
            this.mc.displayGuiScreen(this.parentScreen);
        }
        if (button.id == 3) {
            this.holder.addLinkingTrigger(this.songCode, this.song, "trigger");
            this.info = this.holder.getAllLinkingInfo(this.songCode, this.song);
            this.scrollingSongs.refreshList(this.holder.getAllLinkingInfo(this.songCode, this.song));
        }
    }

    @Override
    public void onGuiClosed() {
        eventsClient.renderDebug = true;
    }
}
