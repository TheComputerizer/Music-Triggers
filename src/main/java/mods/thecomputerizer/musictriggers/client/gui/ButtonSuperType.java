package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ButtonSuperType extends ExtendedButton {
    private final List<ITextComponent> hoverLines;
    private final BiConsumer<GuiSuperType, ButtonSuperType> handlerFunction;
    private final GuiSuperType parentScreen;
    private final boolean isApply;
    private String baseDisplayString;
    public ButtonSuperType(int xPos, int yPos, int width, int height, String displayString,
                           List<String> hoverText, BiConsumer<GuiSuperType, ButtonSuperType> handler,
                           GuiSuperType parent, boolean isApply) {
        super(xPos, yPos, width, height, new StringTextComponent(displayString), null);
        this.hoverLines = hoverText.stream().map(StringTextComponent::new).collect(Collectors.toList());
        this.handlerFunction = handler;
        this.parentScreen = parent;
        this.isApply = isApply;
        if(isApply) {
            this.active = parent.getInstance().hasEdits();
            this.visible = parent.getInstance().hasEdits();
        }
        this.baseDisplayString = displayString;
    }

    public boolean isApplyButton() {
        return this.isApply;
    }

    public void setEnable(boolean is) {
        this.active = is;
        this.visible = is;
    }

    public void updateBaseTextAndFormatting(FontRenderer font, String newDisplay, TextFormatting ... formatSettings) {
        //int prevWidth = this.width;
        //int newWidth = font.getStringWidth(newDisplay);
        this.width = font.width(newDisplay)+4;
        this.baseDisplayString = newDisplay;
        updateDisplayFormat(formatSettings);
    }

    public void updateDisplayFormat(TextFormatting ... formatSettings) {
        if(Objects.isNull(formatSettings)) {
            this.message = new StringTextComponent(this.baseDisplayString);
            return;
        }
        StringBuilder builder = new StringBuilder();
        for(TextFormatting format : formatSettings)
            builder.append(format.toString());
        builder.append(this.baseDisplayString);
        this.message = new StringTextComponent(builder.toString());
    }


    protected boolean isHovering(int mouseX, int mouseY) {
        return this.active && this.visible && mouseX>=this.x && mouseX<this.x+this.width
                && mouseY>=this.y && mouseY<this.y+this.height;
    }

    @Override
    public void renderButton(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partial) {
        super.renderButton(matrix, mouseX, mouseY, partial);
        if(isHovering(mouseX, mouseY)) this.parentScreen.renderComponentTooltip(matrix,this.hoverLines,mouseX,mouseY);
    }

    @Override
    public void onPress() {

    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if(isHovering((int)mouseX, (int)mouseY)) this.handlerFunction.accept(this.parentScreen,this);
    }

    @FunctionalInterface
    public interface CreatorFunction<X, Y, W, H, D, L, F, P, A, B> {
        B apply(X x, Y y, W width, H height, D display, L lines, F function, P parent, A apply);
    }
}
