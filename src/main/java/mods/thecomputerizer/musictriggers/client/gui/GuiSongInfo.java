package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.client.eventsClient;
import mods.thecomputerizer.musictriggers.config.configObject;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GuiSongInfo extends Screen {

    public String song;
    public String songCode;
    public List<String> info;
    public List<String> triggersCodes;
    public Screen parentScreen;
    public GuiScrollingInfo scrollingSongs;
    public List<String> parameters;
    public configObject holder;
    private final Identifier background;

    public GuiSongInfo(Screen parentScreen, String song, String songCode, configObject holder) {
        super(new TranslatableText("screen.musictriggers.song_info"));
        this.parentScreen = parentScreen;
        this.song = song;
        this.songCode = songCode;
        this.parameters = Arrays.stream(new String[]{"pitch","play_once","must_finish","chance","volume"}).collect(Collectors.toList());
        this.holder = holder;
        this.triggersCodes = this.holder.getAllTriggersForCode(this.songCode);
        this.background = new Identifier(MusicTriggersCommon.MODID,"textures/block/recorder_side_active.png");
    }

    @Override
    public void render(@NotNull MatrixStack matrix, int i, int j, float f) {
        this.fillGradient(matrix, 0, 0, this.width, this.height, -1072689136, -804253680);
        this.scrollingSongs.render(matrix, i, j, f);
        this.renderBorders(0, 32);
        this.renderBorders(this.height-32, this.height);
        super.render(matrix, i, j, f);
        String curInfo = this.song;
        if(this.scrollingSongs.getSelectedOrNull()!=null) curInfo = this.holder.getSongInfoAtIndex(this.songCode, this.scrollingSongs.index);
        drawCenteredText(matrix, this.textRenderer, curInfo, width/2, 8, 10526880);
    }

    @Override
    public boolean keyPressed(int keyCode, int i, int j) {
        if(keyCode==259 && !this.holder.getSongInfoAtIndex(this.songCode, this.scrollingSongs.index).matches("")) {
            this.holder.editOtherInfoParameter(this.songCode, this.scrollingSongs.index, StringUtils.chop(this.holder.getSongInfoAtIndex(this.songCode, this.scrollingSongs.index)));
            return true;
        }
        return super.keyPressed(keyCode, i, j);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if(this.scrollingSongs.getSelectedOrNull()!=null) {
            this.holder.editOtherInfoParameter(this.songCode, this.scrollingSongs.index, this.holder.getSongInfoAtIndex(this.songCode, this.scrollingSongs.index) + typedChar);
            return true;
        }
        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public void init() {
        this.addBackButton();
        this.addScrollable();
        this.addAddTriggerButton();
        this.addLinkingButton();
        this.addLoopsButton();
        this.addDeleteButton();
        eventsClient.renderDebug = false;
    }

    private void addScrollable() {
        List<String> everything = new ArrayList<>();
        everything.addAll(this.parameters);
        everything.addAll(this.triggersCodes);
        this.scrollingSongs = new GuiScrollingInfo(this.client, this.width, this.height,32,this.height-32, everything,this, this.holder);
        this.scrollingSongs.setRenderBackground(false);
        this.scrollingSongs.setRenderHorizontalShadows(false);
        this.addSelectableChild(this.scrollingSongs);
    }

    private void addBackButton() {
        this.addDrawableChild(new ButtonWidget(16, 8, 64, 16, new TranslatableText("screen.musictriggers.button.back"),
                (button) -> {
                    assert this.client != null;
                    if(this.parentScreen instanceof GuiAddSongs) ((GuiAddSongs)this.parentScreen).holder = this.holder;
                    else ((GuiEditSongs)this.parentScreen).holder = this.holder;
                    this.client.setScreen(this.parentScreen);
                }));
    }

    private void addDeleteButton() {
        this.addDrawableChild(new ButtonWidget(this.width - 80, 8, 64, 16, new TranslatableText("screen.musictriggers.button.delete").setStyle(Style.EMPTY.withFormatting(Formatting.RED)),
                (button) -> {
                    assert this.client != null;
                    if(this.parentScreen instanceof GuiAddSongs parent) {
                        parent.holder.removeSong(this.songCode);
                    } else {
                        GuiEditSongs parent = ((GuiEditSongs)this.parentScreen);
                        parent.holder.removeSong(this.songCode);
                        parent.songs = parent.holder.getAllSongs();
                        parent.codes = parent.holder.getAllCodes();
                    }
                    this.client.setScreen(this.parentScreen);
                }));
    }

    private void addAddTriggerButton() {
        this.addDrawableChild(new ButtonWidget(this.width/2-48, this.height-24, 96, 16, new TranslatableText("screen.musictriggers.button.add_trigger"),
                (button) -> {
                    assert this.client != null;
                    this.client.setScreen(new GuiTriggers(this, this.holder, this.songCode));
                }));
    }

    private void addLinkingButton() {
        this.addDrawableChild(new ButtonWidget(this.width/2+64, this.height-24, 96, 16, new TranslatableText("screen.musictriggers.button.linking"),
                (button) -> {
                    assert this.client != null;
                    this.client.setScreen(new GuiLinking(this, this.songCode, this.holder));
                }));
    }

    private void addLoopsButton() {
        this.addDrawableChild(new ButtonWidget(this.width/2-160, this.height-24, 96, 16, new TranslatableText("screen.musictriggers.button.loops"),
                (button) -> {
                    assert this.client != null;
                    this.client.setScreen(new GuiLoops(this, this.holder, this.songCode, null, false));
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
        eventsClient.renderDebug = true;
        super.close();
    }
}
