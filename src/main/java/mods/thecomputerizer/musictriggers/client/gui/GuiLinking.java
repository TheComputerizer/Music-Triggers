package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.config.ConfigObject;
import mods.thecomputerizer.musictriggers.util.Json;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GuiLinking extends Screen {
    public List<String> songs;
    public String songCode;
    public Screen parentScreen;
    public GuiScrollingLinking scrollingSongs;
    public ConfigObject holder;
    private final ResourceLocation background;

    public GuiLinking(Screen parentScreen, String songCode, ConfigObject holder) {
        super(new TranslatableComponent("screen.musictriggers.linking"));
        this.parentScreen = parentScreen;
        this.songCode = songCode;
        this.holder = holder;
        this.songs = this.holder.getAllSongsForLinking(this.songCode);
        this.background = new ResourceLocation(MusicTriggers.MODID,"textures/block/recorder_side_active.png");
    }

    @Override
    public void render(@NotNull PoseStack matrix, int i, int j, float f) {
        this.fillGradient(matrix, 0, 0, this.width, this.height, -1072689136, -804253680);
        this.scrollingSongs.render(matrix, i, j, f);
        this.renderBorders(0, 32);
        this.renderBorders(this.height-32, this.height);
        super.render(matrix, i, j, f);
        drawCenteredString(matrix, this.font, new TranslatableComponent("screen.musictriggers.text.edit_song"), width/2, 8, 10526880);
    }

    @Override
    public void init() {
        this.addBackButton();
        this.addAddSongButton();
        this.addSongs();
        EventsClient.renderDebug = false;
    }

    private void addSongs() {
        this.scrollingSongs = new GuiScrollingLinking(this.minecraft, this.width, this.height,32,this.height-32, this.songs,this);
        this.scrollingSongs.setRenderBackground(false);
        this.scrollingSongs.setRenderTopAndBottom(false);
        this.addWidget(this.scrollingSongs);
    }

    private void addBackButton() {
        this.addRenderableWidget(new Button(16, 8, 64, 16, new TranslatableComponent("screen.musictriggers.button.back"),
                (button) -> {
                    assert this.minecraft != null;
                    ((GuiSongInfo)this.parentScreen).holder = this.holder;
                    this.minecraft.setScreen(this.parentScreen);
                }));
    }

    private void addAddSongButton() {
        this.addRenderableWidget(new Button(this.width/2-64, this.height-24, 128, 16, new TranslatableComponent("screen.musictriggers.button.add_song"),
                (button) -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new GuiAddSongs(this, Json.allSongs, this.holder, this));
                }));
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

    @Override
    public void onClose() {
        EventsClient.renderDebug = true;
        super.onClose();
    }

}
