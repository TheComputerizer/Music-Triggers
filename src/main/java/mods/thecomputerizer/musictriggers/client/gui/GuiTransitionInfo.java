package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.eventsClient;
import mods.thecomputerizer.musictriggers.config.configObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

public class GuiTransitionInfo extends GuiScreen {

    public String trigger;
    public int index;
    public List<String> parameters;
    public boolean title;
    public GuiScreen parentScreen;
    public GuiScrollingTransitionsInfo scrollingSongs;
    public configObject holder;

    public GuiTransitionInfo(GuiScreen parentScreen, configObject holder, int index, boolean create, boolean title, boolean ismoving, String name) {
        this.parentScreen = parentScreen;
        this.index = index;
        this.holder = holder;
        this.title = title;
        if(create) this.index = this.holder.addTransition(this.title, ismoving, name);
        MusicTriggers.logger.info(this.index);
        this.parameters = this.holder.getAllTransitionParametersAtIndex(this.title, this.index);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        scrollingSongs.drawScreen(mouseX,mouseY,partialTicks);
        super.drawScreen(mouseX,mouseY,partialTicks);
        if(this.scrollingSongs.curSelected!=null && this.scrollingSongs.index!=-1) {
            String curInfo = this.holder.getTransitionInfoAtIndex(this.title, this.index, this.scrollingSongs.index);
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
                if(keyCode==14 && !this.holder.getTransitionInfoAtIndex(this.title, this.index, this.scrollingSongs.index).matches("")) this.holder.editTransitionInfoAtIndex(this.title, this.index, this.scrollingSongs.index, StringUtils.chop(this.holder.getTransitionInfoAtIndex(this.title, this.index, this.scrollingSongs.index)), typedChar, keyCode);
                else if(keyCode==14
                        && this.holder.getTransitionInfoAtIndex(this.title, this.index, this.scrollingSongs.index).matches("")
                        && this.holder.checkIfTransitionIndexIsTrigger(this.title, this.index, this.scrollingSongs.index)) {
                    this.holder.removeTransitionTrigger(this.title, this.index, this.scrollingSongs.index);
                    this.parameters = this.holder.getAllTransitionParametersAtIndex(this.title, this.index);
                    this.scrollingSongs.info = this.parameters;
                    this.scrollingSongs.setSize(this.parameters.size());
                }
                else if(keyCode!=14) this.holder.editTransitionInfoAtIndex(this.title, this.index, this.scrollingSongs.index, this.holder.getTransitionInfoAtIndex(this.title, this.index, this.scrollingSongs.index)+typedChar, typedChar, keyCode);
            }
        }
    }

    @Override
    public void initGui() {
        this.addBackButton();
        this.addScrollable();
        this.addDeleteButton();
        this.addAddTriggerButton();
        eventsClient.renderDebug = false;
    }

    private void addScrollable() {
        this.scrollingSongs = new GuiScrollingTransitionsInfo(this.mc, this.width, this.height,32,this.height-32, this.parameters, this);
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

    private void addAddTriggerButton() {
        this.buttonList.add(new GuiButton(3, this.width/2-64, this.height-24, 128, 16, "Add Trigger"));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            if(parentScreen instanceof GuiTransitions) {
                GuiTransitions parent = (GuiTransitions) this.parentScreen;
                parent.holder = this.holder;
                parent.info = this.holder.getAllTransitions();
                parent.scrollingSongs.info = parent.info;
                parent.scrollingSongs.size = parent.info.size();
            }
            else if(parentScreen instanceof GuiAddTransition) {
                ((GuiAddTransition) this.parentScreen).holder = this.holder;
                GuiTransitions parent = (GuiTransitions)((GuiAddTransition)this.parentScreen).parentScreen;
                parent.info = this.holder.getAllTransitions();
                parent.scrollingSongs.info = parent.info;
                parent.scrollingSongs.size = parent.info.size();
            }
            else {
                ((GuiChooseImage) this.parentScreen).holder = this.holder;
                GuiTransitions parent = (GuiTransitions)((GuiAddTransition)((GuiChooseImage)this.parentScreen).parentScreen).parentScreen;
                parent.info = this.holder.getAllTransitions();
                parent.scrollingSongs.info = parent.info;
                parent.scrollingSongs.size = parent.info.size();
            }
            this.mc.displayGuiScreen(this.parentScreen);
        }
        if (button.id == 2) {
            if(this.parentScreen instanceof GuiTransitions) {
                GuiTransitions parent = ((GuiTransitions) this.parentScreen);
                this.holder.removeTransition(this.title, this.index);
                parent.holder = this.holder;
                parent.info = parent.holder.getAllTransitions();
            } else if(this.parentScreen instanceof GuiAddTransition) {
                GuiTransitions parent = (GuiTransitions)((GuiAddTransition)this.parentScreen).parentScreen;
                this.holder.removeTransition(this.title, this.index);
                ((GuiAddTransition)this.parentScreen).holder = this.holder;
                parent.info = parent.holder.getAllTransitions();
            } else {
                GuiTransitions parent = (GuiTransitions)((GuiAddTransition)((GuiChooseImage)this.parentScreen).parentScreen).parentScreen;
                this.holder.removeTransition(this.title, this.index);
                ((GuiChooseImage)this.parentScreen).holder = this.holder;
                parent.info = parent.holder.getAllTransitions();
            }
            this.mc.displayGuiScreen(this.parentScreen);
        }
        if (button.id == 3) {
            this.holder.addTransitionTrigger(this.title, this.index);
            this.parameters = this.holder.getAllTransitionParametersAtIndex(this.title, this.index);
            this.scrollingSongs.info = this.parameters;
            this.scrollingSongs.setSize(this.parameters.size());
        }
    }

    @Override
    public void onGuiClosed() {
        eventsClient.renderDebug = true;
    }
}
