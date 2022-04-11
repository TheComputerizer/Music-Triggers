package mods.thecomputerizer.musictriggers.client.gui;


import com.mojang.blaze3d.systems.RenderSystem;
import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.client.MusicPicker;
import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import mods.thecomputerizer.musictriggers.client.eventsClient;
import mods.thecomputerizer.musictriggers.config.configObject;
import mods.thecomputerizer.musictriggers.util.json;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class GuiMain extends Screen {

    public boolean reload;
    private final Identifier background;
    public configObject holder;

    public GuiMain(configObject holder) {
        super(new TranslatableText("screen.musictriggers.main"));
        this.reload = false;
        this.background = new Identifier(MusicTriggersCommon.MODID,"textures/block/recorder_side_active.png");
        this.holder = holder;
        this.holder.addAllExistingParameters();
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
    public boolean charTyped(char typedChar, int keyCode) {
        MusicTriggersCommon.logger.info("Char: "+typedChar+" Code: "+keyCode);
        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public boolean keyPressed(int keyCode, int i, int j) {
        MusicTriggersCommon.logger.info("Key Pressed "+keyCode);
        return super.keyPressed(keyCode, i, j);
    }

    @Override
    public void init() {
        this.addApplyButton();
        this.addReloadButton();
        this.addAddSongsButton();
        this.addEditSongsButton();
        this.addTransitionsButton();
        this.addDebugButton();
        this.addCurrentSongButton();
        eventsClient.renderDebug = false;
    }

    private void addApplyButton() {
        this.addDrawableChild(new ButtonWidget(this.width / 2 +25, this.height / 2 - 50, 150, 20, new TranslatableText("screen.musictriggers.button.apply_all"),
                (button) -> {
                    try {
                        this.holder.write();
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Could not write new configuration to file! Check the log for the actual stacktrace");
                    }
                    this.reload = true;
                    this.close();
                }));
    }

    private void addReloadButton() {
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 175, this.height / 2 - 50, 150, 20, new TranslatableText("screen.musictriggers.button.reload"),
                (button) -> {
                    this.reload = true;
                    this.close();
                }));
    }

    private void addAddSongsButton() {
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 175, this.height / 2 - 10, 150, 20, new TranslatableText("screen.musictriggers.button.add_songs"),
                (button) -> {
                    assert this.client != null;
                    this.client.setScreen(new GuiAddSongs(this, json.allSongs, holder, null));
                }));
    }

    private void addEditSongsButton() {
        this.addDrawableChild(new ButtonWidget(this.width / 2 + 25, this.height / 2 - 10, 150, 20, new TranslatableText("screen.musictriggers.button.edit_songs"),
                (button) -> {
                    assert this.client != null;
                    this.client.setScreen(new GuiEditSongs(this, holder));
                }));
    }

    private void addTransitionsButton() {
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 175, this.height / 2 + 30, 150, 20, new TranslatableText("screen.musictriggers.button.transitions"),
                (button) -> {
                    assert this.client != null;
                    this.client.setScreen(new GuiTransitions(this, holder));
                }));
    }

    private void addDebugButton() {
        this.addDrawableChild(new ButtonWidget(this.width / 2 + 25, this.height / 2 + 30, 150, 20, new TranslatableText("screen.musictriggers.button.other"),
                (button) -> {
                    assert this.client != null;
                    this.client.setScreen(new GuiOther(this, holder));
                }));
    }

    private void addCurrentSongButton() {
        this.addDrawableChild(new ButtonWidget(this.width - 80, 8, 64, 16, new TranslatableText("screen.musictriggers.button.playback"),
                (button) -> {
                    if(MusicPlayer.curMusic!=null && !MusicPlayer.reloading && !MusicPlayer.playing) {
                        assert this.client != null;
                        this.client.setScreen(new GuiCurPlaying(this,this.holder));
                    }
                }));
    }

    @Override
    public void close() {
        eventsClient.renderDebug = true;
        if(this.reload) {
            assert this.client != null;
            if (this.client.player!=null) {
                this.client.getSoundManager().stopAll();
                MusicPicker.player.sendMessage(new TranslatableText("musictriggers.reloading").setStyle(Style.EMPTY.withFormatting(Formatting.RED).withFormatting(Formatting.ITALIC)), false);
                MusicPlayer.reloading = true;
                eventsClient.reloadCounter = 5;
            }
        }
        super.close();
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
}
