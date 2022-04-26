package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.config.ConfigObject;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.List;

public class GuiCurPlaying extends Screen {

    public Screen parentScreen;
    public List<String> parameters;
    public ConfigObject holder;
    private final Identifier background;
    public CustomSlider slider;

    public GuiCurPlaying(Screen parentScreen, ConfigObject holder) {
        super(new TranslatableText("screen.musictriggers.curplaying"));
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
        this.addSkipSongButton();
        this.addSongSlider();
        EventsClient.renderDebug = false;
    }


    private void addBackButton() {
        this.addDrawableChild(new ButtonWidget(16, 8, 64, 16, new TranslatableText("screen.musictriggers.button.back"),
                (button) -> {
                    assert this.client != null;
                    ((GuiMain)this.parentScreen).holder = this.holder;
                    this.client.setScreen(this.parentScreen);
                }));
    }

    private void addSkipSongButton() {
        this.addDrawableChild(new ButtonWidget(this.width - 80, 8, 64, 16, new TranslatableText("screen.musictriggers.button.skip_song").setStyle(Style.EMPTY.withFormatting(Formatting.RED)),
                (button) -> {
                    if(MusicPlayer.curMusic!=null && !MusicPlayer.reloading && !MusicPlayer.playing) {
                        assert this.client != null;
                        this.client.getSoundManager().stop(MusicPlayer.curMusic);
                    }
                }));
    }

    private void addSongSlider() {
        this.slider = new CustomSlider(this.width/2-80,this.height/2-10, 160,20, new TranslatableText("screen.musictriggers.button.slider"), getSongPosInSeconds(MusicPlayer.curMusic), this.holder, this.getMaxSongSeconds(MusicPlayer.curMusic));
        this.addSelectableChild(this.slider);
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

    public void setSlider(double value) {
        this.slider.set(value);
    }

    public float getMaxSongSeconds(SoundInstance sound) {
        float seconds = 134;
        try {
            assert this.client != null;
            InputStream is = this.client.getResourceManager().getResource(new Identifier(sound.getId().toString().replaceAll("\\.","/"))).getInputStream();
            float length = is.available();
            seconds = length/(44100*16*2);
            MusicTriggersCommon.logger.info("Second length: "+seconds);
        } catch (Exception e) {
            MusicTriggersCommon.logger.error("Could not read size of sound");
            e.printStackTrace();
        }
        return seconds;
    }

    public static double getSongPosInSeconds(SoundInstance sound) {
        double seconds = 0;
        try {
            if(sound!=null) seconds = Math.floor((float)MusicPlayer.curMusicTimer/1000f);
        } catch (Exception e) {
            MusicTriggersCommon.logger.error("Could not get current position of song");
            e.printStackTrace();
        }
        return seconds;
    }
}
