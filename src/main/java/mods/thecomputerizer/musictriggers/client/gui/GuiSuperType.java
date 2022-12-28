package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class GuiSuperType extends Screen {

    protected static final List<Integer> blacklistedKeys = Stream.of(GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT,
            GLFW.GLFW_KEY_TAB, GLFW.GLFW_KEY_CAPS_LOCK, GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT, GLFW.GLFW_KEY_END, GLFW.GLFW_KEY_UP,
            GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_F2, GLFW.GLFW_KEY_F3, GLFW.GLFW_KEY_F4,
            GLFW.GLFW_KEY_F5, GLFW.GLFW_KEY_F6, GLFW.GLFW_KEY_F7, GLFW.GLFW_KEY_F8, GLFW.GLFW_KEY_F9, GLFW.GLFW_KEY_F10,
            GLFW.GLFW_KEY_F11, GLFW.GLFW_KEY_F12, GLFW.GLFW_KEY_F13, GLFW.GLFW_KEY_F14, GLFW.GLFW_KEY_F15, GLFW.GLFW_KEY_F16,
            GLFW.GLFW_KEY_F17, GLFW.GLFW_KEY_F18, GLFW.GLFW_KEY_F19, GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL,
            GLFW.GLFW_KEY_NUM_LOCK, GLFW.GLFW_KEY_SCROLL_LOCK, GLFW.GLFW_KEY_PRINT_SCREEN, GLFW.GLFW_KEY_HOME, GLFW.GLFW_KEY_PAUSE,
            GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_INSERT, GLFW.GLFW_KEY_DELETE, GLFW.GLFW_KEY_RIGHT_SUPER, GLFW.GLFW_KEY_F20,
            GLFW.GLFW_KEY_F21, GLFW.GLFW_KEY_F22, GLFW.GLFW_KEY_F23, GLFW.GLFW_KEY_F24, GLFW.GLFW_KEY_F25, GLFW.GLFW_KEY_UNKNOWN,
            GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_END, GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_KP_ENTER).collect(Collectors.toList());
    protected static final List<Integer> numberKeys = Stream.of(GLFW.GLFW_KEY_BACKSPACE, GLFW.GLFW_KEY_1, GLFW.GLFW_KEY_2,
            GLFW.GLFW_KEY_3, GLFW.GLFW_KEY_4, GLFW.GLFW_KEY_5, GLFW.GLFW_KEY_6, GLFW.GLFW_KEY_7, GLFW.GLFW_KEY_8,
            GLFW.GLFW_KEY_9, GLFW.GLFW_KEY_0, GLFW.GLFW_KEY_KP_1, GLFW.GLFW_KEY_KP_2, GLFW.GLFW_KEY_KP_3,
            GLFW.GLFW_KEY_KP_4, GLFW.GLFW_KEY_KP_5, GLFW.GLFW_KEY_KP_6, GLFW.GLFW_KEY_KP_7, GLFW.GLFW_KEY_KP_8,
            GLFW.GLFW_KEY_KP_9, GLFW.GLFW_KEY_KP_0, GLFW.GLFW_KEY_KP_SUBTRACT).collect(Collectors.toList());
    protected final GuiSuperType parent;
    protected final GuiType type;
    private final Instance configInstance;
    private final List<ButtonSuperType> superButtons;
    protected final int spacing;

    public GuiSuperType(GuiSuperType parent, GuiType type, Instance configInstance) {
        super(new StringTextComponent(type.getId()));
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

    public Vector4f white(int alpha) {
        return new Vector4f(255,255,255,alpha);
    }

    public Vector4f black(int alpha) {
        return new Vector4f(0,0,0,alpha);
    }

    @Override
    public boolean keyPressed(int keyCode, int x, int y) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if(!getInstance().hasEdits()) {
                this.getMinecraft().setScreen(null);
            } else this.getMinecraft().setScreen(new GuiPopUp(this,GuiType.POPUP,getInstance(),"confirm"));
        }
        return true;
    }

    protected String backspace(String value) {
        if(!value.isEmpty()) return value.substring(0,value.length()-1);
        return value;
    }

    @Override
    public void init() {
        EventsClient.renderDebug = false;
        for(ButtonType buttonHolder : this.type.getButtonHolders())
            if(buttonHolder.isNormal())
                addSuperButton(buttonHolder.getNormalButton(this));
    }

    protected ButtonSuperType addTopButton(int x, String name, int width, List<String> hoverText,
                                BiConsumer<GuiSuperType, ButtonSuperType> handler, GuiSuperType parent) {
        ButtonSuperType newButton = new ButtonSuperType(x,8,width,16, name,hoverText,
                handler,parent,false);
        this.addSuperButton(newButton);
        return newButton;
    }

    protected void addSuperButton(ButtonSuperType button) {
        this.buttons.add(button);
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

    protected abstract void drawStuff(MatrixStack matrix, int mouseX, int mouseY, float partialTicks);

    @Override
    public void render(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrix);
        drawStuff(matrix,mouseX, mouseY, partialTicks);
        super.render(matrix, mouseX, mouseY, partialTicks);
    }

    protected boolean mouseHover(Vector2f topLeft, int mouseX, int mouseY, int width, int height) {
        return mouseX>=topLeft.x && mouseX<topLeft.x+width && mouseY>=topLeft.y && mouseY<topLeft.y+height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 0) {
            for (ButtonSuperType superButton : this.superButtons)
                superButton.onClick(mouseX, mouseY);
            return true;
        }
        return false;
    }

    public void saveAndDisplay(GuiSuperType next) {
        save();
        this.getMinecraft().setScreen(next);
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
            this.getMinecraft().setScreen(null);
        } else this.getMinecraft().setScreen(this.parent);
    }

    public void applyButton() {
        if(this.parent==null)
            getInstance().writeAndReload();
        else this.parent.applyButton();
    }

    @Override
    public void onClose() {
        EventsClient.renderDebug = true;
    }

    public void playGenericClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));

    }
}
