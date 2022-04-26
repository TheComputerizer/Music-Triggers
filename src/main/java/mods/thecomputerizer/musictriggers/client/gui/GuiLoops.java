package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.config.ConfigObject;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GuiLoops extends Screen {

    public List<String> info;
    public Screen parentScreen;
    public GuiScrollingLoops scrollingSongs;
    public ConfigObject holder;
    public String code;
    public String song;
    public boolean linked;
    private final Identifier background;

    public GuiLoops(Screen parentScreen, ConfigObject holder, String code, String song, boolean linked) {
        super(new TranslatableText("screen.musictriggers.loops"));
        this.parentScreen = parentScreen;
        this.holder = holder;
        this.code = code;
        this.song = song;
        this.linked = linked;
        this.info = this.holder.getAllLoops(this.code,this.song,this.linked);
        this.background = new Identifier(MusicTriggersCommon.MODID,"textures/block/recorder_side_active.png");
    }

    @Override
    public void render(@NotNull MatrixStack matrix, int i, int j, float f) {
        this.fillGradient(matrix, 0, 0, this.width, this.height, -1072689136, -804253680);
        this.scrollingSongs.render(matrix, i, j, f);
        this.renderBorders(0, 32);
        this.renderBorders(this.height-32, this.height);
        super.render(matrix, i, j, f);
        String curInfo;
        if(!linked) curInfo = "Loops";
        else curInfo = "Linked Loops";
        if(!this.holder.buildLoopTitle(this.code,this.song,this.linked,this.scrollingSongs.index).isEmpty()) {
            if (this.scrollingSongs.getSelectedOrNull() != null)
                curInfo = this.holder.buildLoopTitle(this.code, this.song, this.linked, this.scrollingSongs.index);
        }
        drawCenteredText(matrix, this.textRenderer, curInfo, width/2, 8, 10526880);
    }

    @Override
    public void init() {
        this.addBackButton();
        this.addScrollable();
        this.addAddLoopButton();
        EventsClient.renderDebug = false;
    }

    private void addScrollable() {
        this.scrollingSongs = new GuiScrollingLoops(this.client, this.width, this.height,32,this.height-32, this.info,this, this.code, this.song, this.linked);
        this.scrollingSongs.setRenderBackground(false);
        this.scrollingSongs.setRenderHorizontalShadows(false);
        this.addSelectableChild(this.scrollingSongs);
    }

    private void addBackButton() {
        this.addDrawableChild(new ButtonWidget(16, 8, 64, 16, new TranslatableText("screen.musictriggers.button.back"),
                (button) -> {
                    assert this.client != null;
                    if(!this.linked) ((GuiSongInfo)this.parentScreen).holder = this.holder;
                    else ((GuiLinkingInfo)this.parentScreen).holder = this.holder;
                    this.client.setScreen(this.parentScreen);
                }));
    }

    private void addAddLoopButton() {
        this.addDrawableChild(new ButtonWidget(this.width/2-64, this.height-24, 128, 16, new TranslatableText("screen.musictriggers.button.addloop"),
                (button) -> {
                    this.holder.addLoop(this.code,this.song,this.linked);
                    this.info = this.holder.getAllLoops(this.code,this.song,this.linked);
                    this.scrollingSongs.resetEntries(this.info);
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
    public void onClose() {
        EventsClient.renderDebug = true;
        super.onClose();
    }
}
