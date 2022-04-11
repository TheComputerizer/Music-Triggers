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

import java.util.List;

public class GuiLoopInfo extends Screen {

    public List<String> info;
    public Screen parentScreen;
    public GuiScrollingLoopsInfo scrollingSongs;
    public configObject holder;
    public int loopIndex;
    public String code;
    public String song;
    public boolean linked;
    private final Identifier background;

    public GuiLoopInfo(Screen parentScreen, configObject holder, int loopIndex, String code, String song, boolean linked) {
        super(new TranslatableText("screen.musictriggers.loop_info"));
        this.parentScreen = parentScreen;
        this.holder = holder;
        this.loopIndex = loopIndex;
        this.code = code;
        this.song = song;
        this.linked = linked;
        this.info = this.holder.getAllLoopInfo();
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
        if(!linked) curInfo = "Loop Info";
        else curInfo = "Linked Loop Info";
        if(this.scrollingSongs.getSelectedOrNull()!=null) curInfo = this.holder.getLoopParameter(this.code,this.song,this.linked,this.loopIndex,this.scrollingSongs.index);
        drawCenteredText(matrix, this.textRenderer, curInfo, width/2, 8, 10526880);
    }

    @Override
    public boolean keyPressed(int keyCode, int i, int j) {
        if(keyCode==259 && !this.holder.getLoopParameter(this.code,this.song,this.linked,this.loopIndex,this.scrollingSongs.index).matches("")) {
            this.holder.editLoopInfoAtIndex(this.code,this.song,this.linked,this.loopIndex,this.scrollingSongs.index, StringUtils.chop(this.holder.getLoopParameter(this.code,this.song,this.linked,this.loopIndex,this.scrollingSongs.index)));
            return true;
        }
        return super.keyPressed(keyCode, i, j);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if(this.scrollingSongs.getSelectedOrNull()!=null) {
            this.holder.editLoopInfoAtIndex(this.code,this.song,this.linked,this.loopIndex,this.scrollingSongs.index, this.holder.getLoopParameter(this.code,this.song,this.linked,this.loopIndex,this.scrollingSongs.index)+typedChar);
            return true;
        }
        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public void init() {
        this.addBackButton();
        this.addDeleteButton();
        this.addScrollable();
        eventsClient.renderDebug = false;
    }

    private void addScrollable() {
        this.scrollingSongs = new GuiScrollingLoopsInfo(this.client, this.width, this.height,32,this.height-32, this.info,this);
        this.scrollingSongs.setRenderBackground(false);
        this.scrollingSongs.setRenderHorizontalShadows(false);
        this.addSelectableChild(this.scrollingSongs);
    }

    private void addBackButton() {
        this.addDrawableChild(new ButtonWidget(16, 8, 64, 16, new TranslatableText("screen.musictriggers.button.back"),
                (button) -> {
                    assert this.client != null;
                    ((GuiLoops)this.parentScreen).holder = this.holder;
                    this.client.setScreen(this.parentScreen);
                }));
    }

    private void addDeleteButton() {
        this.addDrawableChild(new ButtonWidget(this.width - 80, 8, 64, 16, new TranslatableText("screen.musictriggers.button.delete").setStyle(Style.EMPTY.withFormatting(Formatting.RED)),
                (button) -> {
                    assert this.client != null;
                    this.holder.removeLoop(this.code,this.song,this.linked,this.loopIndex);
                    ((GuiLoops)this.parentScreen).holder = this.holder;
                    ((GuiLoops)this.parentScreen).info = this.holder.getAllLoops(this.code,this.song,this.linked);
                    ((GuiLoops)this.parentScreen).scrollingSongs.resetEntries(((GuiLoops)this.parentScreen).info);
                    this.client.setScreen(this.parentScreen);
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
