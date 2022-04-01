package mods.thecomputerizer.musictriggers.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GuiScrollingLoopsInfo extends AlwaysSelectedEntryListWidget<GuiScrollingLoopsInfo.Entry> {

    public int size;
    public List<String> info;
    private final GuiLoopInfo IN;
    public int index;

    public GuiScrollingLoopsInfo(MinecraftClient client, int width, int height, int top, int bottom, List<String> info, GuiLoopInfo IN) {
        super(client, width, height, top, bottom, 32);
        this.size = info.size();
        this.info = info;
        this.IN = IN;
        for (String s : info) {
            this.addEntry(new Entry(s));
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
    public void setSelected(@Nullable GuiScrollingLoopsInfo.Entry entry) {
        super.setSelected(entry);
    }

    @Override
    protected void renderBackground(@NotNull MatrixStack matrix) {}

    @Override
    protected boolean isFocused() {
        return this.IN.getFocused() == this;
    }

    class Entry extends AlwaysSelectedEntryListWidget.Entry<GuiScrollingLoopsInfo.Entry> {

        private final String info;

        public Entry(String info) {
            this.info = info;
        }

        public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
            if (p_231044_5_ == 0) {
                this.select();
                return true;
            } else {
                return false;
            }
        }

        private int getIndex(String s){
            return GuiScrollingLoopsInfo.this.IN.info.indexOf(s);
        }

        public @NotNull Text getNarration() {
            return new TranslatableText("");
        }

        private void select() {
            GuiScrollingLoopsInfo.this.index = this.getIndex(this.info);
            GuiScrollingLoopsInfo.this.setSelected(this);
        }

        public void render(@NotNull MatrixStack matrix, int i, int j, int k, int l, int m, int n, int o, boolean b, float f) {
            MinecraftClient.getInstance().textRenderer.drawWithShadow(matrix, this.info, (float)(GuiScrollingLoopsInfo.this.width / 2 - MinecraftClient.getInstance().textRenderer.getWidth(this.info) / 2), (float)(j + 1), 16777215, true);
        }
    }
}
