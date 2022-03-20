package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mods.thecomputerizer.musictriggers.config.configObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("NullableProblems")
public class GuiScrollingSong extends ObjectSelectionList<GuiScrollingSong.Entry> {

    private final List<String> codes;
    private final Screen IN;
    private final configObject holder;
    private GuiLinking linking = null;

    public GuiScrollingSong(Minecraft client, int width, int height, int top, int bottom, List<String> songs, List<String> codes, Screen IN, configObject holder, GuiLinking linking) {
        super(client, width, height, top, bottom, 32);
        this.codes = codes;
        this.IN = IN;
        this.holder = holder;
        if(linking!=null) this.linking = linking;
        for(int i=0;i<songs.size();i++) {
            if(codes!=null) this.addEntry(new Entry(songs.get(i), codes.get(i)));
            else this.addEntry(new Entry(songs.get(i), null));
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
    public void setSelected(@Nullable GuiScrollingSong.Entry entry) {
        super.setSelected(entry);
    }

    @Override
    protected void renderBackground(PoseStack matrix) {}

    @Override
    protected boolean isFocused() {
        return this.IN.getFocused() == this;
    }

    class Entry extends ObjectSelectionList.Entry<GuiScrollingSong.Entry> {

        private final String write;
        private final String code;

        public Entry(String s, String c) {
            this.write = s;
            this.code = c;
        }
        
        public String getCode() {
            return this.code;
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

        private void select() {
            GuiScrollingSong.this.setSelected(this);
            if(GuiScrollingSong.this.linking==null) {
                if (GuiScrollingSong.this.codes != null) {
                    GuiScrollingSong.this.minecraft.setScreen(new GuiSongInfo(GuiScrollingSong.this.IN, this.write, code, GuiScrollingSong.this.holder));
                } else {
                    GuiScrollingSong.this.minecraft.setScreen(new GuiSongInfo(GuiScrollingSong.this.IN, this.write, GuiScrollingSong.this.holder.addSong(this.write), GuiScrollingSong.this.holder));
                }
            }
            else {
                GuiScrollingSong.this.holder.addLinkingSong(GuiScrollingSong.this.linking.songCode, this.write);
                GuiScrollingSong.this.minecraft.setScreen(new GuiLinkingInfo(GuiScrollingSong.this.IN, this.write, GuiScrollingSong.this.linking.songCode, GuiScrollingSong.this.holder));
            }
        }

        public void render(PoseStack matrix, int i, int j, int k, int l, int m, int n, int o, boolean b, float f) {
            GuiScrollingSong.this.IN.getMinecraft().font.drawShadow(matrix, this.write, (float)(GuiScrollingSong.this.width / 2 - GuiScrollingSong.this.IN.getMinecraft().font.width(this.write) / 2), (float)(j + 1), 16777215, true);
        }
    }
}
