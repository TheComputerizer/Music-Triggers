package mods.thecomputerizer.musictriggers.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GuiScrollingLoops extends AlwaysSelectedEntryListWidget<GuiScrollingLoops.Entry> {

    public int size;
    public List<String> info;
    private final GuiLoops IN;
    public int index;
    public String code;
    public String song;
    public boolean linked;

    public GuiScrollingLoops(MinecraftClient client, int width, int height, int top, int bottom, List<String> info, GuiLoops IN, String code, String song, boolean linked) {
        super(client, width, height, top, bottom, 32);
        this.size = info.size();
        this.info = info;
        this.IN = IN;
        this.code = code;
        this.song = song;
        this.linked = linked;
        for(int i=0;i<info.size();i++) {
            this.addEntry(new GuiScrollingLoops.Entry(info.get(i), i));
        }
    }

    public void resetEntries(List<String> info) {
        List<GuiScrollingLoops.Entry> newEntries = new ArrayList<>();
        for(int i=0;i<info.size();i++) {
            newEntries.add(new GuiScrollingLoops.Entry(info.get(i), i));
        }
        this.replaceEntries(newEntries);
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
    public void setSelected(@Nullable GuiScrollingLoops.Entry entry) {
        super.setSelected(entry);
    }

    @Override
    protected void renderBackground(@NotNull MatrixStack matrix) {}

    @Override
    protected boolean isFocused() {
        return this.IN.getFocused() == this;
    }

    class Entry extends AlwaysSelectedEntryListWidget.Entry<GuiScrollingLoops.Entry> {

        private final String info;
        private final int index;

        public Entry(String info, int index) {
            this.info = info;
            this.index = index;
        }

        public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
            if (p_231044_5_ == 0) {
                this.select();
                return true;
            } else {
                return false;
            }
        }

        private int getIndex(){
            return this.index;
        }

        public @NotNull Text getNarration() {
            return new TranslatableText("");
        }

        private void select() {
            GuiScrollingLoops.this.index = this.getIndex();
            GuiScrollingLoops.this.setSelected(this);
            GuiScrollingLoops.this.client.setScreen(new GuiLoopInfo(GuiScrollingLoops.this.IN, GuiScrollingLoops.this.IN.holder, this.getIndex(), GuiScrollingLoops.this.IN.code, null, false));
        }

        public void render(@NotNull MatrixStack matrix, int i, int j, int k, int l, int m, int n, int o, boolean b, float f) {
            MinecraftClient.getInstance().textRenderer.drawWithShadow(matrix, this.info, (float)(GuiScrollingLoops.this.width / 2 - MinecraftClient.getInstance().textRenderer.getWidth(this.info) / 2), (float)(j + 1), 16777215, true);
        }
    }
}
