package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.client.eventsClient;
import mods.thecomputerizer.musictriggers.config.configObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GuiSongInfo extends GuiScreen {

    public String song;
    public String songCode;
    public List<String> info;
    public List<String> triggers;
    public GuiScreen parentScreen;
    public GuiScrollingInfo scrollingSongs;
    public List<String> parameters;
    public configObject holder;

    public GuiSongInfo(GuiScreen parentScreen, String song, String songCode, configObject holder) {
        this.parentScreen = parentScreen;
        this.song = song;
        this.songCode = songCode;
        this.parameters = Arrays.stream(new String[]{"pitch","play_once","must_finish","chance","volume"}).collect(Collectors.toList());
        this.holder = holder;
        this.triggers = this.holder.getAllTriggersForCode(this.songCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        scrollingSongs.drawScreen(mouseX,mouseY,partialTicks);
        super.drawScreen(mouseX,mouseY,partialTicks);
        if(this.scrollingSongs.curSelected!=null && this.scrollingSongs.index!=-1) {
            String curInfo = this.holder.getSongInfoAtIndex(this.songCode, this.scrollingSongs.index);
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
                if(keyCode==14 && !this.holder.getSongInfoAtIndex(this.songCode, this.scrollingSongs.index).matches("")) this.holder.editOtherInfoParameter(this.songCode, index, StringUtils.chop(this.holder.getSongInfoAtIndex(this.songCode, this.scrollingSongs.index)));
                else if(keyCode!=14) this.holder.editOtherInfoParameter(this.songCode, index, this.holder.getSongInfoAtIndex(this.songCode, this.scrollingSongs.index)+typedChar);
            }
        }
    }

    @Override
    public void initGui() {
        this.addBackButton();
        this.addScrollable();
        this.addAddTriggerButton();
        this.addLinkingButton();
        this.addDeleteButton();
        eventsClient.renderDebug = false;
    }

    private void addScrollable() {
        List<String> everything = new ArrayList<>();
        everything.addAll(this.parameters);
        everything.addAll(this.triggers);
        this.scrollingSongs = new GuiScrollingInfo(this.mc, this.width, this.height,32,this.height-32, everything,this);
        this.scrollingSongs.registerScrollButtons(7, 8);
    }

    private void addBackButton() {
        this.buttonList.add(new GuiButton(1, 16, 8, 64, 16,"Back"));
    }

    private void addDeleteButton() {
        this.buttonList.add(new GuiButton(2, this.width-80, 8, 64, 16, "\u00A74Delete"));
    }

    private void addAddTriggerButton() {
        this.buttonList.add(new GuiButton(3, this.width/2-114, this.height-24, 96, 16, "Add Trigger"));
    }

    private void addLinkingButton() {
        this.buttonList.add(new GuiButton(4, this.width/2+16, this.height-24, 96, 16, "Trigger Linking"));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            if(this.parentScreen instanceof GuiAddSongs) {
                ((GuiAddSongs)this.parentScreen).holder = this.holder;
            } else {
                ((GuiEditSongs)this.parentScreen).holder = this.holder;
            }
            this.mc.displayGuiScreen(this.parentScreen);
        }
        if (button.id == 2) {
            if(this.parentScreen instanceof GuiAddSongs) {
                GuiAddSongs parent = (GuiAddSongs)this.parentScreen;
                parent.holder.removeSong(this.songCode);
            } else {
                GuiEditSongs parent = ((GuiEditSongs)this.parentScreen);
                parent.holder.removeSong(this.songCode);
                parent.songs = parent.holder.getAllSongs();
                parent.codes = parent.holder.getAllCodes();
            }
            this.mc.displayGuiScreen(this.parentScreen);
        }
        if (button.id == 3) {
            this.mc.displayGuiScreen(new GuiTriggers(this, this.holder, this.songCode));
        }
        if (button.id == 4) {
            this.mc.displayGuiScreen(new GuiLinking(this, this.songCode, this.holder));
        }
    }

    @Override
    public void onGuiClosed() {
        eventsClient.renderDebug = true;
    }
}
