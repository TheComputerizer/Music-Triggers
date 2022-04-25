package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.config.ConfigObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class GuiScrollingLinking extends ObjectSelectionList<GuiScrollingLinking.Entry> {

    private final GuiLinking IN;
    public int index;
    public ConfigObject holder;

    public GuiScrollingLinking(Minecraft client, int width, int height, int top, int bottom, List<String> songs, GuiLinking IN) {
        super(client, width, height, top, bottom, 32);
        this.IN = IN;
        for (String song : songs) {
            this.addEntry(new Entry(song));
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
    public void setSelected(@Nullable GuiScrollingLinking.Entry entry) {
        super.setSelected(entry);
    }

    @Override
    protected void renderBackground(@NotNull PoseStack matrix) {}

    @Override
    protected boolean isFocused() {
        return this.IN.getFocused() == this;
    }

    class Entry extends ObjectSelectionList.Entry<GuiScrollingLinking.Entry> {

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

        private int getIndex(String s){
            return GuiScrollingLinking.this.IN.songs.indexOf(s);
        }

        private void select() {
            GuiScrollingLinking.this.index = this.getIndex(this.info);
            GuiScrollingLinking.this.setSelected(this);
            GuiScrollingLinking.this.minecraft.setScreen(new GuiLinkingInfo(GuiScrollingLinking.this.IN, this.info, GuiScrollingLinking.this.IN.songCode, GuiScrollingLinking.this.IN.holder));
        }

        public void render(@NotNull PoseStack matrix, int i, int j, int k, int l, int m, int n, int o, boolean b, float f) {
            GuiScrollingLinking.this.IN.getMinecraft().font.drawShadow(matrix, this.info, (float)(GuiScrollingLinking.this.width / 2 - GuiScrollingLinking.this.IN.getMinecraft().font.width(this.info) / 2), (float)(j + 1), 16777215, true);
        }
    }
}
