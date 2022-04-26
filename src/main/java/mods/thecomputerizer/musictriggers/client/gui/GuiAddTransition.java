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

public class GuiAddTransition extends Screen {

    public Screen parentScreen;
    private final Identifier background;
    public ConfigObject holder;

    public GuiAddTransition(Screen parentScreen, ConfigObject holder) {
        super(new TranslatableText("screen.musictriggers.add_transition"));
        this.parentScreen = parentScreen;
        this.holder = holder;
        this.background = new Identifier(MusicTriggersCommon.MODID,"textures/block/recorder_side_active.png");
    }

    @Override
    public void render(@NotNull MatrixStack matrix, int i, int j, float f) {
        this.fillGradient(matrix, 0, 0, this.width, this.height, -1072689136, -804253680);
        this.renderBorders(0, 32);
        this.renderBorders(this.height-32, this.height);
        super.render(matrix, i, j, f);
        drawCenteredText(matrix, this.textRenderer, this.title, width/2, 8, 10526880);
    }

    @Override
    public void init() {
        this.addBackButton();
        this.addTitleCardButton();
        this.addImageCardButton();
        EventsClient.renderDebug = false;
    }

    private void addBackButton() {
        this.addDrawableChild(new ButtonWidget(16, 8, 64, 16, new TranslatableText("screen.musictriggers.button.back"),
                (button) -> {
                    assert this.client != null;
                    ((GuiTransitions)this.parentScreen).holder = this.holder;
                    this.client.setScreen(this.parentScreen);
                }));
    }

    private void addTitleCardButton() {
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 175, this.height / 2 - 10, 150, 20, new TranslatableText("screen.musictriggers.button.title_card"),
                (button) -> {
                    assert this.client != null;
                    this.client.setScreen(new GuiTransitionInfo(this, holder, 0, true, true, false, ""));
                }));
    }

    private void addImageCardButton() {
        this.addDrawableChild(new ButtonWidget(this.width / 2 +25, this.height / 2 - 10, 150, 20, new TranslatableText("screen.musictriggers.button.image_card"),
                (button) -> {
                    assert this.client != null;
                    this.client.setScreen(new GuiChooseImage(this, this.holder));
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
