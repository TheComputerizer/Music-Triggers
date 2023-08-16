package mods.thecomputerizer.musictriggers.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ButtonSuperType extends ExtendedButton {
    private final List<Component> hoverLines;
    private final TriConsumer<GuiSuperType, ButtonSuperType, Integer> onClick;
    private final int numModes;
    private int mode;
    public ButtonSuperType(int xPos, int yPos, int width, int height, int numModes, String displayString,
                           List<String> hoverText, TriConsumer<GuiSuperType, ButtonSuperType, Integer> onClick,
                           boolean isEnabled) {
        super(xPos, yPos, width, height, Component.literal(displayString), null);
        this.hoverLines = hoverText.stream().map(Component::literal).collect(Collectors.toList());
        this.numModes = numModes;
        this.mode = 1;
        this.onClick = onClick;
        this.active = isEnabled;
        this.visible = isEnabled;
    }

    public void setEnable(boolean is) {
        this.active = is;
        this.visible = is;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void updateDisplay(String newDisplay, Font font, GuiSuperType screenToUpdate) {
        this.message = Component.literal(newDisplay);
        this.width = font.width(this.message) + 8;
        screenToUpdate.buttonWidthUpdate();
    }


    protected boolean isHovering(int mouseX, int mouseY) {
        return this.active && this.visible && mouseX>=this.x && mouseX<this.x+this.width
                && mouseY>=this.y && mouseY<this.y+this.height;
    }

    public List<Component> getHoverText(int mouseX, int mouseY) {
        return isHovering(mouseX, mouseY) ? this.hoverLines : new ArrayList<>();
    }
    @Override
    public void onPress() {

    }

    public boolean handle(GuiSuperType parent, double mouseX, double mouseY) {
        if(isHovering((int)mouseX, (int)mouseY)) {
            incrementMode();
            this.onClick.accept(parent,this,this.mode);
            parent.playGenericClickSound();
            return true;
        }
        return false;
    }

    public void incrementMode() {
        this.mode++;
        if(this.mode>this.numModes) this.mode = 1;
    }

    @FunctionalInterface
    public interface CreatorFunction<X, Y, W, H, M, D, L, F, A, B> {
        B apply(X x, Y y, W width, H height, M Modes, D display, L lines, F function, A enabled);
    }
}
