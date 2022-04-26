package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.config.ConfigObject;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GuiLinkingInfo extends Screen {

    public String song;
    public String songCode;
    public List<String> info;
    public Screen parentScreen;
    public GuiScrollingLinkingInfo scrollingSongs;
    public ConfigObject holder;
    private final Identifier background;

    public GuiLinkingInfo(Screen parentScreen, String song, String songCode, ConfigObject holder) {
        super(new TranslatableText("screen.musictriggers.linking_info"));
        this.parentScreen = parentScreen;
        this.song = song;
        this.songCode = songCode;
        this.holder = holder;
        this.info = this.holder.getAllLinkingInfo(this.songCode, this.song);
        this.background = new Identifier(MusicTriggersCommon.MODID,"textures/block/recorder_side_active.png");
    }

    @Override
    public void render(@NotNull MatrixStack matrix, int i, int j, float f) {
        this.fillGradient(matrix, 0, 0, this.width, this.height, -1072689136, -804253680);
        this.scrollingSongs.render(matrix, i, j, f);
        this.renderBorders(0, 32);
        this.renderBorders(this.height-32, this.height);
        super.render(matrix, i, j, f);
        String curInfo = new TranslatableText("screen.musictriggers.text.edit_song").getString();
        if(this.scrollingSongs.getSelectedOrNull()!=null) curInfo = this.holder.getLinkingInfoAtIndex(this.songCode, this.song, this.scrollingSongs.index);
        drawCenteredText(matrix, this.textRenderer, curInfo, width/2, 8, 10526880);
    }

    @Override
    public boolean keyPressed(int keyCode, int i, int j) {
        if(keyCode==259) {
            if(!this.holder.getLinkingInfoAtIndex(this.songCode, this.song, this.scrollingSongs.index).matches("")) {
                this.holder.editLinkingInfoParameter(this.songCode, this.song, this.scrollingSongs.index, StringUtils.chop(this.holder.getLinkingInfoAtIndex(this.songCode, this.song, this.scrollingSongs.index)));
                return true;
            } else if(this.holder.isLinkingInfoTrigger(this.songCode, this.song, this.scrollingSongs.index)) {
                this.holder.removeLinkingTrigger(this.songCode, this.song, this.scrollingSongs.index);
                this.scrollingSongs.refreshList(this.holder.getAllLinkingInfo(this.songCode, this.song));
                return true;
            }
        }
        return super.keyPressed(keyCode, i, j);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if(this.scrollingSongs.getSelectedOrNull()!=null) {
            this.holder.editLinkingInfoParameter(this.songCode, this.song, this.scrollingSongs.index, this.holder.getLinkingInfoAtIndex(this.songCode, this.song, this.scrollingSongs.index) + typedChar);
            return true;
        }
        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public void init() {
        this.addBackButton();
        this.addScrollable();
        this.addAddTriggerButton();
        this.addAddLoopsButton();
        this.addDeleteButton();
        EventsClient.renderDebug = false;
    }

    private void addScrollable() {
        this.scrollingSongs = new GuiScrollingLinkingInfo(this.client, this.width, this.height,32,this.height-32, this.info,this);
        this.scrollingSongs.setRenderBackground(false);
        this.scrollingSongs.setRenderHorizontalShadows(false);
        this.addSelectableChild(this.scrollingSongs);
    }

    private void addBackButton() {
        this.addDrawableChild(new ButtonWidget(16, 8, 64, 16, new TranslatableText("screen.musictriggers.button.back"),
                (button) -> {
                    assert this.client != null;
                    if(this.parentScreen instanceof GuiLinking) ((GuiLinking) this.parentScreen).holder = this.holder;
                    else ((GuiAddSongs) this.parentScreen).holder = this.holder;
                    this.client.setScreen(this.parentScreen);
                }));
    }

    private void addDeleteButton() {
        this.addDrawableChild(new ButtonWidget(this.width - 80, 8, 64, 16, new TranslatableText("screen.musictriggers.button.delete").setStyle(Style.EMPTY.withFormatting(Formatting.RED)),
                (button) -> {
                    assert this.client != null;
                    if (this.parentScreen instanceof GuiLinking parent) {
                        parent.holder.removeLinkingSong(this.songCode, this.song);
                        parent.songs = parent.holder.getAllSongsForLinking(this.songCode);
                    } else ((GuiAddSongs) this.parentScreen).holder = this.holder;
                    this.client.setScreen(this.parentScreen);
                }));
    }

    private void addAddTriggerButton() {
        this.addDrawableChild(new ButtonWidget(this.width/2-114, this.height-24, 96, 16, new TranslatableText("screen.musictriggers.button.add_trigger"),
                (button) -> {
                    this.holder.addLinkingTrigger(this.songCode, this.song, "trigger");
                    this.info = this.holder.getAllLinkingInfo(this.songCode, this.song);
                    this.scrollingSongs.refreshList(this.info);
                }));
    }

    private void addAddLoopsButton() {
        this.addDrawableChild(new ButtonWidget(this.width/2+16, this.height-24, 96, 16, new TranslatableText("screen.musictriggers.button.addloop"),
                (button) -> {
                    assert this.client != null;
                    this.client.setScreen(new GuiLoops(this,this.holder,this.songCode,this.song,true));
                }));
    }

    @Override
    public void renderBackgroundTexture(int i) {}

    protected void renderBorders(int startY, int endY) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, this.background);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferbuilder.vertex(0, endY, 0.0D).texture(0.0f, (float)endY / 32.0F).color(64, 64, 64, 255).next();
        bufferbuilder.vertex(this.width, endY, 0.0D).texture((float)this.width / 32.0F, (float)endY / 32.0F).color(64, 64, 64, 255).next();
        bufferbuilder.vertex(this.width, startY, 0.0D).texture((float)this.width / 32.0F, (float)startY / 32.0F).color(64, 64, 64, 255).next();
        bufferbuilder.vertex(0, startY, 0.0D).texture(0.0f, (float)startY / 32.0F).color(64, 64, 64, 255).next();
        tessellator.draw();
    }

    @Override
    public void close() {
        EventsClient.renderDebug = true;
        super.close();
    }
}
