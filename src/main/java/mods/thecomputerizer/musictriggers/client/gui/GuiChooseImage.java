package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.config.ConfigObject;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;
import java.util.Map;

public class GuiChooseImage extends Screen {

    public Map<String, Boolean> imageMap;
    public List<String> images;
    public Screen parentScreen;
    public GuiScrollingChooseImage scrollingSongs;
    public ConfigObject holder;
    private final ResourceLocation background;

    public GuiChooseImage(Screen parentScreen, ConfigObject holder) {
        super(new TranslationTextComponent("screen.musictriggers.choose_image"));
        this.parentScreen = parentScreen;
        this.holder = holder;
        this.imageMap = this.holder.getAllImages();
        this.images = this.holder.extractStringListFromMapKeys(this.imageMap);
        this.background = new ResourceLocation(MusicTriggers.MODID,"textures/block/recorder_side_active.png");
    }
    @Override
    public void render(MatrixStack matrix, int i, int j, float f) {
        this.fillGradient(matrix, 0, 0, this.width, this.height, -1072689136, -804253680);
        this.scrollingSongs.render(matrix, i, j, f);
        this.renderBorders(0, 32);
        this.renderBorders(this.height-32, this.height);
        super.render(matrix, i, j, f);
        drawCenteredString(matrix, this.font, new TranslationTextComponent("screen.musictriggers.text.add_card"), width/2, 8, 10526880);
    }

    @Override
    public void init() {
        this.addBackButton();
        this.addSongs();
        EventsClient.renderDebug = false;
    }

    private void addSongs() {
        this.scrollingSongs = new GuiScrollingChooseImage(this.minecraft, this.width, this.height,32,this.height-32, this.images, this, this.imageMap);
        this.scrollingSongs.setRenderBackground(false);
        this.scrollingSongs.setRenderTopAndBottom(false);
        this.children.add(this.scrollingSongs);
    }

    private void addBackButton() {
        this.addButton(new Button(16, 8, 64, 16, new TranslationTextComponent("screen.musictriggers.button.back"),
                (button) -> {
                    assert this.minecraft != null;
                    ((GuiAddTransition)this.parentScreen).holder = this.holder;
                    this.minecraft.setScreen(this.parentScreen);
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
        EventsClient.renderDebug = true;
        super.onClose();
    }
}
