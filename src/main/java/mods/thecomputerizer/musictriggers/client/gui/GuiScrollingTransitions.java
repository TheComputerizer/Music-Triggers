package mods.thecomputerizer.musictriggers.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class GuiScrollingTransitions extends AlwaysSelectedEntryListWidget<GuiScrollingTransitions.Entry> {

    public int size;
    public List<String> info;
    private final GuiTransitions IN;
    public int index;

    public GuiScrollingTransitions(MinecraftClient client, int width, int height, int top, int bottom, List<String> info, GuiTransitions IN) {
        super(client, width, height, top, bottom, 32);
        this.IN = IN;
        for(int i=0;i<info.size();i++) {
            this.addEntry(new GuiScrollingTransitions.Entry(info.get(i), i));
        }
    }

    @Override
    protected int getScrollbarPositionX() {
        return super.getScrollbarPositionX() + 20;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 50;
    }

    @Override
    public void setSelected(@Nullable GuiScrollingTransitions.Entry entry) {
        super.setSelected(entry);
    }

    @Override
    protected void renderBackground(@NotNull MatrixStack matrix) {}

    @Override
    protected boolean isFocused() {
        return this.IN.getFocused() == this;
    }

    class Entry extends AlwaysSelectedEntryListWidget.Entry<GuiScrollingTransitions.Entry> {

        private final String info;
        private final int index;

        public Entry(String info, int i) {
            this.info = info;
            this.index = i;
        }

        
        public @NotNull Text getNarration() {
            return new TranslatableText("");
         }

        public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
            if (p_231044_5_ == 0) {
                this.select();
                return true;
            } else {
                return false;
            }
        }

        private void select() {
            GuiScrollingTransitions.this.index = this.index;
            GuiScrollingTransitions.this.setSelected(this);
            GuiScrollingTransitions.this.client.setScreen(new GuiTransitionInfo(GuiScrollingTransitions.this.IN, GuiScrollingTransitions.this.IN.holder, this.index, false, GuiScrollingTransitions.this.IN.holder.isTitle(this.index), false, ""));
        }

        public void render(@NotNull MatrixStack matrix, int i, int j, int k, int l, int m, int n, int o, boolean b, float f) {
            MinecraftClient.getInstance().textRenderer.drawWithShadow(matrix, this.info, (float)(GuiScrollingTransitions.this.width / 2 - MinecraftClient.getInstance().textRenderer.getWidth(this.info) / 2), (float)(j + 1), 16777215, true);
        }
    }
}
