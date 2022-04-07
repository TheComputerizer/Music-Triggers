package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.thecomputerizer.musictriggers.config.configObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("NullableProblems")
public class GuiScrollingInfo extends ExtendedList<GuiScrollingInfo.Entry> {

    private final GuiSongInfo IN;
    public int index;
    public configObject holder;

    public GuiScrollingInfo(Minecraft client, int width, int height, int top, int bottom, List<String> info, GuiSongInfo IN, configObject holder) {
        super(client, width, height, top, bottom, 32);
        this.IN = IN;
        this.holder = holder;
        for(int i=0;i<info.size();i++) {
            this.addEntry(new GuiScrollingInfo.Entry(info.get(i), i));
        }
    }

    public void resetEntries(List<String> info) {
        List<GuiScrollingInfo.Entry> newEntries = new ArrayList<>();
        for(int i=0;i<info.size();i++) {
            newEntries.add(new GuiScrollingInfo.Entry(info.get(i), i));
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
    public void setSelected(@Nullable GuiScrollingInfo.Entry entry) {
        super.setSelected(entry);
    }

    @Override
    protected void renderBackground(MatrixStack matrix) {}

    @Override
    protected boolean isFocused() {
        return this.IN.getFocused() == this;
    }

    class Entry extends ExtendedList.AbstractListEntry<GuiScrollingInfo.Entry> {

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

        private int getIndex(String s){
            return GuiScrollingInfo.this.IN.parameters.indexOf(s);
        }

        private void select() {
            GuiScrollingInfo.this.index = this.getIndex(this.info);
            GuiScrollingInfo.this.setSelected(this);
            if(this.index>=5) GuiScrollingInfo.this.minecraft.setScreen(new GuiTriggerInfo(GuiScrollingInfo.this.IN, this.info, GuiScrollingInfo.this.IN.songCode, GuiScrollingInfo.this.IN.holder,false));
        }

        public void render(MatrixStack matrix, int i, int j, int k, int l, int m, int n, int o, boolean b, float f) {
            String render;
            if(this.index>=5) render = GuiScrollingInfo.this.holder.translateCodedTrigger(GuiScrollingInfo.this.IN.songCode,this.info);
            else render = this.info;
            GuiScrollingInfo.this.IN.getMinecraft().font.drawShadow(matrix, render, (float)(GuiScrollingInfo.this.width / 2 - GuiScrollingInfo.this.IN.getMinecraft().font.width(render) / 2), (float)(j + 1), 16777215, true);
        }
    }
}
