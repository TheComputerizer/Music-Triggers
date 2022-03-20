package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GuiScrollingTransitionsInfo extends ExtendedList<GuiScrollingTransitionsInfo.Entry> {

    public List<String> info;
    private final GuiTransitionInfo IN;
    public int index;

    public GuiScrollingTransitionsInfo(Minecraft client, int width, int height, int top, int bottom, List<String> info, GuiTransitionInfo IN) {
        super(client, width, height, top, bottom, 32);
        this.info = info;
        this.IN = IN;
        for(int i=0;i<info.size();i++) {
            this.addEntry(new GuiScrollingTransitionsInfo.Entry(info.get(i), i));
        }
    }

    public void resetEntries() {
        List<GuiScrollingTransitionsInfo.Entry> newEntries = new ArrayList<>();
        for(int i=0;i<this.info.size();i++) {
            newEntries.add(new GuiScrollingTransitionsInfo.Entry(this.info.get(i), i));
        }
        this.replaceEntries(newEntries);
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
    public void setSelected(@Nullable GuiScrollingTransitionsInfo.Entry entry) {
        super.setSelected(entry);
    }

    @Override
    protected void renderBackground(MatrixStack matrix) {}

    @Override
    protected boolean isFocused() {
        return this.IN.getFocused() == this;
    }

    class Entry extends ExtendedList.AbstractListEntry<GuiScrollingTransitionsInfo.Entry> {

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
            GuiScrollingTransitionsInfo.this.index = index;
            GuiScrollingTransitionsInfo.this.setSelected(this);
        }

        public void render(MatrixStack matrix, int i, int j, int k, int l, int m, int n, int o, boolean b, float f) {
            GuiScrollingTransitionsInfo.this.IN.getMinecraft().font.drawShadow(matrix, this.info, (float)(GuiScrollingTransitionsInfo.this.width / 2 - GuiScrollingTransitionsInfo.this.IN.getMinecraft().font.width(this.info) / 2), (float)(j + 1), 16777215, true);
        }
    }
}
