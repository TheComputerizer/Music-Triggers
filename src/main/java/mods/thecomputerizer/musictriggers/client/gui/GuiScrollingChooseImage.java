package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class GuiScrollingChooseImage extends ExtendedList<GuiScrollingChooseImage.Entry> {

    public Map<String, Boolean> imageMap;
    private final GuiChooseImage IN;
    public int index;

    public GuiScrollingChooseImage(Minecraft client, int width, int height, int top, int bottom, List<String> info, GuiChooseImage IN, Map<String, Boolean> imageMap) {
        super(client, width, height, top, bottom, 32);
        this.imageMap = imageMap;
        this.IN = IN;
        for(int i=0;i<info.size();i++) {
            this.addEntry(new GuiScrollingChooseImage.Entry(info.get(i), i));
        }
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 20;
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
    protected void renderBackground(MatrixStack matrix) {}

    @Override
    protected boolean isFocused() {
        return this.IN.getFocused() == this;
    }

    class Entry extends ExtendedList.AbstractListEntry<GuiScrollingChooseImage.Entry> {

        private final String info;
        private final int index;

        public Entry(String info, int i) {
            this.info = info;
            this.index = i;
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
            GuiScrollingChooseImage.this.minecraft.setScreen(new GuiTransitionInfo(GuiScrollingChooseImage.this.IN, GuiScrollingChooseImage.this.IN.holder, 0, true, false, GuiScrollingChooseImage.this.imageMap.get(this.info), this.info.split("\\.")[0]));
        }

        public void render(MatrixStack matrix, int i, int j, int k, int l, int m, int n, int o, boolean b, float f) {
            GuiScrollingChooseImage.this.IN.getMinecraft().font.drawShadow(matrix, this.info, (float)(GuiScrollingChooseImage.this.width / 2 - GuiScrollingChooseImage.this.IN.getMinecraft().font.width(this.info) / 2), (float)(j + 1), 16777215, true);
        }
    }
}
