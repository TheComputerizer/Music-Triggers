package mods.thecomputerizer.musictriggers.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class GuiScrollingOther extends AlwaysSelectedEntryListWidget<GuiScrollingOther.Entry> {

    public List<String> info;
    private final Screen IN;
    public int index;

    public GuiScrollingOther(MinecraftClient client, int width, int height, int top, int bottom, List<String> info, Screen IN) {
        super(client, width, height, top, bottom, 32);
        this.info = info;
        this.IN = IN;
        for(int i=0;i<info.size();i++) {
            this.addEntry(new GuiScrollingOther.Entry(info.get(i), i));
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
    public void setSelected(@Nullable GuiScrollingOther.Entry entry) {
        super.setSelected(entry);
    }

    @Override
    protected void renderBackground(@NotNull MatrixStack matrix) {}

    @Override
    protected boolean isFocused() {
        return this.IN.getFocused() == this;
    }

    class Entry extends AlwaysSelectedEntryListWidget.Entry<GuiScrollingOther.Entry> {

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
            GuiScrollingOther.this.index = this.index;
            GuiScrollingOther.this.setSelected(this);
        }

        public void render(@NotNull MatrixStack matrix, int i, int j, int k, int l, int m, int n, int o, boolean b, float f) {
            MinecraftClient.getInstance().textRenderer.drawWithShadow(matrix, this.info, (float)(GuiScrollingOther.this.width / 2 - MinecraftClient.getInstance().textRenderer.getWidth(this.info) / 2), (float)(j + 1), 16777215, true);
        }
    }
}
