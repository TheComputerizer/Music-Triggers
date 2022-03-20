package mods.thecomputerizer.musictriggers.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GuiScrollingInfo extends AlwaysSelectedEntryListWidget<GuiScrollingInfo.Entry> {

    private final GuiSongInfo IN;
    public int index;

    public GuiScrollingInfo(MinecraftClient client, int width, int height, int top, int bottom, List<String> info, GuiSongInfo IN) {
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
    protected int getScrollbarPositionX() {
        return super.getScrollbarPositionX() + 20;
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

    class Entry extends AlwaysSelectedEntryListWidget.Entry<GuiScrollingInfo.Entry> {

        private final String info;
        private final int index;

        public Entry(String info, int i) {
            this.info = info;
            this.index = i;
        }

        
        public Text getNarration() {
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

        private int getIndex(String s){
            return GuiScrollingInfo.this.IN.parameters.indexOf(s);
        }

        private void select() {
            GuiScrollingInfo.this.index = this.getIndex(this.info);
            GuiScrollingInfo.this.setSelected(this);
            if(this.index>=5) GuiScrollingInfo.this.client.setScreen(new GuiTriggerInfo(GuiScrollingInfo.this.IN, this.info, GuiScrollingInfo.this.IN.songCode, GuiScrollingInfo.this.IN.holder,false));
        }

        public void render(MatrixStack matrix, int i, int j, int k, int l, int m, int n, int o, boolean b, float f) {
            MinecraftClient.getInstance().textRenderer.drawWithShadow(matrix, this.info, (float)(GuiScrollingInfo.this.width / 2 - MinecraftClient.getInstance().textRenderer.getWidth(this.info) / 2), (float)(j + 1), 16777215, true);
        }
    }
}
