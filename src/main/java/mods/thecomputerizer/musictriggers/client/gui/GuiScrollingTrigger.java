package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mods.thecomputerizer.musictriggers.config.configObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class GuiScrollingTrigger extends ObjectSelectionList<GuiScrollingTrigger.Entry> {

    private final Screen IN;
    private final String songCode;
    private final configObject holder;

    public GuiScrollingTrigger(Minecraft client, int width, int height, int top, int bottom, List<String> triggers, Screen IN, configObject holder, String songCode) {
        super(client, width, height, top, bottom, 32);
        this.IN = IN;
        this.songCode = songCode;
        this.holder = holder;
        for (String trigger : triggers) {
            this.addEntry(new GuiScrollingTrigger.Entry(trigger));
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
    public void setSelected(@Nullable GuiScrollingTrigger.Entry entry) {
        super.setSelected(entry);
    }

    @Override
    protected void renderBackground(@NotNull PoseStack matrix) {}

    @Override
    protected boolean isFocused() {
        return this.IN.getFocused() == this;
    }

    class Entry extends ObjectSelectionList.Entry<GuiScrollingTrigger.Entry> {

        private final String info;

        public Entry(String info) {
            this.info = info;
        }

        
        public @NotNull Component getNarration() {
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

        private void select() {
            GuiScrollingTrigger.this.setSelected(this);
            GuiScrollingTrigger.this.minecraft.setScreen(new GuiTriggerInfo(GuiScrollingTrigger.this.IN, this.info, GuiScrollingTrigger.this.songCode, GuiScrollingTrigger.this.holder, true));
        }

        public void render(@NotNull PoseStack matrix, int i, int j, int k, int l, int m, int n, int o, boolean b, float f) {
            GuiScrollingTrigger.this.IN.getMinecraft().font.drawShadow(matrix, this.info, (float)(GuiScrollingTrigger.this.width / 2 - GuiScrollingTrigger.this.IN.getMinecraft().font.width(this.info) / 2), (float)(j + 1), 16777215, true);
        }
    }
}
