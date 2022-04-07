package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.eventsClient;
import mods.thecomputerizer.musictriggers.config.configObject;
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
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GuiSongInfo extends Screen {

    public String song;
    public String songCode;
    public List<String> info;
    public List<String> triggersCodes;
    public Screen parentScreen;
    public GuiScrollingInfo scrollingSongs;
    public List<String> parameters;
    public configObject holder;
    private final ResourceLocation background;

    public GuiSongInfo(Screen parentScreen, String song, String songCode, configObject holder) {
        super(new TranslationTextComponent("screen.musictriggers.song_info"));
        this.parentScreen = parentScreen;
        this.song = song;
        this.songCode = songCode;
        this.parameters = Arrays.stream(new String[]{"pitch","play_once","must_finish","chance","volume"}).collect(Collectors.toList());
        this.holder = holder;
        this.triggersCodes = this.holder.getAllTriggersForCode(this.songCode);
        this.background = new ResourceLocation(MusicTriggers.MODID,"textures/block/recorder_side_active.png");
    }

    @Override
    public void render(MatrixStack matrix, int i, int j, float f) {
        this.fillGradient(matrix, 0, 0, this.width, this.height, -1072689136, -804253680);
        this.scrollingSongs.render(matrix, i, j, f);
        this.renderBorders(0, 32);
        this.renderBorders(this.height-32, this.height);
        super.render(matrix, i, j, f);
        String curInfo = this.song;
        if(this.scrollingSongs.getSelected()!=null) curInfo = this.holder.getSongInfoAtIndex(this.songCode, this.scrollingSongs.index);
        drawCenteredString(matrix, this.font, curInfo, width/2, 8, 10526880);
    }

    @Override
    public boolean keyPressed(int keyCode, int i, int j) {
        if(keyCode==259 && !this.holder.getSongInfoAtIndex(this.songCode, this.scrollingSongs.index).matches("")) {
            this.holder.editOtherInfoParameter(this.songCode, this.scrollingSongs.index, StringUtils.chop(this.holder.getSongInfoAtIndex(this.songCode, this.scrollingSongs.index)));
            return true;
        }
        return super.keyPressed(keyCode, i, j);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if(this.scrollingSongs.getSelected()!=null) {
            this.holder.editOtherInfoParameter(this.songCode, this.scrollingSongs.index, this.holder.getSongInfoAtIndex(this.songCode, this.scrollingSongs.index) + typedChar);
            return true;
        }
        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public void init() {
        this.addBackButton();
        this.addScrollable();
        this.addAddTriggerButton();
        this.addLinkingButton();
        this.addLoopsButton();
        this.addDeleteButton();
        eventsClient.renderDebug = false;
    }

    private void addScrollable() {
        List<String> everything = new ArrayList<>();
        everything.addAll(this.parameters);
        everything.addAll(this.triggersCodes);
        this.scrollingSongs = new GuiScrollingInfo(this.minecraft, this.width, this.height,32,this.height-32, everything,this, this.holder);
        this.scrollingSongs.setRenderBackground(false);
        this.scrollingSongs.setRenderTopAndBottom(false);
        this.children.add(this.scrollingSongs);
    }

    private void addBackButton() {
        this.addButton(new Button(16, 8, 64, 16, new TranslationTextComponent("screen.musictriggers.button.back"),
                (button) -> {
                    assert this.minecraft != null;
                    if(this.parentScreen instanceof GuiAddSongs) ((GuiAddSongs)this.parentScreen).holder = this.holder;
                    else ((GuiEditSongs)this.parentScreen).holder = this.holder;
                    this.minecraft.setScreen(this.parentScreen);
                }));
    }

    private void addDeleteButton() {
        this.addButton(new Button(this.width - 80, 8, 64, 16, new TranslationTextComponent("screen.musictriggers.button.delete").withStyle(TextFormatting.RED),
                (button) -> {
                    assert this.minecraft != null;
                    if(this.parentScreen instanceof GuiAddSongs) {
                        GuiAddSongs parent = (GuiAddSongs)this.parentScreen;
                        parent.holder.removeSong(this.songCode);
                    } else {
                        GuiEditSongs parent = ((GuiEditSongs)this.parentScreen);
                        parent.holder.removeSong(this.songCode);
                        parent.songs = parent.holder.getAllSongs();
                        parent.codes = parent.holder.getAllCodes();
                    }
                    this.minecraft.setScreen(this.parentScreen);
                }));
    }

    private void addAddTriggerButton() {
        this.addButton(new Button(this.width/2-48, this.height-24, 96, 16, new TranslationTextComponent("screen.musictriggers.button.add_trigger"),
                (button) -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new GuiTriggers(this, this.holder, this.songCode));
                }));
    }

    private void addLinkingButton() {
        this.addButton(new Button(this.width/2+64, this.height-24, 96, 16, new TranslationTextComponent("screen.musictriggers.button.linking"),
                (button) -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new GuiLinking(this, this.songCode, this.holder));
                }));
    }

    private void addLoopsButton() {
        this.addButton(new Button(this.width/2-160, this.height-24, 96, 16, new TranslationTextComponent("screen.musictriggers.button.loops"),
                (button) -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new GuiLoops(this, this.holder, this.songCode, null, false));
                }));
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
}
