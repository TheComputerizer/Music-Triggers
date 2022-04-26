package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.config.ConfigObject;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GuiLinkingInfo extends Screen {

    public String song;
    public String songCode;
    public List<String> info;
    public Screen parentScreen;
    public GuiScrollingLinkingInfo scrollingSongs;
    public ConfigObject holder;
    private final ResourceLocation background;

    public GuiLinkingInfo(Screen parentScreen, String song, String songCode, ConfigObject holder) {
        super(new TranslatableComponent("screen.musictriggers.linking_info"));
        this.parentScreen = parentScreen;
        this.song = song;
        this.songCode = songCode;
        this.holder = holder;
        this.info = this.holder.getAllLinkingInfo(this.songCode, this.song);
        this.background = new ResourceLocation(MusicTriggers.MODID,"textures/block/recorder_side_active.png");
    }

    @Override
    public void render(@NotNull PoseStack matrix, int i, int j, float f) {
        this.fillGradient(matrix, 0, 0, this.width, this.height, -1072689136, -804253680);
        this.scrollingSongs.render(matrix, i, j, f);
        this.renderBorders(0, 32);
        this.renderBorders(this.height-32, this.height);
        super.render(matrix, i, j, f);
        String curInfo = new TranslatableComponent("screen.musictriggers.text.edit_song").getString();
        if(this.scrollingSongs.getSelected()!=null) curInfo = this.holder.getLinkingInfoAtIndex(this.songCode, this.song, this.scrollingSongs.index);
        drawCenteredString(matrix, this.font, curInfo, width/2, 8, 10526880);
    }

    @Override
    public boolean keyPressed(int keyCode, int i, int j) {
        if(keyCode==259) {
            if(!this.holder.getLinkingInfoAtIndex(this.songCode, this.song, this.scrollingSongs.index).matches("")) {
                this.holder.editLinkingInfoParameter(this.songCode, this.song, this.scrollingSongs.index, StringUtils.chop(this.holder.getLinkingInfoAtIndex(this.songCode, this.song, this.scrollingSongs.index)));
                return true;
            } else if(this.holder.isLinkingInfoTrigger(this.songCode, this.song, this.scrollingSongs.index)) {
                this.holder.removeLinkingTrigger(this.songCode, this.song, this.scrollingSongs.index);
                this.scrollingSongs.refreshList(this.holder.getAllLinkingInfo(this.songCode, this.song));
                return true;
            }
        }
        return super.keyPressed(keyCode, i, j);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if(this.scrollingSongs.getSelected()!=null) {
            this.holder.editLinkingInfoParameter(this.songCode, this.song, this.scrollingSongs.index, this.holder.getLinkingInfoAtIndex(this.songCode, this.song, this.scrollingSongs.index) + typedChar);
            return true;
        }
        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public void init() {
        this.addBackButton();
        this.addScrollable();
        this.addAddTriggerButton();
        this.addAddLoopsButton();
        this.addDeleteButton();
        EventsClient.renderDebug = false;
    }

    private void addScrollable() {
        this.scrollingSongs = new GuiScrollingLinkingInfo(this.minecraft, this.width, this.height,32,this.height-32, this.info,this);
        this.scrollingSongs.setRenderBackground(false);
        this.scrollingSongs.setRenderTopAndBottom(false);
        this.addWidget(this.scrollingSongs);
    }

    private void addBackButton() {
        this.addRenderableWidget(new Button(16, 8, 64, 16, new TranslatableComponent("screen.musictriggers.button.back"),
                (button) -> {
                    assert this.minecraft != null;
                    if(this.parentScreen instanceof GuiLinking) ((GuiLinking) this.parentScreen).holder = this.holder;
                    else ((GuiAddSongs) this.parentScreen).holder = this.holder;
                    this.minecraft.setScreen(this.parentScreen);
                }));
    }

    private void addDeleteButton() {
        this.addRenderableWidget(new Button(this.width - 80, 8, 64, 16, new TranslatableComponent("screen.musictriggers.button.delete").withStyle(ChatFormatting.RED),
                (button) -> {
                    assert this.minecraft != null;
                    if (this.parentScreen instanceof GuiLinking parent) {
                        parent.holder.removeLinkingSong(this.songCode, this.song);
                        parent.songs = parent.holder.getAllSongsForLinking(this.songCode);
                    } else ((GuiAddSongs) this.parentScreen).holder = this.holder;
                    this.minecraft.setScreen(this.parentScreen);
                }));
    }

    private void addAddTriggerButton() {
        this.addRenderableWidget(new Button(this.width/2-114, this.height-24, 96, 16, new TranslatableComponent("screen.musictriggers.button.add_trigger"),
                (button) -> {
                    this.holder.addLinkingTrigger(this.songCode, this.song, "trigger");
                    this.info = this.holder.getAllLinkingInfo(this.songCode, this.song);
                    this.scrollingSongs.refreshList(this.info);
                }));
    }

    private void addAddLoopsButton() {
        this.addRenderableWidget(new Button(this.width/2+16, this.height-24, 96, 16, new TranslatableComponent("screen.musictriggers.button.addloop"),
                (button) -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new GuiLoops(this,this.holder,this.songCode,this.song,true));
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
