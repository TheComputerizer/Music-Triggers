package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.sun.org.apache.xpath.internal.operations.Bool;
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
import java.util.List;

public class GuiTransitionInfo extends Screen {

    public String trigger;
    public int index;
    public List<String> parameters;
    public boolean title;
    public Screen parentScreen;
    public GuiScrollingTransitionsInfo scrollingSongs;
    public configObject holder;
    private final ResourceLocation background;

    public GuiTransitionInfo(Screen parentScreen, configObject holder, int index, boolean create, boolean title, boolean ismoving, String name) {
        super(new TranslationTextComponent("screen.musictriggers.transition_info"));
        this.parentScreen = parentScreen;
        this.index = index;
        this.holder = holder;
        this.title = title;
        if(create) this.index = this.holder.addTransition(this.title, ismoving, name);
        MusicTriggers.logger.info(this.index);
        this.parameters = this.holder.getAllTransitionParametersAtIndex(this.title, this.index);
        this.background = new ResourceLocation(MusicTriggers.MODID,"textures/block/recorder_side_active.png");
    }

    @Override
    public void render(MatrixStack matrix, int i, int j, float f) {
        this.fillGradient(matrix, 0, 0, this.width, this.height, -1072689136, -804253680);
        this.scrollingSongs.render(matrix, i, j, f);
        this.renderBorders(0, 32);
        this.renderBorders(this.height-32, this.height);
        super.render(matrix, i, j, f);
        String curInfo = this.trigger;
        if(this.scrollingSongs.getSelected()!=null) curInfo = this.holder.getTransitionInfoAtIndex(this.title, this.index, this.scrollingSongs.index);
        drawCenteredString(matrix, this.font, curInfo, width/2, 8, 10526880);
    }

    @Override
    public boolean keyPressed(int keyCode, int i, int j) {
        if(keyCode==259) {
            if(!this.holder.getTransitionInfoAtIndex(this.title, this.index, this.scrollingSongs.index).matches("")) {
                this.holder.editTransitionInfoAtIndex(this.title, this.index, this.scrollingSongs.index, StringUtils.chop(this.holder.getTransitionInfoAtIndex(this.title, this.index, this.scrollingSongs.index)), getChange('~'));
                return true;
            } else if(this.holder.getTransitionInfoAtIndex(this.title, this.index, this.scrollingSongs.index).matches("") && this.holder.checkIfTransitionIndexIsArray(this.title, this.index, this.scrollingSongs.index)) {
                this.holder.removeTransitionTrigger(this.title, this.index, this.scrollingSongs.index);
                this.parameters = this.holder.getAllTransitionParametersAtIndex(this.title, this.index);
                this.scrollingSongs.info = this.parameters;
                this.scrollingSongs.resetEntries();
                return true;
            }
        }
        return super.keyPressed(keyCode, i, j);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if(this.scrollingSongs.getSelected()!=null) {
            this.holder.editTransitionInfoAtIndex(this.title, this.index, this.scrollingSongs.index, this.holder.getTransitionInfoAtIndex(this.title, this.index, this.scrollingSongs.index) + typedChar, getChange(typedChar));
            return true;
        }
        return super.charTyped(typedChar, keyCode);
    }

    private int getChange(char typedChar) {
        List<Boolean> hold = new ArrayList<>();
        if(Screen.hasShiftDown()) hold.add(true);
        if(Screen.hasControlDown()) hold.add(true);
        if(Screen.hasAltDown()) hold.add(true);
        int multiplier = 1;
        if(hold.size()>0) {
            switch (hold.size()) {
                case 1:
                    multiplier = 10;
                    break;
                case 2:
                    multiplier = 100;
                    break;
                case 3:
                    multiplier = 1000;
                    break;
            }
        }
        if(typedChar=='~') return multiplier*-1;
        else if (Character.getNumericValue(typedChar)==-1) return multiplier;
        return Character.getNumericValue(typedChar)*multiplier;
    }

    @Override
    public void init() {
        this.addBackButton();
        this.addScrollable();
        this.addDeleteButton();
        this.addAddTriggerButton();
        if(title) {
            this.addAddTitleButton();
            this.addAddSubtitleButton();
        }
        eventsClient.renderDebug = false;
    }

    private void addScrollable() {
        this.scrollingSongs = new GuiScrollingTransitionsInfo(this.minecraft, this.width, this.height,32,this.height-32, this.parameters, this);
        this.scrollingSongs.setRenderBackground(false);
        this.scrollingSongs.setRenderTopAndBottom(false);
        this.children.add(this.scrollingSongs);
    }

