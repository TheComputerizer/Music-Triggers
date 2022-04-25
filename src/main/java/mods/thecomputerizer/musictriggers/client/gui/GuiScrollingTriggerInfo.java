package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.config.ConfigObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("NullableProblems")
public class GuiScrollingTriggerInfo extends ExtendedList<GuiScrollingTriggerInfo.Entry> {

    private final GuiTriggerInfo IN;
    public int index;
    public ConfigObject holder;

    public GuiScrollingTriggerInfo(Minecraft client, int width, int height, int top, int bottom, List<String> parameters, GuiTriggerInfo IN, ConfigObject holder) {
        super(client, width, height, top, bottom, 32);
        this.IN = IN;
        this.holder = holder;
        for (String parameter : parameters) {
            this.addEntry(new Entry(parameter));
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
    public void setSelected(@Nullable GuiScrollingTriggerInfo.Entry entry) {
        super.setSelected(entry);
    }

    @Override
    protected void renderBackground(MatrixStack matrix) {}

    @Override
    protected boolean isFocused() {
        return this.IN.getFocused() == this;
    }

    class Entry extends ExtendedList.AbstractListEntry<GuiScrollingTriggerInfo.Entry> {

        private final String info;

        public Entry(String info) {
            this.info = info;
        }

        public int getIndex(String s){
            return Mappings.reverseparameters.get(s);
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
                GuiScrollingTriggerInfo.this.IN.onClose();
            }
        }

        public void render(MatrixStack matrix, int i, int j, int k, int l, int m, int n, int o, boolean b, float f) {
            GuiScrollingTriggerInfo.this.IN.getMinecraft().font.drawShadow(matrix, this.info, (float)(GuiScrollingTriggerInfo.this.width / 2 - GuiScrollingTriggerInfo.this.IN.getMinecraft().font.width(this.info) / 2), (float)(j + 1), 16777215, true);
        }
    }
}
