package mods.thecomputerizer.musictriggers.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiButtonExt;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class ButtonSuperType extends GuiButtonExt {
    private final List<String> hoverLines;
    private final BiConsumer<GuiSuperType, ButtonSuperType> handlerFunction;
    private final GuiSuperType parentScreen;
    private final boolean isApply;
    private String baseDisplayString;
    public ButtonSuperType(int id, int xPos, int yPos, int width, int height, String displayString,
                           List<String> hoverText, BiConsumer<GuiSuperType, ButtonSuperType> handler,
                           GuiSuperType parent, boolean isApply) {
        super(id, xPos, yPos, width, height, displayString);
        this.hoverLines = hoverText;
        this.handlerFunction = handler;
        this.parentScreen = parent;
        this.isApply = isApply;
        if(isApply) {
            this.visible = parent.getInstance().hasEdits();
            this.enabled = parent.getInstance().hasEdits();
        }
        this.baseDisplayString = displayString;
    }

    public boolean isApplyButton() {
        return this.isApply;
    }

    public void setEnable(boolean is) {
        this.visible = is;
        this.enabled = is;
    }

    public void updateBaseTextAndFormatting(FontRenderer font, String newDisplay, TextFormatting ... formatSettings) {
        //int prevWidth = this.width;
        //int newWidth = font.getStringWidth(newDisplay);
        this.width = font.getStringWidth(newDisplay)+4;
        this.baseDisplayString = newDisplay;
        updateDisplayFormat(formatSettings);
    }

    public void updateDisplayFormat(TextFormatting ... formatSettings) {
        if(Objects.isNull(formatSettings)) {
            this.displayString = this.baseDisplayString;
            return;
        }
        StringBuilder builder = new StringBuilder();
        for(TextFormatting format : formatSettings)
            builder.append(format.toString());
        builder.append(this.baseDisplayString);
        this.displayString = builder.toString();
    }


    protected boolean isHovering(int mouseX, int mouseY) {
        return this.enabled && this.visible && mouseX>=this.x && mouseX<this.x+this.width
                && mouseY>=this.y && mouseY<this.y+this.height;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partial) {
        super.drawButton(mc, mouseX, mouseY, partial);
        if(isHovering(mouseX, mouseY)) this.parentScreen.drawHoveringText(this.hoverLines,mouseX,mouseY);
    }

    public void handle() {
        this.handlerFunction.accept(this.parentScreen,this);
    }

    @FunctionalInterface
    public interface CreatorFunction<I, X, Y, W, H, D, L, F, P, A, B> {
        B apply(I id, X x, Y y, W width, H height, D display, L lines, F function, P parent, A apply);
    }
}
