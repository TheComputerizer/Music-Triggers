package mods.thecomputerizer.musictriggers.client.gui;

import net.minecraftforge.fml.client.config.GuiButtonExt;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.ArrayList;
import java.util.List;

public class ButtonSuperType extends GuiButtonExt {
    private final List<String> hoverLines;
    private final TriConsumer<GuiSuperType, ButtonSuperType, Integer> onClick;
    private final int numModes;
    private int mode;
    public ButtonSuperType(int id, int xPos, int yPos, int width, int height, int numModes, String displayString,
                           List<String> hoverText, TriConsumer<GuiSuperType, ButtonSuperType, Integer> onClick,
                           boolean isEnabled) {
        super(id, xPos, yPos, width, height, displayString);
        this.hoverLines = hoverText;
        this.numModes = numModes;
        this.mode = 1;
        this.onClick = onClick;
        this.enabled = isEnabled;
        this.visible = isEnabled;
    }

    public void setEnable(boolean is) {
        this.enabled = is;
        this.visible = is;
    }

    public void updateDisplay(String newDisplay) {
        this.displayString = newDisplay;
    }


    protected boolean isHovering(int mouseX, int mouseY) {
        return this.enabled && this.visible && mouseX>=this.x && mouseX<this.x+this.width
                && mouseY>=this.y && mouseY<this.y+this.height;
    }

    public List<String> getHoverText(int mouseX, int mouseY) {
        return isHovering(mouseX, mouseY) ? this.hoverLines : new ArrayList<>();
    }

    public void handle(GuiSuperType parent) {
        incrementMode();
        this.onClick.accept(parent,this,this.mode);
    }

    public void incrementMode() {
        this.mode++;
        if(this.mode>this.numModes) this.mode = 1;
    }

    @FunctionalInterface
    public interface CreatorFunction<I, X, Y, W, H, M, D, L, F, A, B> {
        B apply(I id, X x, Y y, W width, H height, M Modes, D display, L lines, F function, A enabled);
    }
}
