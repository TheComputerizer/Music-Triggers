package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import mods.thecomputerizer.musictriggers.client.eventsClient;
import mods.thecomputerizer.musictriggers.config.configObject;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;

import java.io.InputStream;
import java.util.List;

public class GuiCurPlaying extends Screen {

    public Screen parentScreen;
    public List<String> parameters;
    public configObject holder;
    private final ResourceLocation background;
    public CustomSlider slider;

    public GuiCurPlaying(Screen parentScreen, configObject holder) {
        super(new TranslationTextComponent("screen.musictriggers.curplaying"));
        this.parentScreen = parentScreen;
        this.holder = holder;
        this.background = new ResourceLocation(MusicTriggers.MODID,"textures/block/recorder_side_active.png");
    }

    @Override
    public void render(MatrixStack matrix, int i, int j, float f) {
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
        this.addButton(new Button(16, 8, 64, 16, new TranslationTextComponent("screen.musictriggers.button.back"),
                (button) -> {
                    assert this.minecraft != null;
                    ((GuiMain)this.parentScreen).holder = this.holder;
                    this.minecraft.setScreen(this.parentScreen);
                }));
    }

    private void addSkipSongButton() {
        this.addButton(new Button(this.width - 80, 8, 64, 16, new TranslationTextComponent("screen.musictriggers.button.skip_song").withStyle(TextFormatting.RED),
                (button) -> {
                    if(MusicPlayer.curMusic!=null && !MusicPlayer.reloading && !MusicPlayer.playing) {
                        assert this.minecraft != null;
                        this.minecraft.getSoundManager().stop(MusicPlayer.curMusic);
                    }
                }));
    }

    private void addSongSlider() {
        this.slider = new CustomSlider(this.width/2-80,this.height/2-10, 160,20, new TranslationTextComponent("screen.musictriggers.button.slider"), getSongPosInSeconds(MusicPlayer.curMusic), this.holder, this.getMaxSongSeconds(MusicPlayer.curMusic));
        this.addButton(this.slider);
    }


    @Override
    public void renderDirtBackground(int i) {}

    protected void renderBorders(int startY, int endY) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        assert this.minecraft != null;
        this.minecraft.getTextureManager().bind(this.background);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.vertex(0, endY, 0.0D).uv(0.0f, (float)endY / 32.0F).color(64, 64, 64, 255).endVertex();
        bufferbuilder.vertex(this.width, endY, 0.0D).uv((float)this.width / 32.0F, (float)endY / 32.0F).color(64, 64, 64, 255).endVertex();
        bufferbuilder.vertex(this.width, startY, 0.0D).uv((float)this.width / 32.0F, (float)startY / 32.0F).color(64, 64, 64, 255).endVertex();
        bufferbuilder.vertex(0, startY, 0.0D).uv(0.0f, (float)startY / 32.0F).color(64, 64, 64, 255).endVertex();
        tessellator.end();
        MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.BackgroundDrawnEvent(this, new MatrixStack()));
    }

    @Override
    public void onClose() {
        eventsClient.renderDebug = true;
        super.onClose();
    }

    public void setSlider(double value) {
        this.slider.set(value);
    }

    public float getMaxSongSeconds(ISound sound) {
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

    public static double getSongPosInSeconds(ISound sound) {
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
