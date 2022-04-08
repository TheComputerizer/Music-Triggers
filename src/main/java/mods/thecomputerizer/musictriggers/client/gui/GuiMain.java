package mods.thecomputerizer.musictriggers.client.gui;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.MusicPicker;
import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import mods.thecomputerizer.musictriggers.client.eventsClient;
import mods.thecomputerizer.musictriggers.config.configObject;
import mods.thecomputerizer.musictriggers.util.json;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class GuiMain extends Screen {

    public boolean reload;
    private final ResourceLocation background;
    public configObject holder;

    public GuiMain(configObject holder) {
        super(new TranslatableComponent("screen.musictriggers.main"));
        this.reload = false;
        this.background = new ResourceLocation(MusicTriggers.MODID,"textures/block/recorder_side_active.png");
        this.holder = holder;
        this.holder.addAllExistingParameters();
    }

    @Override
    public void render(@NotNull PoseStack matrix, int i, int j, float f) {
        this.fillGradient(matrix, 0, 0, this.width, this.height, -1072689136, -804253680);
        this.renderBorders(0, 32);
        this.renderBorders(this.height-32, this.height);
        super.render(matrix, i, j, f);
        drawCenteredString(matrix, this.font, this.title, width/2, 8, 10526880);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        MusicTriggers.logger.info("Char: "+typedChar+" Code: "+keyCode);
        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public boolean keyPressed(int keyCode, int i, int j) {
        MusicTriggers.logger.info("Key Pressed "+keyCode);
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
        this.addRenderableWidget(new Button(this.width / 2 +25, this.height / 2 - 50, 150, 20, new TranslatableComponent("screen.musictriggers.button.apply_all"),
                (button) -> {
                    try {
                        this.holder.write();
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Could not write new configuration to file! Check the log for the actual stacktrace");
                    }
                    this.reload = true;
                    this.onClose();
                }));
    }

    private void addReloadButton() {
        this.addRenderableWidget(new Button(this.width / 2 - 175, this.height / 2 - 50, 150, 20, new TranslatableComponent("screen.musictriggers.button.reload"),
                (button) -> {
                    this.reload = true;
                    this.onClose();
                }));
    }

    private void addAddSongsButton() {
        this.addRenderableWidget(new Button(this.width / 2 - 175, this.height / 2 - 10, 150, 20, new TranslatableComponent("screen.musictriggers.button.add_songs"),
                (button) -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new GuiAddSongs(this, json.allSongs, holder, null));
                }));
    }

    private void addEditSongsButton() {
        this.addRenderableWidget(new Button(this.width / 2 + 25, this.height / 2 - 10, 150, 20, new TranslatableComponent("screen.musictriggers.button.edit_songs"),
                (button) -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new GuiEditSongs(this, holder));
                }));
    }

    private void addTransitionsButton() {
        this.addRenderableWidget(new Button(this.width / 2 - 175, this.height / 2 + 30, 150, 20, new TranslatableComponent("screen.musictriggers.button.transitions"),
                (button) -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new GuiTransitions(this, holder));
                }));
    }

    private void addDebugButton() {
        this.addRenderableWidget(new Button(this.width / 2 + 25, this.height / 2 + 30, 150, 20, new TranslatableComponent("screen.musictriggers.button.other"),
                (button) -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new GuiOther(this, holder));
                }));
    }

    private void addCurrentSongButton() {
        this.addRenderableWidget(new Button(this.width - 80, 8, 64, 16, new TranslatableComponent("screen.musictriggers.button.playback"),
                (button) -> {
                    if(MusicPlayer.curMusic!=null && !MusicPlayer.reloading && !MusicPlayer.playing) {
                        assert this.minecraft != null;
                        this.minecraft.setScreen(new GuiCurPlaying(this,this.holder));
                    }
                }));
    }

    @Override
    public void onClose() {
        eventsClient.renderDebug = true;
        if(this.reload) {
            assert this.minecraft != null;
            if (this.minecraft.player!=null) {
                this.minecraft.getSoundManager().stop();
                MusicPicker.player.sendMessage(new TranslatableComponent("musictriggers.reloading").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.ITALIC), MusicPicker.player.getUUID());
                MusicPlayer.reloading = true;
                eventsClient.reloadCounter = 5;
            }
        }
        super.onClose();
    }

    @Override
    public void renderDirtBackground(int i) {}

    protected void renderBorders(int startY, int endY) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, this.background);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(0, endY, 0.0D).uv(0.0f, (float)endY / 32.0F).color(64, 64, 64, 255).endVertex();
        bufferbuilder.vertex(this.width, endY, 0.0D).uv((float)this.width / 32.0F, (float)endY / 32.0F).color(64, 64, 64, 255).endVertex();
        bufferbuilder.vertex(this.width, startY, 0.0D).uv((float)this.width / 32.0F, (float)startY / 32.0F).color(64, 64, 64, 255).endVertex();
        bufferbuilder.vertex(0, startY, 0.0D).uv(0.0f, (float)startY / 32.0F).color(64, 64, 64, 255).endVertex();
        tesselator.end();
        MinecraftForge.EVENT_BUS.post(new ScreenEvent.BackgroundDrawnEvent(this, new PoseStack()));
    }
}
