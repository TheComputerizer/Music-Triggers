package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import mods.thecomputerizer.musictriggers.client.eventsClient;
import mods.thecomputerizer.musictriggers.config.configObject;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.List;

public class GuiCurPlaying extends Screen {

    public Screen parentScreen;
    public List<String> parameters;
    public configObject holder;
    private final ResourceLocation background;
    public CustomSlider slider;

    public GuiCurPlaying(Screen parentScreen, configObject holder) {
        super(new TranslatableComponent("screen.musictriggers.curplaying"));
        this.parentScreen = parentScreen;
        this.holder = holder;
        this.background = new ResourceLocation(MusicTriggers.MODID,"textures/block/recorder_side_active.png");
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
    public void init() {
        this.addBackButton();
        this.addSkipSongButton();
        this.addSongSlider();
        eventsClient.renderDebug = false;
    }


    private void addBackButton() {
        this.addRenderableWidget(new Button(16, 8, 64, 16, new TranslatableComponent("screen.musictriggers.button.back"),
                (button) -> {
                    assert this.minecraft != null;
                    ((GuiMain)this.parentScreen).holder = this.holder;
                    this.minecraft.setScreen(this.parentScreen);
                }));
    }

    private void addSkipSongButton() {
        this.addRenderableWidget(new Button(this.width - 80, 8, 64, 16, new TranslatableComponent("screen.musictriggers.button.skip_song").withStyle(ChatFormatting.RED),
                (button) -> {
                    if(MusicPlayer.curMusic!=null && !MusicPlayer.reloading && !MusicPlayer.playing && !MusicPlayer.fading) {
                        assert this.minecraft != null;
                        this.minecraft.getSoundManager().stop(MusicPlayer.curMusic);
                    }
                }));
    }

    private void addSongSlider() {
        this.slider = new CustomSlider(this.width/2-80,this.height/2-10, 160,20, new TranslatableComponent("screen.musictriggers.button.slider"), getSongPosInSeconds(MusicPlayer.curMusic), this.holder, this.getMaxSongSeconds(MusicPlayer.curMusic));
        this.addRenderableWidget(this.slider);
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
        eventsClient.renderDebug = true;
        super.onClose();
    }

    public void setSlider(double value) {
        this.slider.set(value);
    }

    public float getMaxSongSeconds(SoundInstance sound) {
        float seconds = 134;
        try {
            assert this.minecraft != null;
            InputStream is = this.minecraft.getResourceManager().getResource(new ResourceLocation(sound.getLocation().toString().replaceAll("\\.","/"))).getInputStream();
            float length = is.available();
            seconds = length/(44100*16*2);
            MusicTriggers.logger.info("Second length: "+seconds);
        } catch (Exception e) {
            MusicTriggers.logger.error("Could not read size of sound");
            e.printStackTrace();
        }
        return seconds;
    }

    public static double getSongPosInSeconds(SoundInstance sound) {
        double seconds = 0;
        try {
            if(sound!=null) seconds = Math.floor((float)MusicPlayer.curMusicTimer/1000f);
        } catch (Exception e) {
            MusicTriggers.logger.error("Could not get current position of song");
            e.printStackTrace();
        }
        return seconds;
    }
}