    private void addBackButton() {
        this.addButton(new Button(16, 8, 64, 16, new TranslationTextComponent("screen.musictriggers.button.back"),
                (button) -> {
                    assert this.minecraft != null;
                    if(parentScreen instanceof GuiTransitions) {
                        GuiTransitions parent = (GuiTransitions) this.parentScreen;
                        parent.holder = this.holder;
                        parent.info = this.holder.getAllTransitions();
                        parent.scrollingSongs.info = parent.info;
                        parent.scrollingSongs.size = parent.info.size();
                    }
                    else if(parentScreen instanceof GuiAddTransition) {
                        ((GuiAddTransition) this.parentScreen).holder = this.holder;
                        GuiTransitions parent = (GuiTransitions)((GuiAddTransition)this.parentScreen).parentScreen;
                        parent.info = this.holder.getAllTransitions();
                        parent.scrollingSongs.info = parent.info;
                        parent.scrollingSongs.size = parent.info.size();
                    }
                    else {
                        ((GuiChooseImage) this.parentScreen).holder = this.holder;
                        GuiTransitions parent = (GuiTransitions)((GuiAddTransition)((GuiChooseImage)this.parentScreen).parentScreen).parentScreen;
                        parent.info = this.holder.getAllTransitions();
                        parent.scrollingSongs.info = parent.info;
                        parent.scrollingSongs.size = parent.info.size();
                    }
                    this.minecraft.setScreen(this.parentScreen);
                }));
    }

    private void addDeleteButton() {
        this.addButton(new Button(this.width - 80, 8, 64, 16, new TranslationTextComponent("screen.musictriggers.button.delete").withStyle(TextFormatting.RED),
                (button) -> {
                    assert this.minecraft != null;
                    if(this.parentScreen instanceof GuiTransitions) {
                        GuiTransitions parent = ((GuiTransitions) this.parentScreen);
                        this.holder.removeTransition(this.title, this.index);
                        parent.holder = this.holder;
                        parent.info = parent.holder.getAllTransitions();
                    } else if(this.parentScreen instanceof GuiAddTransition) {
                        GuiTransitions parent = (GuiTransitions)((GuiAddTransition)this.parentScreen).parentScreen;
                        this.holder.removeTransition(this.title, this.index);
                        ((GuiAddTransition)this.parentScreen).holder = this.holder;
                        parent.info = parent.holder.getAllTransitions();
                    } else {
                        GuiTransitions parent = (GuiTransitions)((GuiAddTransition)((GuiChooseImage)this.parentScreen).parentScreen).parentScreen;
                        this.holder.removeTransition(this.title, this.index);
                        ((GuiChooseImage)this.parentScreen).holder = this.holder;
                        parent.info = parent.holder.getAllTransitions();
                    }
                    this.minecraft.setScreen(this.parentScreen);
                }));
    }

    private void addAddTriggerButton() {
        this.addButton(new Button(this.width/2-48, this.height-24, 96, 16, new TranslationTextComponent("screen.musictriggers.button.add_trigger"),
                (button) -> {
                    this.holder.addTransitionTrigger(this.title, this.index);
                    this.parameters = this.holder.getAllTransitionParametersAtIndex(this.title, this.index);
                    this.scrollingSongs.info = this.parameters;
                    this.scrollingSongs.resetEntries();
                }));
    }

    private void addAddTitleButton() {
        this.addButton(new Button(this.width/2-160, this.height-24, 96, 16, new TranslationTextComponent("screen.musictriggers.button.add_title"),
                (button) -> {
                    this.holder.addTitle(this.index);
                    this.parameters = this.holder.getAllTransitionParametersAtIndex(this.title, this.index);
                    this.scrollingSongs.info = this.parameters;
                    this.scrollingSongs.resetEntries();
                }));
    }

    private void addAddSubtitleButton() {
        this.addButton(new Button(this.width/2+64, this.height-24, 96, 16, new TranslationTextComponent("screen.musictriggers.button.add_subtitle"),
                (button) -> {
                    this.holder.addSubtitle(this.index);
                    this.parameters = this.holder.getAllTransitionParametersAtIndex(this.title, this.index);
                    this.scrollingSongs.info = this.parameters;
                    this.scrollingSongs.resetEntries();
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
