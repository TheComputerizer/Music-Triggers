package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.client.eventsClient;
import mods.thecomputerizer.musictriggers.config.configObject;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

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
    private final Identifier background;

    public GuiTransitionInfo(Screen parentScreen, configObject holder, int index, boolean create, boolean title, boolean ismoving, String name) {
        super(new TranslatableText("screen.musictriggers.transition_info"));
        this.parentScreen = parentScreen;
        this.index = index;
        this.holder = holder;
        this.title = title;
        if(create) this.index = this.holder.addTransition(this.title, ismoving, name);
        MusicTriggersCommon.logger.info(this.index);
        this.parameters = this.holder.getAllTransitionParametersAtIndex(this.title, this.index);
        this.background = new Identifier(MusicTriggersCommon.MODID,"textures/block/recorder_side_active.png");
    }

    @Override
    public void render(@NotNull MatrixStack matrix, int i, int j, float f) {
        this.fillGradient(matrix, 0, 0, this.width, this.height, -1072689136, -804253680);
        this.scrollingSongs.render(matrix, i, j, f);
        this.renderBorders(0, 32);
        this.renderBorders(this.height-32, this.height);
        super.render(matrix, i, j, f);
        String curInfo = this.trigger;
        if(this.scrollingSongs.getSelectedOrNull()!=null) curInfo = this.holder.getTransitionInfoAtIndex(this.title, this.index, this.scrollingSongs.index);
        drawCenteredText(matrix, this.textRenderer, curInfo, width/2, 8, 10526880);
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
        if(this.scrollingSongs.getSelectedOrNull()!=null) {
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
                case 1 -> multiplier = 10;
                case 2 -> multiplier = 100;
                case 3 -> multiplier = 1000;
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
        if(this.title) {
            this.addAddTitleButton();
            this.addAddSubtitleButton();
        }
        eventsClient.renderDebug = false;
    }

    private void addScrollable() {
        this.scrollingSongs = new GuiScrollingTransitionsInfo(this.client, this.width, this.height,32,this.height-32, this.parameters, this);
        this.scrollingSongs.setRenderBackground(false);
        this.addSelectableChild(this.scrollingSongs);
    }

    private void addBackButton() {
        this.addDrawableChild(new ButtonWidget(16, 8, 64, 16, new TranslatableText("screen.musictriggers.button.back"),
                (button) -> {
                    assert this.client != null;
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
                    this.client.setScreen(this.parentScreen);
                }));
    }

    private void addDeleteButton() {
        this.addDrawableChild(new ButtonWidget(this.width - 80, 8, 64, 16, new TranslatableText("screen.musictriggers.button.delete").setStyle(Style.EMPTY.withFormatting(Formatting.RED)),
                (button) -> {
                    assert this.client != null;
                    if(this.parentScreen instanceof GuiTransitions parent) {
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
                    this.client.setScreen(this.parentScreen);
                }));
    }

    private void addAddTriggerButton() {
        this.addDrawableChild(new ButtonWidget(this.width/2-48, this.height-24, 96, 16, new TranslatableText("screen.musictriggers.button.add_trigger"),
                (button) -> {
                    this.holder.addTransitionTrigger(this.title, this.index);
                    this.parameters = this.holder.getAllTransitionParametersAtIndex(this.title, this.index);
                    this.scrollingSongs.info = this.parameters;
                    this.scrollingSongs.resetEntries();
                }));
    }

    private void addAddTitleButton() {
        this.addDrawableChild(new ButtonWidget(this.width/2-160, this.height-24, 96, 16, new TranslatableText("screen.musictriggers.button.add_title"),
                (button) -> {
                    this.holder.addTitle(this.index);
                    this.parameters = this.holder.getAllTransitionParametersAtIndex(this.title, this.index);
                    this.scrollingSongs.info = this.parameters;
                    this.scrollingSongs.resetEntries();
                }));
    }

    private void addAddSubtitleButton() {
        this.addDrawableChild(new ButtonWidget(this.width/2+64, this.height-24, 96, 16, new TranslatableText("screen.musictriggers.button.add_subtitle"),
                (button) -> {
                    this.holder.addSubtitle(this.index);
                    this.parameters = this.holder.getAllTransitionParametersAtIndex(this.title, this.index);
                    this.scrollingSongs.info = this.parameters;
                    this.scrollingSongs.resetEntries();
                }));
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
        eventsClient.renderDebug = true;
        super.onClose();
    }
}
