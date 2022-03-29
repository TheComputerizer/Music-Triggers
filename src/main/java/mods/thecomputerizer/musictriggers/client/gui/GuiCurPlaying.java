package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import mods.thecomputerizer.musictriggers.client.eventsClient;
import mods.thecomputerizer.musictriggers.config.configObject;
import mods.thecomputerizer.musictriggers.config.configToml;
import mods.thecomputerizer.musictriggers.util.audio.SoundManipulator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class GuiCurPlaying extends GuiScreen implements GuiSlider.FormatHelper, GuiPageButtonList.GuiResponder {

    public GuiScreen parentScreen;
    public List<String> parameters;
    public configObject holder;
    private final ResourceLocation background;

    public GuiCurPlaying(GuiScreen parentScreen, configObject holder) {
        this.parentScreen = parentScreen;
        this.holder = holder;
        this.background = new ResourceLocation(MusicTriggers.MODID,"textures/block/recorder_side_active.png");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.overlayBackground(0, 32, 255, 255);
        this.overlayBackground(this.height-32, this.height, 255, 255);
        super.drawScreen(mouseX,mouseY,partialTicks);
        this.drawCenteredString(this.fontRenderer, "Current Playing Song", this.width/2, 8, 10526880);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if(this.selectedButton != null && state == 0 && this.selectedButton instanceof CustomSlider) {
            CustomSlider slider = (CustomSlider) this.selectedButton;
            if(slider.isMouseDown) {
                synchronized (SoundSystemConfig.THREAD_SYNC) {
                    if (MusicPlayer.curMusic != null && !MusicPlayer.reloading && !MusicPlayer.playing && !MusicPlayer.fading) {
                        float seconds = ((float) (mouseX - (slider.x + 4)) / (float) (slider.width - 8)) * slider.getMax();
                        if (seconds < 0) seconds = 0;
                        if (seconds > slider.getMax()) seconds = slider.getMax();
                        MusicTriggers.logger.info("Seconds: " + seconds);
                        SoundManipulator.setMillisecondTimeForSource(this.mc.getSoundHandler().sndManager.sndSystem, this.mc.getSoundHandler().sndManager.invPlayingSounds.get(MusicPlayer.curMusic), seconds * 1000f);
                    }
                }
            }
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar,keyCode);
    }

    @Override
    public void initGui() {
        this.addBackButton();
        this.addSkipSongButton();
        this.addSongSlider();
        eventsClient.renderDebug = false;
    }


    private void addBackButton() {
        this.buttonList.add(new GuiButton(0, 16, 8, 64, 16,"Back"));
    }

    private void addSkipSongButton() {
        this.buttonList.add(new GuiButton(1, this.width-80, 8, 64, 16, "\u00A74Skip Song"));
    }

    private void addSongSlider() {
        float max = 1f;
        if(MusicPlayer.curMusic!=null) max = this.getMaxSongSeconds(MusicPlayer.curMusic);
        CustomSlider slider = new CustomSlider(this,2,this.width/2-80,this.height/2-10, configToml.songholder.get(MusicPlayer.curTrack),0f, max, getSongPosInSeconds(MusicPlayer.curMusic),this);
        slider.setWidth(160);
        this.buttonList.add(slider);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 0) this.mc.displayGuiScreen(this.parentScreen);
        if(button.id==1) {
            if(MusicPlayer.curMusic!=null && !MusicPlayer.reloading && !MusicPlayer.playing && !MusicPlayer.fading) this.mc.getSoundHandler().stopSound(MusicPlayer.curMusic);
        }
    }

    @Override
    public void onGuiClosed() {
        eventsClient.renderDebug = true;
    }

    protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        this.mc.getTextureManager().bindTexture(this.background);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(0, endY, 0.0D).tex(0.0D, (float)endY / 32.0F).color(64, 64, 64, endAlpha).endVertex();
        bufferbuilder.pos(this.width, endY, 0.0D).tex((float)this.width / 32.0F, (float)endY / 32.0F).color(64, 64, 64, endAlpha).endVertex();
        bufferbuilder.pos(this.width, startY, 0.0D).tex((float)this.width / 32.0F, (float)startY / 32.0F).color(64, 64, 64, startAlpha).endVertex();
        bufferbuilder.pos(0, startY, 0.0D).tex(0.0D, (float)startY / 32.0F).color(64, 64, 64, startAlpha).endVertex();
        tessellator.draw();
    }

    public void setSlider(float value) {
        ((GuiSlider)this.buttonList.get(2)).setSliderValue(value, true);
    }

    public float getMaxSongSeconds(ISound sound) {
        float seconds = 134;
        try {
            InputStream is = this.mc.getResourceManager().getResource(new ResourceLocation(sound.getSoundLocation().toString().replaceAll("\\.","/"))).getInputStream();
            float length = is.available();
            seconds = length/(44100*16*2);
            MusicTriggers.logger.info("Second length: "+seconds);
        } catch (Exception e) {
            MusicTriggers.logger.error("Could not read size of sound");
            e.printStackTrace();
        }
        return seconds;
    }

    public static float getSongPosInSeconds(ISound sound) {
        float seconds = 0;
        try {
            if(sound!=null) seconds = (float)Math.floor((double)SoundManipulator.getMillisecondTimeForSource(Minecraft.getMinecraft().getSoundHandler().sndManager.sndSystem, Minecraft.getMinecraft().getSoundHandler().sndManager.invPlayingSounds.get(sound))/1000f);
        } catch (Exception e) {
            MusicTriggers.logger.error("Could not get current position of song");
            e.printStackTrace();
        }
        return seconds;
    }

    public String getText(int id, String name, float value)
    {
        int minutes = (int)(value/60);
        int seconds = (int)(value%60);
        String formattedMinutes;
        if(minutes==0) formattedMinutes = "";
        else formattedMinutes = minutes+":";
        String formattedSeconds;
        if(seconds<10) formattedSeconds = "0"+seconds;
        else formattedSeconds  = ""+seconds;
        return name + " "+formattedMinutes+formattedSeconds;
    }

    public void setEntryValue(int id, boolean value) {}

    public void setEntryValue(int id, float value) {}

    public void setEntryValue(int id, String value) {}
}
