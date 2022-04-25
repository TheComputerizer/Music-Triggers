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

public class GuiTransitions extends Screen {

    public List<String> info;
    public Screen parentScreen;
    public GuiScrollingTransitions scrollingSongs;
    public ConfigObject holder;
    private final ResourceLocation background;

    public GuiTransitions(Screen parentScreen, ConfigObject holder) {
        super(new TranslationTextComponent("screen.musictriggers.transitions"));
        this.parentScreen = parentScreen;
        this.holder = holder;
        this.info = this.holder.getAllTransitions();
        this.background = new ResourceLocation(MusicTriggers.MODID,"textures/block/recorder_side_active.png");
    }

    @Override
    public void render(MatrixStack matrix, int i, int j, float f) {
        this.fillGradient(matrix, 0, 0, this.width, this.height, -1072689136, -804253680);
        this.scrollingSongs.render(matrix, i, j, f);
        this.renderBorders(0, 32);
        this.renderBorders(this.height-32, this.height);
        super.render(matrix, i, j, f);
        String curInfo = new TranslationTextComponent("screen.musictriggers.text.transitions").getString();
        if(this.scrollingSongs.getSelected()!=null) curInfo = this.holder.getAllTriggersForTransition(this.scrollingSongs.index);
        drawCenteredString(matrix, this.font, curInfo, width/2, 8, 10526880);
    }

    @Override
    public void init() {
        this.addBackButton();
        this.addScrollable();
        this.addAddTransitionButton();
        EventsClient.renderDebug = false;
    }

    private void addScrollable() {
        this.scrollingSongs = new GuiScrollingTransitions(this.minecraft, this.width, this.height,32,this.height-32, this.info,this);
        this.scrollingSongs.setRenderBackground(false);
        this.scrollingSongs.setRenderTopAndBottom(false);
        this.children.add(this.scrollingSongs);
    }

    private void addBackButton() {
        this.addButton(new Button(16, 8, 64, 16, new TranslationTextComponent("screen.musictriggers.button.back"),
                (button) -> {
                    assert this.minecraft != null;
                    ((GuiMain)this.parentScreen).holder = this.holder;
                    this.minecraft.setScreen(this.parentScreen);
                }));
    }

    private void addAddTransitionButton() {
        this.addButton(new Button(this.width/2-64, this.height-24, 128, 16, new TranslationTextComponent("screen.musictriggers.button.add_transition"),
                (button) -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new GuiAddTransition(this, this.holder));
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
