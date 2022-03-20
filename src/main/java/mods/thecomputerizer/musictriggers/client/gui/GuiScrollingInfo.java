package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("NullableProblems")
public class GuiScrollingInfo extends ObjectSelectionList<GuiScrollingInfo.Entry> {

    private final GuiSongInfo IN;
    public int index;

    public GuiScrollingInfo(Minecraft client, int width, int height, int top, int bottom, List<String> info, GuiSongInfo IN) {
        super(client, width, height, top, bottom, 32);
        this.IN = IN;
        for(int i=0;i<info.size();i++) {
            this.addEntry(new GuiScrollingInfo.Entry(info.get(i), i));
        }
    }

    public void resetEntries(List<String> info) {
        for(GuiScrollingInfo.Entry entry : this.children()) {
            this.removeEntry(entry);
        }
        for(int i=0;i<info.size();i++) {
            this.addEntry(new GuiScrollingInfo.Entry(info.get(i), i));
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
    public void setSelected(@Nullable GuiScrollingInfo.Entry entry) {
        super.setSelected(entry);
    }

    @Override
    protected void renderBackground(PoseStack matrix) {}

    @Override
    protected boolean isFocused() {
        return this.IN.getFocused() == this;
    }

    class Entry extends ObjectSelectionList.Entry<GuiScrollingInfo.Entry> {

        private final String info;
        private final int index;

        public Entry(String info, int i) {
            this.info = info;
            this.index = i;
        }

        
        public Component getNarration() {
            return new TranslatableComponent("");
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

        public void render(PoseStack matrix, int i, int j, int k, int l, int m, int n, int o, boolean b, float f) {
            GuiScrollingInfo.this.IN.getMinecraft().font.drawShadow(matrix, this.info, (float)(GuiScrollingInfo.this.width / 2 - GuiScrollingInfo.this.IN.getMinecraft().font.width(this.info) / 2), (float)(j + 1), 16777215, true);
        }
    }
}
