package mods.thecomputerizer.musictriggers.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class GuiScrollingChooseImage extends AlwaysSelectedEntryListWidget<GuiScrollingChooseImage.Entry> {

    public Map<String, Boolean> imageMap;
    private final GuiChooseImage IN;
    public int index;

    public GuiScrollingChooseImage(MinecraftClient client, int width, int height, int top, int bottom, List<String> info, GuiChooseImage IN, Map<String, Boolean> imageMap) {
        super(client, width, height, top, bottom, 32);
        this.imageMap = imageMap;
        this.IN = IN;
        for(int i=0;i<info.size();i++) {
            this.addEntry(new GuiScrollingChooseImage.Entry(info.get(i), i));
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
    public void setSelected(@Nullable GuiScrollingChooseImage.Entry entry) {
        super.setSelected(entry);
    }

    @Override
    protected void renderBackground(@NotNull MatrixStack matrix) {}

    @Override
    protected boolean isFocused() {
        return this.IN.getFocused() == this;
    }

    class Entry extends AlwaysSelectedEntryListWidget.Entry<GuiScrollingChooseImage.Entry> {

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
            GuiScrollingChooseImage.this.index = this.index;
            GuiScrollingChooseImage.this.setSelected(this);
            GuiScrollingChooseImage.this.client.setScreen(new GuiTransitionInfo(GuiScrollingChooseImage.this.IN, GuiScrollingChooseImage.this.IN.holder, 0, true, false, GuiScrollingChooseImage.this.imageMap.get(this.info), this.info.split("\\.")[0]));
        }

        public void render(@NotNull MatrixStack matrix, int i, int j, int k, int l, int m, int n, int o, boolean b, float f) {
            MinecraftClient.getInstance().textRenderer.drawWithShadow(matrix, this.info, (float)(GuiScrollingChooseImage.this.width / 2 - MinecraftClient.getInstance().textRenderer.getWidth(this.info) / 2), (float)(j + 1), 16777215, true);
        }
    }
}
