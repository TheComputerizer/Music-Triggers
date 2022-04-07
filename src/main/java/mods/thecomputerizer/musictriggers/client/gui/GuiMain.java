package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.MusicPicker;
import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import mods.thecomputerizer.musictriggers.client.eventsClient;
import mods.thecomputerizer.musictriggers.config.configObject;
import mods.thecomputerizer.musictriggers.util.json;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;

import java.io.IOException;

public class GuiMain extends Screen {

    public boolean reload;
    private final ResourceLocation background;
    public configObject holder;

    public GuiMain(configObject holder) {
        super(new TranslationTextComponent("screen.musictriggers.main"));
        this.reload = false;
        this.background = new ResourceLocation(MusicTriggers.MODID,"textures/block/recorder_side_active.png");
        this.holder = holder;
        this.holder.addAllExistingParameters();
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
        assert this.minecraft != null;
        this.minecraft.getSoundManager().stop(null, SoundCategory.MUSIC);
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
        this.addButton(new Button(this.width / 2 +25, this.height / 2 - 50, 150, 20, new TranslationTextComponent("screen.musictriggers.button.apply_all"),
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
        this.addButton(new Button(this.width / 2 - 175, this.height / 2 - 50, 150, 20, new TranslationTextComponent("screen.musictriggers.button.reload"),
                (button) -> {
                    this.reload = true;
                    this.onClose();
                }));
    }

    private void addAddSongsButton() {
        this.addButton(new Button(this.width / 2 - 175, this.height / 2 - 10, 150, 20, new TranslationTextComponent("screen.musictriggers.button.add_songs"),
                (button) -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new GuiAddSongs(this, json.allSongs, holder, null));
                }));
    }

    private void addEditSongsButton() {
        this.addButton(new Button(this.width / 2 + 25, this.height / 2 - 10, 150, 20, new TranslationTextComponent("screen.musictriggers.button.edit_songs"),
                (button) -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new GuiEditSongs(this, holder));
                }));
    }

    private void addTransitionsButton() {
        this.addButton(new Button(this.width / 2 - 175, this.height / 2 + 30, 150, 20, new TranslationTextComponent("screen.musictriggers.button.transitions"),
                (button) -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new GuiTransitions(this, holder));
                }));
    }

    private void addDebugButton() {
        this.addButton(new Button(this.width / 2 + 25, this.height / 2 + 30, 150, 20, new TranslationTextComponent("screen.musictriggers.button.other"),
                (button) -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new GuiOther(this, holder));
                }));
    }

    private void addCurrentSongButton() {
        this.addButton(new Button(this.width - 80, 8, 64, 16, new TranslationTextComponent("screen.musictriggers.button.playback"),
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
                MusicPicker.player.sendMessage(new TranslationTextComponent("musictriggers.reloading").withStyle(TextFormatting.RED).withStyle(TextFormatting.ITALIC), MusicPicker.player.getUUID());
                MusicPlayer.reloading = true;
                eventsClient.reloadCounter = 5;
            }
        }
        super.onClose();
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
}
