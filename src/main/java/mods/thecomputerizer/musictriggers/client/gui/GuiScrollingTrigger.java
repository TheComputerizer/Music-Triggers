package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.config.ConfigObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class GuiScrollingTrigger extends AlwaysSelectedEntryListWidget<GuiScrollingTrigger.Entry> {

    private final Screen IN;
    private final String songCode;
    private final ConfigObject holder;

    public GuiScrollingTrigger(MinecraftClient client, int width, int height, int top, int bottom, List<String> triggers, Screen IN, ConfigObject holder, String songCode) {
        super(client, width, height, top, bottom, 32);
        this.IN = IN;
        this.songCode = songCode;
        this.holder = holder;
        for (String trigger : triggers) {
            this.addEntry(new GuiScrollingTrigger.Entry(trigger));
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
    public void setSelected(@Nullable GuiScrollingTrigger.Entry entry) {
        super.setSelected(entry);
    }

    @Override
    protected void renderBackground(@NotNull MatrixStack matrix) {}

    @Override
    protected boolean isFocused() {
        return this.IN.getFocused() == this;
    }

    class Entry extends AlwaysSelectedEntryListWidget.Entry<GuiScrollingTrigger.Entry> {

        private final String info;

        public Entry(String info) {
            this.info = info;
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
            GuiScrollingTrigger.this.setSelected(this);
            GuiScrollingTrigger.this.client.setScreen(new GuiTriggerInfo(GuiScrollingTrigger.this.IN, this.info, GuiScrollingTrigger.this.songCode, GuiScrollingTrigger.this.holder, true));
        }

        public void render(@NotNull MatrixStack matrix, int i, int j, int k, int l, int m, int n, int o, boolean b, float f) {
            MinecraftClient.getInstance().textRenderer.drawWithShadow(matrix, this.info, (float)(GuiScrollingTrigger.this.width / 2 - MinecraftClient.getInstance().textRenderer.getWidth(this.info) / 2), (float)(j + 1), 16777215, true);
        }
    }
}
