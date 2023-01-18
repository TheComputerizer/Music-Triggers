package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.SoundEvents;
import org.lwjgl.input.Keyboard;

import javax.vecmath.Point2i;
import javax.vecmath.Point4i;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class GuiSuperType extends GuiScreen {

    protected static final List<Integer> blacklistedKeys = Stream.of(Keyboard.KEY_LSHIFT, Keyboard.KEY_RSHIFT,
            Keyboard.KEY_TAB, Keyboard.KEY_CAPITAL, Keyboard.KEY_LMETA, Keyboard.KEY_RMETA, Keyboard.KEY_END, Keyboard.KEY_UP,
            Keyboard.KEY_DOWN, Keyboard.KEY_LEFT, Keyboard.KEY_RIGHT, Keyboard.KEY_F2, Keyboard.KEY_F3, Keyboard.KEY_F4,
            Keyboard.KEY_F5, Keyboard.KEY_F6, Keyboard.KEY_F7, Keyboard.KEY_F8, Keyboard.KEY_F9, Keyboard.KEY_F10,
            Keyboard.KEY_F11, Keyboard.KEY_F12, Keyboard.KEY_F13, Keyboard.KEY_F14, Keyboard.KEY_F15, Keyboard.KEY_F16,
            Keyboard.KEY_F17, Keyboard.KEY_F18, Keyboard.KEY_F19, Keyboard.KEY_FUNCTION, Keyboard.KEY_APPS,
            Keyboard.KEY_POWER, Keyboard.KEY_SLEEP, Keyboard.KEY_CLEAR, Keyboard.KEY_HOME, Keyboard.KEY_PAUSE,
            Keyboard.KEY_PRIOR, Keyboard.KEY_INSERT, Keyboard.KEY_DELETE, Keyboard.KEY_NUMPADENTER, Keyboard.KEY_LMENU,
            Keyboard.KEY_RMENU, Keyboard.KEY_SYSRQ, Keyboard.KEY_STOP, Keyboard.KEY_SCROLL, Keyboard.KEY_NUMLOCK,
            Keyboard.KEY_RETURN, Keyboard.KEY_LCONTROL, Keyboard.KEY_RCONTROL, Keyboard.KEY_NEXT, Keyboard.KEY_NONE,
            Keyboard.KEY_ESCAPE).collect(Collectors.toList());
    protected static final List<Integer> numberKeys = Stream.of(Keyboard.KEY_BACK, Keyboard.KEY_1, Keyboard.KEY_2,
            Keyboard.KEY_3, Keyboard.KEY_4, Keyboard.KEY_5, Keyboard.KEY_6, Keyboard.KEY_7, Keyboard.KEY_8,
            Keyboard.KEY_9, Keyboard.KEY_0, Keyboard.KEY_NUMPAD1, Keyboard.KEY_NUMPAD2, Keyboard.KEY_NUMPAD3,
            Keyboard.KEY_NUMPAD4, Keyboard.KEY_NUMPAD5, Keyboard.KEY_NUMPAD6, Keyboard.KEY_NUMPAD7, Keyboard.KEY_NUMPAD8,
            Keyboard.KEY_NUMPAD9, Keyboard.KEY_NUMPAD0, Keyboard.KEY_SUBTRACT).collect(Collectors.toList());
    protected final GuiSuperType parent;
    protected final GuiType type;
    private final Instance configInstance;
    private final List<ButtonSuperType> superButtons;
    protected final int spacing;
    private int buttonIDCounter;

    public GuiSuperType(GuiSuperType parent, GuiType type, Instance configInstance) {
        this.parent = parent;
        this.type = type;
        this.configInstance = configInstance;
        this.superButtons = new ArrayList<>();
        this.spacing = 16;
    }

    public Instance getInstance() {
        return this.configInstance;
    }

    public GuiSuperType getParent() {
        return this.parent;
    }

    public Point4i white(int alpha) {
        return new Point4i(255,255,255,alpha);
    }

    public Point4i black(int alpha) {
        return new Point4i(0,0,0,alpha);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if(!getInstance().hasEdits()) {
                this.mc.displayGuiScreen(null);
                if (this.mc.currentScreen == null) this.mc.setIngameFocus();
            } else this.mc.displayGuiScreen(new GuiPopUp(this,GuiType.POPUP,getInstance(),"confirm"));
        }
    }

    protected String backspace(String value) {
        if(!value.isEmpty()) return value.substring(0,value.length()-1);
        return value;
    }

    @Override
    public void initGui() {
        EventsClient.renderDebug = false;
        for(ButtonType buttonHolder : this.type.getButtonHolders())
            if(buttonHolder.isNormal())
                addSuperButton(buttonHolder.getNormalButton(this.buttonIDCounter++, this));
    }

    protected ButtonSuperType addTopButton(int x, String name, int width, List<String> hoverText,
                                BiConsumer<GuiSuperType, ButtonSuperType> handler, GuiSuperType parent) {
        ButtonSuperType newButton = new ButtonSuperType(this.buttonIDCounter++,x,8,width,16, name,hoverText,
                handler,parent,false);
        this.addSuperButton(newButton);
        return newButton;
    }

    protected void addSuperButton(ButtonSuperType button) {
        this.buttonList.add(button);
        this.superButtons.add(button);
    }

    public void madeChange(boolean needReload) {
        recursivelySetApply(this);
        getInstance().madeChanges(needReload);
    }

    private void recursivelySetApply(GuiSuperType superScreen) {
        for(ButtonSuperType button : superScreen.superButtons)
            if(button.isApplyButton())
                button.setEnable(true);
        if(superScreen.parent!=null) recursivelySetApply(superScreen.parent);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    protected abstract void drawStuff(int mouseX, int mouseY, float partialTicks);

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        drawStuff(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    protected boolean mouseHover(Point2i topLeft, int mouseX, int mouseY, int width, int height) {
        return mouseX>=topLeft.x && mouseX<topLeft.x+width && mouseY>=topLeft.y && mouseY<topLeft.y+height;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            for (ButtonSuperType superButton : this.superButtons) {
                if (superButton.mousePressed(this.mc, mouseX, mouseY)) {
                    this.selectedButton = superButton;
                    superButton.playPressSound(this.mc.getSoundHandler());
                    superButton.handle();
                }
            }
        }
    }

    public void saveAndDisplay(GuiSuperType next) {
        save();
        this.mc.displayGuiScreen(next);
    }

    public void parentUpdate() {

    }

    protected abstract void save();

    public void saveAndClose(boolean reload) {
        save();
        if(reload) {
            if(this.configInstance.hasEdits())
                applyButton();
            EventsClient.initReload();
            this.mc.displayGuiScreen(null);
            Minecraft.getMinecraft().setIngameFocus();
        } else this.mc.displayGuiScreen(this.parent);
    }

    public void applyButton() {
        if(this.parent==null)
            getInstance().writeAndReload();
        else this.parent.applyButton();
    }

    @Override
    public void onGuiClosed() {
        EventsClient.renderDebug = true;
    }

    public void playGenericClickSound() {
        Minecraft.getMinecraft().getSoundHandler().playSound(
                PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
