package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.config.ConfigObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class GuiScrollingTriggerInfo extends AlwaysSelectedEntryListWidget<GuiScrollingTriggerInfo.Entry> {

    private final GuiTriggerInfo IN;
    public int index;
    public ConfigObject holder;

    public GuiScrollingTriggerInfo(MinecraftClient client, int width, int height, int top, int bottom, List<String> parameters, GuiTriggerInfo IN, ConfigObject holder) {
        super(client, width, height, top, bottom, 32);
        this.IN = IN;
        this.holder = holder;
        for (String parameter : parameters) {
            this.addEntry(new Entry(parameter));
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
    public void setSelected(@Nullable GuiScrollingTriggerInfo.Entry entry) {
        super.setSelected(entry);
    }

    @Override
    protected void renderBackground(@NotNull MatrixStack matrix) {}

    @Override
    protected void renderHeader(@NotNull MatrixStack matrices, int x, int y, Tessellator tessellator) {}

    @Override
    protected boolean isFocused() {
        return this.IN.getFocused() == this;
    }

    class Entry extends AlwaysSelectedEntryListWidget.Entry<GuiScrollingTriggerInfo.Entry> {

        private final String info;

        public Entry(String info) {
            this.info = info;
        }

        public int getIndex(String s){
            return Mappings.reverseparameters.get(s);
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
            GuiScrollingTriggerInfo.this.index = this.getIndex(this.info);
            GuiScrollingTriggerInfo.this.setSelected(this);
            if(this.info.matches("zone")) {
                EventsClient.parentScreen = GuiScrollingTriggerInfo.this.IN;
                EventsClient.zone = true;
                GuiScrollingTriggerInfo.this.IN.close();
            }
        }

        public void render(@NotNull MatrixStack matrix, int i, int j, int k, int l, int m, int n, int o, boolean b, float f) {
            MinecraftClient.getInstance().textRenderer.drawWithShadow(matrix, this.info, (float)(GuiScrollingTriggerInfo.this.width / 2 - MinecraftClient.getInstance().textRenderer.getWidth(this.info) / 2), (float)(j + 1), 16777215, true);
        }
    }
}
